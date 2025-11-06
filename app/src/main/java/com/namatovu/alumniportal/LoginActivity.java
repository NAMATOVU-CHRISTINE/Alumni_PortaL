package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.namatovu.alumniportal.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("LoginActivity", "App Launch");

        // Set click listeners
        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.signupPrompt.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in and verified
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            navigateToHome();
        }
    }

    private void loginUser() {
        String username = binding.emailHint.getText().toString().trim();
        String password = binding.passwordHint.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(username)) {
            binding.emailHint.setError("Username is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.passwordHint.setError("Password is required.");
            return;
        }

        // Look up the user's email from their username in Firestore
        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String email = task.getResult().getDocuments().get(0).getString("email");
                        if (email != null) {
                            // Username found, proceed to sign in with email and password
                            signInWithEmail(email, password);
                        } else {
                            Toast.makeText(this, "Could not find an email for this user.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (task.isSuccessful()) {
                        // Task was successful, but no user was found
                        Toast.makeText(this, "Username not found.", Toast.LENGTH_SHORT).show();
                    } else {
                        // An error occurred while fetching the user
                        Log.e(TAG, "Error looking up username", task.getException());
                        Toast.makeText(this, "Error finding user. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Login successful and email is verified
                            Log.d(TAG, "Login successful.");
                            navigateToHome();
                        } else {
                            // Login successful, but email is not verified
                            Log.w(TAG, "Login successful, but email not verified.");
                            Toast.makeText(LoginActivity.this, "Please verify your email address first.", Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Sign out to force user to log in again after verification
                        }                    } else {
                        // Authentication failed (e.g., incorrect password)
                        Log.w(TAG, "Authentication failed.", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void navigateToHome() {
        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
        
        // Log analytics event for successful login
        AnalyticsHelper.logLogin("email");
        if (mAuth.getCurrentUser() != null) {
            AnalyticsHelper.setUserId(mAuth.getCurrentUser().getUid());
        }
        
        // Schedule background data sync now that user is logged in
        if (getApplication() instanceof AlumniApplication) {
            ((AlumniApplication) getApplication()).scheduleDataSync();
        }

        // Navigate to the main screen
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish LoginActivity so the user can't navigate back to it
    }
}

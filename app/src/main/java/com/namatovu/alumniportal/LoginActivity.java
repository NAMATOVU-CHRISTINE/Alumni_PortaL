package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.namatovu.alumniportal.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

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

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    }
                });

        // Set click listeners
        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        binding.signupPrompt.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        binding.forgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            hideLoadingIndicator();
            Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Check if user exists in Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        // New user - save to Firestore
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("fullName", mAuth.getCurrentUser().getDisplayName());
                                        user.put("email", mAuth.getCurrentUser().getEmail());
                                        user.put("userId", userId);
                                        user.put("username", mAuth.getCurrentUser().getEmail().split("@")[0]);

                                        db.collection("users").document(userId).set(user);
                                    }
                                    navigateToHome();
                                })
                                .addOnFailureListener(e -> {
                                    hideLoadingIndicator();
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        hideLoadingIndicator();
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
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

        // Show loading indicator (Google style)
        binding.loginButton.setText("");
        binding.loginProgressBar.setVisibility(android.view.View.VISIBLE);
        binding.loginButton.setEnabled(false);
        binding.googleSignInButton.setEnabled(false);

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
                            hideLoadingIndicator();
                            Toast.makeText(this, "Could not find an email for this user.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (task.isSuccessful()) {
                        // Task was successful, but no user was found
                        hideLoadingIndicator();
                        Toast.makeText(this, "Username not found.", Toast.LENGTH_SHORT).show();
                    } else {
                        // An error occurred while fetching the user
                        hideLoadingIndicator();
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
                            hideLoadingIndicator();
                            Log.w(TAG, "Login successful, but email not verified.");
                            Toast.makeText(LoginActivity.this, "Please verify your email address first.", Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Sign out to force user to log in again after verification
                        }                    } else {
                        // Authentication failed (e.g., incorrect password)
                        hideLoadingIndicator();
                        Log.w(TAG, "Authentication failed.", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void hideLoadingIndicator() {
        binding.loginProgressBar.setVisibility(android.view.View.GONE);
        binding.loginButton.setText(R.string.login);
        binding.loginButton.setEnabled(true);
        binding.googleSignInButton.setEnabled(true);
    }
    
    private void navigateToHome() {
        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
        
        // Log analytics event for successful login
        AnalyticsHelper.logLogin("email");
        if (mAuth.getCurrentUser() != null) {
            AnalyticsHelper.setUserId(mAuth.getCurrentUser().getUid());
        }
        
        // Schedule background data sync now that user is logged in
        if (getApplication() instanceof AlumniPortalApplication) {
            // ((AlumniPortalApplication) getApplication()).scheduleDataSync();
            // Data sync functionality can be implemented later
        }

        // Navigate to the main screen
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish LoginActivity so the user can't navigate back to it
    }
}

package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameEditText, usernameEditText, studentIDEditText, personalEmailEditText, passwordEditText, confirmPasswordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullNameEditText = findViewById(R.id.fullName);
        usernameEditText = findViewById(R.id.username);
        studentIDEditText = findViewById(R.id.studentID);
        personalEmailEditText = findViewById(R.id.personalEmail);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        Button signupButton = findViewById(R.id.signupButton);
        Button googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView backToLoginText = findViewById(R.id.backToLogin);

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

        signupButton.setOnClickListener(v -> registerUser());

        googleSignInButton.setOnClickListener(v -> signUpWithGoogle());

        backToLoginText.setOnClickListener(v -> {
            finish();
        });
    }

    private void signUpWithGoogle() {
        showLoadingIndicator();
        // Sign out first to force account chooser
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            hideLoadingIndicator();
            Toast.makeText(this, "Google sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        
                        // Check if user already exists
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        // New user - redirect to complete profile
                                        Intent intent = new Intent(SignupActivity.this, CompleteGoogleSignupActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // User already exists, just login
                                        Toast.makeText(SignupActivity.this, "Account already exists. Logging you in...", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    hideLoadingIndicator();
                                    Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        hideLoadingIndicator();
                        Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String fullName = fullNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String studentID = studentIDEditText.getText().toString().trim();
        String personalEmail = personalEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("Full name is required");
            fullNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(studentID)) {
            studentIDEditText.setError("Student ID is required");
            studentIDEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(personalEmail)) {
            personalEmailEditText.setError("Email is required");
            personalEmailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Show loading indicator
        showLoadingIndicator();

        // Check if username already exists
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        hideLoadingIndicator();
                        usernameEditText.setError("Username already exists");
                        usernameEditText.requestFocus();
                    } else {
                        // Create Firebase Auth account
                        mAuth.createUserWithEmailAndPassword(personalEmail, password)
                                .addOnCompleteListener(this, authTask -> {
                                    if (authTask.isSuccessful()) {
                                        String userId = mAuth.getCurrentUser().getUid();

                                        // Send email verification
                                        mAuth.getCurrentUser().sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    if (emailTask.isSuccessful()) {
                                                        // Save user data to Firestore
                                                        Map<String, Object> user = new HashMap<>();
                                                        user.put("fullName", fullName);
                                                        user.put("username", username);
                                                        user.put("studentID", studentID);
                                                        user.put("email", personalEmail);
                                                        user.put("userId", userId);

                                                        db.collection("users").document(userId)
                                                                .set(user)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    hideLoadingIndicator();
                                                                    Toast.makeText(SignupActivity.this, "Registration successful! Please check your email to verify your account.", Toast.LENGTH_LONG).show();
                                                                    mAuth.signOut(); // Sign out to force verification
                                                                    finish(); // Go back to login
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    hideLoadingIndicator();
                                                                    Toast.makeText(SignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                });
                                                    } else {
                                                        hideLoadingIndicator();
                                                        Toast.makeText(SignupActivity.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        hideLoadingIndicator();
                                        Toast.makeText(SignupActivity.this, "Registration failed: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
    }
    
    private void showLoadingIndicator() {
        ProgressBar progressBar = findViewById(R.id.signupProgressBar);
        Button signupButton = findViewById(R.id.signupButton);
        Button googleButton = findViewById(R.id.googleSignInButton);
        
        if (progressBar != null) progressBar.setVisibility(android.view.View.VISIBLE);
        if (signupButton != null) signupButton.setEnabled(false);
        if (googleButton != null) googleButton.setEnabled(false);
    }
    
    private void hideLoadingIndicator() {
        ProgressBar progressBar = findViewById(R.id.signupProgressBar);
        Button signupButton = findViewById(R.id.signupButton);
        Button googleButton = findViewById(R.id.googleSignInButton);
        
        if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
        if (signupButton != null) signupButton.setEnabled(true);
        if (googleButton != null) googleButton.setEnabled(true);
    }
}

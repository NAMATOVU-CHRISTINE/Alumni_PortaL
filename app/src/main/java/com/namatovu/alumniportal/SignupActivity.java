package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameEditText, usernameEditText, studentIDEditText, personalEmailEditText, passwordEditText, confirmPasswordEditText;
    private android.widget.AutoCompleteTextView userTypeDropdown;
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
        userTypeDropdown = findViewById(R.id.userTypeDropdown);
        personalEmailEditText = findViewById(R.id.personalEmail);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        Button signupButton = findViewById(R.id.signupButton);
        Button googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView backToLoginText = findViewById(R.id.backToLogin);
        
        // Setup user type dropdown
        setupUserTypeDropdown();

        // Configure Google Sign-In with email selection
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
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
        // Sign out first to show account picker
        mAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Launch Google Sign-in to get email and account info
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                android.util.Log.d("SignupActivity", "Google account obtained: " + account.getEmail());
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                hideLoadingIndicator();
                android.util.Log.e("SignupActivity", "Google account is null or missing ID token");
                Toast.makeText(this, "Google sign up failed: Invalid account", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            hideLoadingIndicator();
            android.util.Log.e("SignupActivity", "Google sign up error code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
            String errorMsg = "Google sign up failed";
            if (e.getStatusCode() == 12500) {
                errorMsg = "Network error. Please check your connection.";
            } else if (e.getStatusCode() == 12501) {
                errorMsg = "Sign in was cancelled.";
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        String googleEmail = mAuth.getCurrentUser().getEmail();
                        
                        // Check if user already exists
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        // New user - redirect to complete profile
                                        hideLoadingIndicator();
                                        Intent intent = new Intent(SignupActivity.this, CompleteGoogleSignupActivity.class);
                                        intent.putExtra("googleEmail", googleEmail);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // User already exists - check if email is verified
                                        hideLoadingIndicator();
                                        Boolean emailVerified = documentSnapshot.getBoolean("emailVerified");
                                        if (emailVerified != null && emailVerified) {
                                            // Email verified, go to home
                                            Toast.makeText(SignupActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Email not verified yet, show verification screen
                                            Toast.makeText(SignupActivity.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                            intent.putExtra("email_verification_pending", true);
                                            intent.putExtra("user_email", googleEmail);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    hideLoadingIndicator();
                                    android.util.Log.e("SignupActivity", "Error checking user", e);
                                    Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    // Sign out on failure
                                    mAuth.signOut();
                                });
                    } else {
                        hideLoadingIndicator();
                        android.util.Log.e("SignupActivity", "Firebase auth failed", task.getException());
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

                                        // Save user data to Firestore first
                                        String selectedUserType = userTypeDropdown.getText() != null ? 
                                            userTypeDropdown.getText().toString().trim() : "Student";
                                        
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("fullName", fullName);
                                        user.put("username", username);
                                        user.put("studentID", studentID);
                                        user.put("email", personalEmail);
                                        user.put("userId", userId);
                                        user.put("userType", selectedUserType.toLowerCase());
                                        user.put("isAlumni", "alumni".equalsIgnoreCase(selectedUserType));
                                        user.put("emailVerified", false);

                                        db.collection("users").document(userId)
                                                .set(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Send email verification after saving user
                                                    mAuth.getCurrentUser().sendEmailVerification()
                                                            .addOnCompleteListener(emailTask -> {
                                                                hideLoadingIndicator();
                                                                if (emailTask.isSuccessful()) {
                                                                    Toast.makeText(SignupActivity.this, "Registration successful! Please verify your email to continue.", Toast.LENGTH_LONG).show();
                                                                    // Sign out user until they verify email
                                                                    mAuth.signOut();
                                                                    // Show verification pending message and go back to login
                                                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                                    intent.putExtra("email_verification_pending", true);
                                                                    intent.putExtra("user_email", personalEmail);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(SignupActivity.this, "Registration successful! But verification email failed to send. Please try logging in.", Toast.LENGTH_LONG).show();
                                                                    // Sign out and go back to login
                                                                    mAuth.signOut();
                                                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    hideLoadingIndicator();
                                                    Toast.makeText(SignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        hideLoadingIndicator();
                                        Toast.makeText(SignupActivity.this, "Registration failed: " + Objects.requireNonNull(authTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
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
        if (signupButton != null) {
            signupButton.setText("");  // Hide text like Google does
            signupButton.setEnabled(false);
        }
        if (googleButton != null) googleButton.setEnabled(false);
    }
    
    private void hideLoadingIndicator() {
        ProgressBar progressBar = findViewById(R.id.signupProgressBar);
        Button signupButton = findViewById(R.id.signupButton);
        Button googleButton = findViewById(R.id.googleSignInButton);
        
        if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
        if (signupButton != null) {
            signupButton.setText(R.string.signup_button_text);  // Restore text
            signupButton.setEnabled(true);
        }
        if (googleButton != null) googleButton.setEnabled(true);
    }
    
    private void setupUserTypeDropdown() {
        // User Type options:
        // - "Student" = Current students
        // - "Alumni" = Graduated students
        // - "Staff" = Faculty/staff members
        String[] userTypes = {"Student", "Alumni", "Staff"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, userTypes);
        userTypeDropdown.setAdapter(adapter);
        userTypeDropdown.setText("Student", false); // Default to Student
    }
}

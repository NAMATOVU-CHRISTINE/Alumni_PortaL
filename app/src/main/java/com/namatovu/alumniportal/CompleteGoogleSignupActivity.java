package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CompleteGoogleSignupActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, usernameEditText, studentIDEditText, passwordEditText, confirmPasswordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_google_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullNameEditText = findViewById(R.id.fullName);
        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.username);
        studentIDEditText = findViewById(R.id.studentID);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        Button completeSignupButton = findViewById(R.id.completeSignupButton);

        // Pre-fill data from Google account
        if (mAuth.getCurrentUser() != null) {
            fullNameEditText.setText(mAuth.getCurrentUser().getDisplayName());
            emailEditText.setText(mAuth.getCurrentUser().getEmail());
            emailEditText.setEnabled(true); // Allow user to edit email
            
            // Suggest a username from email
            String suggestedUsername = mAuth.getCurrentUser().getEmail().split("@")[0];
            usernameEditText.setText(suggestedUsername);
        }

        completeSignupButton.setOnClickListener(v -> completeSignup());
    }

    private void completeSignup() {
        String username = usernameEditText.getText().toString().trim();
        String studentID = studentIDEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

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
                        // Update the user's password in Firebase Auth
                        mAuth.getCurrentUser().updatePassword(password)
                                .addOnSuccessListener(aVoid -> {
                                    // Save user data to Firestore with default type as "student"
                                    String userId = mAuth.getCurrentUser().getUid();
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("fullName", fullNameEditText.getText().toString());
                                    user.put("email", emailEditText.getText().toString());
                                    user.put("username", username);
                                    user.put("studentID", studentID);
                                    user.put("userId", userId);
                                    user.put("userType", "student"); // Default: Current students
                                    user.put("isAlumni", false);

                                    db.collection("users").document(userId)
                                            .set(user)
                                            .addOnSuccessListener(aVoid2 -> {
                                                // Send email verification
                                                mAuth.getCurrentUser().sendEmailVerification()
                                                        .addOnCompleteListener(emailTask -> {
                                                            hideLoadingIndicator();
                                                            if (emailTask.isSuccessful()) {
                                                                Toast.makeText(CompleteGoogleSignupActivity.this, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show();
                                                                // Navigate to home
                                                                Intent intent = new Intent(CompleteGoogleSignupActivity.this, HomeActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                Toast.makeText(CompleteGoogleSignupActivity.this, "Registration successful! Verification email failed to send.", Toast.LENGTH_LONG).show();
                                                                Intent intent = new Intent(CompleteGoogleSignupActivity.this, HomeActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                hideLoadingIndicator();
                                                Toast.makeText(CompleteGoogleSignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    hideLoadingIndicator();
                                    Toast.makeText(CompleteGoogleSignupActivity.this, "Failed to set password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void showLoadingIndicator() {
        Button completeSignupButton = findViewById(R.id.completeSignupButton);
        if (completeSignupButton != null) {
            completeSignupButton.setText("");
            completeSignupButton.setEnabled(false);
        }
    }

    private void hideLoadingIndicator() {
        Button completeSignupButton = findViewById(R.id.completeSignupButton);
        if (completeSignupButton != null) {
            completeSignupButton.setText("Complete Signup");
            completeSignupButton.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        // Sign out the user if they press back
        mAuth.signOut();
        super.onBackPressed();
    }
}

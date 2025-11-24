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
    private android.widget.AutoCompleteTextView userTypeDropdown;
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
        userTypeDropdown = findViewById(R.id.userTypeDropdown);
        Button completeSignupButton = findViewById(R.id.completeSignupButton);
        
        setupUserTypeDropdown();

        // Pre-fill data from Google account
        if (mAuth.getCurrentUser() != null) {
            fullNameEditText.setText(mAuth.getCurrentUser().getDisplayName());
            
            // Get email from intent or from Google account
            String googleEmail = getIntent().getStringExtra("googleEmail");
            if (googleEmail == null) {
                googleEmail = mAuth.getCurrentUser().getEmail();
            }
            emailEditText.setText(googleEmail);
            emailEditText.setEnabled(true); // Allow user to edit email
            
            // Suggest a username from email
            String suggestedUsername = googleEmail.split("@")[0];
            usernameEditText.setText(suggestedUsername);
        }

        completeSignupButton.setOnClickListener(v -> completeSignup());
    }

    private void setupUserTypeDropdown() {
        String[] userTypes = {"Student", "Alumni", "Staff"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, userTypes);
        userTypeDropdown.setAdapter(adapter);
        userTypeDropdown.setText("Student", false); // Default to Student
        
        // Add listener to update ID field hint when user type changes
        userTypeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = userTypes[position];
            updateIdFieldHint(selectedType);
        });
    }
    
    private void updateIdFieldHint(String userType) {
        // Find the TextInputLayout for Student ID
        android.view.ViewParent parent = studentIDEditText.getParent();
        if (parent instanceof com.google.android.material.textfield.TextInputLayout) {
            com.google.android.material.textfield.TextInputLayout studentIdLayout = 
                (com.google.android.material.textfield.TextInputLayout) parent;
            
            if ("student".equalsIgnoreCase(userType)) {
                studentIdLayout.setHint("Student ID");
            } else {
                // Alumni and Staff both use "ID NO"
                studentIdLayout.setHint("ID NO");
            }
        }
    }

    private void completeSignup() {
        String username = usernameEditText.getText().toString().trim();
        String studentID = studentIDEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String userType = userTypeDropdown.getText().toString().trim();

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

        if (TextUtils.isEmpty(userType)) {
            Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show();
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
                                    // Save user data to Firestore with selected user type
                                    String userId = mAuth.getCurrentUser().getUid();
                                    String selectedUserType = userTypeDropdown.getText().toString().trim().toLowerCase();
                                    boolean isAlumni = "alumni".equalsIgnoreCase(selectedUserType);
                                    
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("fullName", fullNameEditText.getText().toString());
                                    user.put("email", emailEditText.getText().toString());
                                    user.put("username", username);
                                    user.put("userId", userId);
                                    user.put("userType", selectedUserType);
                                    user.put("isAlumni", isAlumni);
                                    
                                    // Save ID field based on user type
                                    if ("alumni".equalsIgnoreCase(selectedUserType)) {
                                        user.put("alumniID", studentID);
                                    } else if ("staff".equalsIgnoreCase(selectedUserType)) {
                                        user.put("staffID", studentID);
                                    } else {
                                        user.put("studentID", studentID);
                                    }
                                    
                                    // Get and save FCM token immediately
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                                            .addOnCompleteListener(tokenTask -> {
                                                if (tokenTask.isSuccessful()) {
                                                    String fcmToken = tokenTask.getResult();
                                                    user.put("fcmToken", fcmToken);
                                                    android.util.Log.d("CompleteGoogleSignup", "FCM token obtained: " + fcmToken);
                                                }
                                                
                                                db.collection("users").document(userId)
                                                        .set(user)
                                                        .addOnSuccessListener(aVoid2 -> {
                                                // Send email verification
                                                mAuth.getCurrentUser().sendEmailVerification()
                                                        .addOnCompleteListener(emailTask -> {
                                                            if (emailTask.isSuccessful()) {
                                                                // Update user document to mark email verification as pending
                                                                Map<String, Object> updateData = new HashMap<>();
                                                                updateData.put("emailVerified", false);
                                                                updateData.put("emailVerificationSent", System.currentTimeMillis());
                                                                
                                                                db.collection("users").document(userId)
                                                                        .update(updateData)
                                                                        .addOnSuccessListener(aVoid3 -> {
                                                                            hideLoadingIndicator();
                                                                            Toast.makeText(CompleteGoogleSignupActivity.this, "Profile complete! Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                                                            // Sign out user until they verify email
                                                                            mAuth.signOut();
                                                                            // Go back to login with verification pending message
                                                                            Intent intent = new Intent(CompleteGoogleSignupActivity.this, LoginActivity.class);
                                                                            intent.putExtra("email_verification_pending", true);
                                                                            intent.putExtra("user_email", emailEditText.getText().toString());
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            hideLoadingIndicator();
                                                                            Toast.makeText(CompleteGoogleSignupActivity.this, "Error updating profile. Please try again.", Toast.LENGTH_SHORT).show();
                                                                        });
                                                            } else {
                                                                hideLoadingIndicator();
                                                                Toast.makeText(CompleteGoogleSignupActivity.this, "Profile complete! But verification email failed to send. Please try logging in.", Toast.LENGTH_LONG).show();
                                                                // Sign out and go back to login
                                                                mAuth.signOut();
                                                                Intent intent = new Intent(CompleteGoogleSignupActivity.this, LoginActivity.class);
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

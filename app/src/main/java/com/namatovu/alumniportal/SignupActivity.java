package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.namatovu.alumniportal.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import com.namatovu.alumniportal.utils.ValidationHelper;
import com.namatovu.alumniportal.utils.FormValidationHelper;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("SignupActivity", "LoginActivity");

        // Setup real-time validation for all fields
        setupFormValidation();

        binding.signupButton.setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.fullName.addTextChangedListener(textWatcher);
        binding.username.addTextChangedListener(textWatcher);
        binding.studentID.addTextChangedListener(textWatcher);
        binding.personalEmail.addTextChangedListener(textWatcher);
        binding.password.addTextChangedListener(textWatcher);
        binding.confirmPassword.addTextChangedListener(textWatcher);

        binding.signupButton.setOnClickListener(v -> createUser());

        binding.backToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void setupFormValidation() {
        // Setup real-time validation for each field
        FormValidationHelper.setupFullNameValidation(binding.fullNameLayout, binding.fullName);
        FormValidationHelper.setupEmailValidation(binding.personalEmailLayout, binding.personalEmail);
        FormValidationHelper.setupStudentIdValidation(binding.studentIDLayout, binding.studentID);
        FormValidationHelper.setupPasswordValidation(binding.passwordLayout, binding.password);
        
        // Custom validation for username
        binding.username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                ValidationHelper.ValidationResult result = validateUsername(username);
                
                if (!result.isValid && !username.isEmpty()) {
                    binding.usernameLayout.setError(result.errorMessage);
                    binding.usernameLayout.setErrorEnabled(true);
                } else {
                    binding.usernameLayout.setError(null);
                    binding.usernameLayout.setErrorEnabled(false);
                }
                
                validateInputs();
            }
        });
        
        // Custom validation for confirm password
        binding.confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = binding.password.getText().toString();
                String confirmPassword = s.toString();
                
                if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
                    binding.confirmPasswordLayout.setError("Passwords do not match");
                    binding.confirmPasswordLayout.setErrorEnabled(true);
                } else {
                    binding.confirmPasswordLayout.setError(null);
                    binding.confirmPasswordLayout.setErrorEnabled(false);
                }
                
                validateInputs();
            }
        });

        // General text watcher for enabling/disabling the signup button
        TextWatcher generalWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.fullName.addTextChangedListener(generalWatcher);
        binding.personalEmail.addTextChangedListener(generalWatcher);
        binding.studentID.addTextChangedListener(generalWatcher);
        binding.password.addTextChangedListener(generalWatcher);
    }
    
    private ValidationHelper.ValidationResult validateUsername(String username) {
        if (username.isEmpty()) {
            return new ValidationHelper.ValidationResult(false, "Username is required");
        }
        
        if (username.length() < 3) {
            return new ValidationHelper.ValidationResult(false, "Username must be at least 3 characters long");
        }
        
        if (username.length() > 20) {
            return new ValidationHelper.ValidationResult(false, "Username is too long (max 20 characters)");
        }
        
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            return new ValidationHelper.ValidationResult(false, "Username can only contain letters, numbers, dots, underscores, and hyphens");
        }
        
        return new ValidationHelper.ValidationResult(true, null);
    }

    private void validateInputs() {
        String fullName = binding.fullName.getText().toString().trim();
        String username = binding.username.getText().toString().trim();
        String studentID = binding.studentID.getText().toString().trim();
        String email = binding.personalEmail.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();

        // Validate all fields using comprehensive validation
        ValidationHelper.ValidationResult emailResult = ValidationHelper.validateEmail(email);
        ValidationHelper.ValidationResult passwordResult = ValidationHelper.validatePassword(password);
        ValidationHelper.ValidationResult fullNameResult = ValidationHelper.validateFullName(fullName);
        ValidationHelper.ValidationResult studentIdResult = ValidationHelper.validateStudentId(studentID);
        ValidationHelper.ValidationResult usernameResult = validateUsername(username);
        
        boolean passwordsMatch = password.equals(confirmPassword);
        boolean allFieldsFilled = !fullName.isEmpty() && !username.isEmpty() && !studentID.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty();
        boolean allFieldsValid = emailResult.isValid && passwordResult.isValid && fullNameResult.isValid && 
                                studentIdResult.isValid && usernameResult.isValid && passwordsMatch;
        
        binding.signupButton.setEnabled(allFieldsFilled && allFieldsValid);
    }

    private void createUser() {
        String name = binding.fullName.getText().toString().trim();
        String username = binding.username.getText().toString().trim();
        String studentID = binding.studentID.getText().toString().trim();
        String email = binding.personalEmail.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();

        // Comprehensive validation before submission
        ValidationHelper.ValidationResult formResult = FormValidationHelper.validateForm(
            new FormValidationHelper.EmailField(email),
            new FormValidationHelper.PasswordField(password),
            new FormValidationHelper.FullNameField(name),
            new FormValidationHelper.StudentIdField(studentID),
            () -> validateUsername(username)
        );
        
        if (!formResult.isValid) {
            Toast.makeText(this, "Please fix the errors: " + formResult.getAllErrors(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Passwords do not match");
            return;
        }

        // Check username uniqueness
        db.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    binding.usernameLayout.setError("Username already taken");
                } else {
                    createAuthUser(email, password, name, username, studentID);
                }
            } else {
                Log.e(TAG, "Error checking if username exists", task.getException());
                Toast.makeText(SignupActivity.this, "Error checking username. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createAuthUser(String email, String password, String name, String username, String studentID) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, authTask -> {
                if (authTask.isSuccessful()) {
                    FirebaseUser firebaseUser = authTask.getResult().getUser(); // Use user from task result
                    if (firebaseUser != null) {
                        User newUser = new User(name, email, username, studentID);
                        sendVerificationEmail(firebaseUser, newUser);
                    }
                } else {
                    if (authTask.getException() instanceof FirebaseAuthUserCollisionException) {
                        binding.personalEmail.setError("This email address is already registered.");
                    } else {
                        Log.e(TAG, "createUserWithEmail failed", authTask.getException());
                        Toast.makeText(SignupActivity.this, "Authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser, User newUser) {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email verification sent.");
                        Toast.makeText(SignupActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                        saveUserToDatabase(firebaseUser.getUid(), newUser);
                    } else {
                        Log.e(TAG, "sendEmailVerification failed", task.getException());
                        Toast.makeText(SignupActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, User newUser) {
        db.collection("users").document(userId).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // Log analytics event for successful signup
                    AnalyticsHelper.logSignUp("email");
                    AnalyticsHelper.setUserId(userId);
                    
                    mAuth.signOut();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    Toast.makeText(SignupActivity.this, "A critical error occurred while saving your profile.", Toast.LENGTH_LONG).show();
                    AnalyticsHelper.logError("database_save_failed", e.getMessage(), "SignupActivity");
                });
    }
}

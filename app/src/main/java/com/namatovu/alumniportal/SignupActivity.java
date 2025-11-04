package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.namatovu.alumniportal.databinding.SignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private SignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

    private void validateInputs() {
        String fullName = binding.fullName.getText().toString().trim();
        String username = binding.username.getText().toString().trim();
        String studentID = binding.studentID.getText().toString().trim();
        String email = binding.personalEmail.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();

        boolean allFieldsFilled = !fullName.isEmpty() && !username.isEmpty() && !studentID.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty();
        binding.signupButton.setEnabled(allFieldsFilled);
    }

    private void createUser() {
        String name = binding.fullName.getText().toString().trim();
        String username = binding.username.getText().toString().trim();
        String studentID = binding.studentID.getText().toString().trim();
        String email = binding.personalEmail.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            binding.confirmPassword.setError("Passwords do not match");
            return;
        }

        db.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    binding.username.setError("Username already taken");
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
                    mAuth.signOut();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    Toast.makeText(SignupActivity.this, "A critical error occurred while saving your profile.", Toast.LENGTH_LONG).show();
                });
    }
}

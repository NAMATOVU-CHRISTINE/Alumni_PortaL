package com.example.alumni_portal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    private EditText fullName, studentID, personalEmail, password, confirmPassword;
    private Button signupButton;
    private TextView backToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // make sure this matches your XML file name

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inputs
        fullName = findViewById(R.id.fullName);
        studentID = findViewById(R.id.studentID);
        personalEmail = findViewById(R.id.personalEmail);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signupButton = findViewById(R.id.signupButton);
        backToLogin = findViewById(R.id.backToLogin);

        // Signup Button Click
        signupButton.setOnClickListener(v -> registerUser());

        // Back to Login
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(signup.this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = fullName.getText().toString().trim();
        String id = studentID.getText().toString().trim();
        String email = personalEmail.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();

        if (name.isEmpty() || id.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Save alumni details in Firestore
                        Map<String, Object> alumni = new HashMap<>();
                        alumni.put("name", name);
                        alumni.put("studentID", id);
                        alumni.put("email", email);

                        db.collection("alumni").document(uid).set(alumni)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                    // Navigate to main activity after successful signup
                                    Intent intent = new Intent(signup.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(signup.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(signup.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

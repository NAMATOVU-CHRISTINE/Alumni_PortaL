package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // FIX: Check if the user is already signed in.
        // This check is done before setContentView to prevent the login screen from flashing.
        if (mAuth.getCurrentUser() != null) {
            // If user is already signed in, skip the login screen and go to HomeActivity
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish(); // This is important to prevent the user from coming back to the login screen by pressing the back button.
            return; // We return here so the rest of the onCreate method is not executed.
        }

        // If no user is signed in, then show the login layout.
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        TextView signupPrompt = findViewById(R.id.signupText);
        TextView forgotPasswordText = findViewById(R.id.forgotPassword);

        loginButton.setOnClickListener(v -> loginUser());

        signupPrompt.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });

        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        // Find the user's email by their username in Firestore
        db.collection("users")
                .whereEqualTo("username", username) // Correctly query the 'username' field
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Username found, get the email
                            String email = querySnapshot.getDocuments().get(0).getString("email");
                            if (email != null) {
                                // Now, sign in with the retrieved email and entered password
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this, authTask -> {
                                            if (authTask.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Authentication failed. Incorrect password.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(MainActivity.this, "User data is incomplete. Cannot find email.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Username not found
                            Toast.makeText(MainActivity.this, "Authentication failed. User not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Error querying Firestore
                        Toast.makeText(MainActivity.this, "Error finding user. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

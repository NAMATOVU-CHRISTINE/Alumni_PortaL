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
import com.google.firebase.firestore.QuerySnapshot;
import com.namatovu.alumniportal.utils.CloudinaryHelper;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Cloudinary
        CloudinaryHelper.initialize(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // FIX: Check if the user is already signed in.
        // This check is done before setContentView to prevent the login screen from flashing.
        if (mAuth.getCurrentUser() != null) {
            // Start data sync service when user is logged in
            com.namatovu.alumniportal.utils.SyncHelper.startSync(this);
            
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
        Button googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView signupPrompt = findViewById(R.id.signupText);
        TextView forgotPasswordText = findViewById(R.id.forgotPassword);

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

        loginButton.setOnClickListener(v -> loginUser());

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        signupPrompt.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });

        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
        });
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
                                        // New user - save to Firestore with default type as "student"
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("fullName", mAuth.getCurrentUser().getDisplayName());
                                        user.put("email", mAuth.getCurrentUser().getEmail());
                                        user.put("userId", userId);
                                        user.put("username", mAuth.getCurrentUser().getEmail().split("@")[0]);
                                        user.put("userType", "student"); // Default: Current students
                                        user.put("isAlumni", false);

                                        db.collection("users").document(userId).set(user);
                                    }
                                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
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

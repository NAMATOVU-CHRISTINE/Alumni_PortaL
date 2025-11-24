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

        // Set click listeners
        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        binding.signupPrompt.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        binding.forgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void signInWithGoogle() {
        // Show loading indicator
        binding.loginButton.setText("");
        binding.loginProgressBar.setVisibility(android.view.View.VISIBLE);
        binding.loginButton.setEnabled(false);
        binding.googleSignInButton.setEnabled(false);
        
        // Sign out first to show account picker
        mAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                Log.d(TAG, "Google account obtained: " + account.getEmail());
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                hideLoadingIndicator();
                Log.e(TAG, "Google account is null or missing ID token");
                Toast.makeText(this, "Google sign in failed: Invalid account", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            hideLoadingIndicator();
            Log.e(TAG, "Google sign in error code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
            String errorMsg = "Google sign in failed";
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
                        // Check if user exists in Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        String displayName = mAuth.getCurrentUser().getDisplayName();
                        String email = mAuth.getCurrentUser().getEmail();
                        
                        if (email == null) {
                            hideLoadingIndicator();
                            Toast.makeText(LoginActivity.this, "Could not get email from Google account", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        // New user - redirect to complete profile
                                        hideLoadingIndicator();
                                        Intent intent = new Intent(LoginActivity.this, CompleteGoogleSignupActivity.class);
                                        intent.putExtra("googleEmail", email);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Existing user - check if email is verified
                                        Boolean emailVerified = documentSnapshot.getBoolean("emailVerified");
                                        if (emailVerified != null && emailVerified) {
                                            // Email verified, proceed to home
                                            Log.d(TAG, "Existing user logging in via Google");
                                            // Update FCM token for existing user
                                            com.namatovu.alumniportal.utils.NotificationHelper.updateTokenInFirestore(
                                                com.namatovu.alumniportal.utils.NotificationHelper.getFCMToken()
                                            );
                                            navigateToHome();
                                        } else {
                                            // Email not verified yet - check if user actually verified it
                                            mAuth.getCurrentUser().reload().addOnCompleteListener(reloadTask -> {
                                                if (reloadTask.isSuccessful() && mAuth.getCurrentUser().isEmailVerified()) {
                                                    // User verified email! Update Firestore and proceed
                                                    Log.d(TAG, "Email was verified, updating Firestore");
                                                    Map<String, Object> updateData = new HashMap<>();
                                                    updateData.put("emailVerified", true);
                                                    db.collection("users").document(userId)
                                                            .update(updateData)
                                                            .addOnSuccessListener(aVoid -> {
                                                                com.namatovu.alumniportal.utils.NotificationHelper.updateTokenInFirestore(
                                                                    com.namatovu.alumniportal.utils.NotificationHelper.getFCMToken()
                                                                );
                                                                navigateToHome();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e(TAG, "Error updating email verification", e);
                                                                navigateToHome();
                                                            });
                                                } else {
                                                    // Email still not verified
                                                    hideLoadingIndicator();
                                                    Toast.makeText(LoginActivity.this, "Please verify your email first. Check your inbox for the verification link.", Toast.LENGTH_LONG).show();
                                                    mAuth.signOut();
                                                }
                                            });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking user existence", e);
                                    hideLoadingIndicator();
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        hideLoadingIndicator();
                        Log.e(TAG, "Firebase auth failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Refresh the user to get latest email verification status
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        // Email is verified, proceed to home
                        Log.d(TAG, "User email verified, navigating to home");
                        navigateToHome();
                    } else {
                        // Email not verified yet, show message and sign out
                        Log.d(TAG, "User is signed in but email not verified");
                        Toast.makeText(LoginActivity.this, "Please verify your email to continue. Check your inbox for the verification link.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                }
            });
        }
        
        // Check if coming from signup with pending verification
        if (getIntent().getBooleanExtra("email_verification_pending", false)) {
            String userEmail = getIntent().getStringExtra("user_email");
            Toast.makeText(this, "Verification email sent to " + userEmail + ". Please check your inbox.", Toast.LENGTH_LONG).show();
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
                        if (user != null) {
                            // Reload user to get latest email verification status from Firebase
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (reloadTask.isSuccessful()) {
                                    // Force refresh the email verification status
                                    if (user.isEmailVerified()) {
                                        // Email is verified, update Firestore and proceed to home
                                        Log.d(TAG, "Login successful. Email verified.");
                                        
                                        // Update Firestore to mark email as verified
                                        Map<String, Object> updateData = new HashMap<>();
                                        updateData.put("emailVerified", true);
                                        db.collection("users").document(user.getUid())
                                                .update(updateData)
                                                .addOnSuccessListener(aVoid -> navigateToHome())
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error updating email verification status", e);
                                                    // Still proceed to home even if update fails
                                                    navigateToHome();
                                                });
                                    } else {
                                        hideLoadingIndicator();
                                        Toast.makeText(LoginActivity.this, "Please verify your email before logging in. Check your inbox for the verification link.", Toast.LENGTH_LONG).show();
                                        // Sign out the user since they haven't verified their email
                                        mAuth.signOut();
                                        Log.d(TAG, "User attempted login without email verification");
                                    }
                                } else {
                                    hideLoadingIndicator();
                                    Toast.makeText(LoginActivity.this, "Error checking email verification status.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            hideLoadingIndicator();
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
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
        
        // Request notification permission for Android 13+
        com.namatovu.alumniportal.utils.NotificationPermissionHelper.requestNotificationPermission(this);
        
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

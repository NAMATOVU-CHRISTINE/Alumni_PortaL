package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.namatovu.alumniportal.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    
    private ActivitySettingsBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        setupToolbar();
        setupClickListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // Profile & Account Section
        binding.editProfileOption.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        binding.changePasswordOption.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // App Preferences Section
        binding.themeOption.setOnClickListener(v -> {
            showThemeSelectionDialog();
        });

        // Account Actions Section
        binding.logoutOption.setOnClickListener(v -> {
            logoutUser();
        });

        binding.deleteAccountOption.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showChangePasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Change Password");
        
        // Create input fields
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        final android.widget.EditText currentPasswordInput = new android.widget.EditText(this);
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);
        
        final android.widget.EditText newPasswordInput = new android.widget.EditText(this);
        newPasswordInput.setHint("New Password");
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);
        
        final android.widget.EditText confirmPasswordInput = new android.widget.EditText(this);
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            
            changePassword(currentPassword, newPassword);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null) {
                // Re-authenticate user with current password
                com.google.firebase.auth.AuthCredential credential = 
                    com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword);
                
                mAuth.getCurrentUser().reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update password
                            mAuth.getCurrentUser().updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(this, "Failed to change password: " + 
                                            updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                        } else {
                            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_LONG).show();
                        }
                    });
            }
        }
    }

    private void showThemeSelectionDialog() {
        String[] themes = {"Light", "Dark"};
        int currentTheme = 0; // Default to Light
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Theme");
        builder.setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
            String selectedTheme = themes[which];
            binding.themeSummary.setText(selectedTheme);
            Toast.makeText(this, "Theme changed to " + selectedTheme, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to permanently delete your account? This action cannot be undone and all your data will be lost.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        
        builder.setPositiveButton("DELETE", (dialog, which) -> {
            showDeleteConfirmationDialog();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Final Confirmation");
        builder.setMessage("Type 'DELETE' to confirm account deletion:");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Type DELETE here");
        builder.setView(input);
        
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String confirmation = input.getText().toString().trim();
            if ("DELETE".equals(confirmation)) {
                deleteUserAccount();
            } else {
                Toast.makeText(this, "Confirmation text doesn't match", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteUserAccount() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                        // Navigate to login
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete account: " + 
                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        }
    }
}
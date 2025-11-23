package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.namatovu.alumniportal.databinding.ActivitySettingsBinding;
import com.namatovu.alumniportal.utils.ThemeManager;

public class SettingsActivity extends AppCompatActivity {
    
    private ActivitySettingsBinding binding;
    private FirebaseAuth mAuth;
    private ThemeManager themeManager;
    private android.content.SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        themeManager = ThemeManager.getInstance(this);
        prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        setupToolbar();
        setupClickListeners();
        updateThemeSummary();
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

        // Email notifications switch
        binding.emailNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("email_notifications", isChecked).apply();
            
            if (isChecked) {
                // Save user's email preference to Firestore
                if (mAuth.getCurrentUser() != null) {
                    String userId = mAuth.getCurrentUser().getUid();
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("emailNotificationsEnabled", true)
                        .addOnSuccessListener(aVoid -> 
                            Toast.makeText(this, "Email notifications enabled", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> 
                            Toast.makeText(this, "Failed to update preference", Toast.LENGTH_SHORT).show());
                }
            } else {
                // Disable email notifications
                if (mAuth.getCurrentUser() != null) {
                    String userId = mAuth.getCurrentUser().getUid();
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("emailNotificationsEnabled", false)
                        .addOnSuccessListener(aVoid -> 
                            Toast.makeText(this, "Email notifications disabled", Toast.LENGTH_SHORT).show());
                }
            }
        });

        // App Preferences Section
        binding.themeOption.setOnClickListener(v -> {
            showThemeSelectionDialog();
        });

        // Notification switches
        setupNotificationSwitches();

        // Account Actions Section
        binding.logoutOption.setOnClickListener(v -> {
            logoutUser();
        });

        binding.deleteAccountOption.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
    }

    private void setupNotificationSwitches() {
        // Load saved preferences
        binding.allNotificationsSwitch.setChecked(prefs.getBoolean("all_notifications", true));
        binding.emailNotificationsSwitch.setChecked(prefs.getBoolean("email_notifications", false));
        binding.mentorshipRequestsSwitch.setChecked(prefs.getBoolean("mentorship_notifications", true));
        binding.eventUpdatesSwitch.setChecked(prefs.getBoolean("event_notifications", true));
        binding.announcementsSwitch.setChecked(prefs.getBoolean("announcement_notifications", true));

        // All notifications master switch
        binding.allNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("all_notifications", isChecked).apply();
            
            if (isChecked) {
                // Enable other switches and restore their saved states
                binding.emailNotificationsSwitch.setEnabled(true);
                binding.mentorshipRequestsSwitch.setEnabled(true);
                binding.eventUpdatesSwitch.setEnabled(true);
                binding.announcementsSwitch.setEnabled(true);
                
                // Restore saved states
                binding.emailNotificationsSwitch.setChecked(prefs.getBoolean("email_notifications", false));
                binding.mentorshipRequestsSwitch.setChecked(prefs.getBoolean("mentorship_notifications", true));
                binding.eventUpdatesSwitch.setChecked(prefs.getBoolean("event_notifications", true));
                binding.announcementsSwitch.setChecked(prefs.getBoolean("announcement_notifications", true));
                
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Turn OFF all switches when master is OFF
                binding.emailNotificationsSwitch.setChecked(false);
                binding.mentorshipRequestsSwitch.setChecked(false);
                binding.eventUpdatesSwitch.setChecked(false);
                binding.announcementsSwitch.setChecked(false);
                
                // Disable other switches
                binding.emailNotificationsSwitch.setEnabled(false);
                binding.mentorshipRequestsSwitch.setEnabled(false);
                binding.eventUpdatesSwitch.setEnabled(false);
                binding.announcementsSwitch.setEnabled(false);
                
                Toast.makeText(this, "All notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Individual notification switches
        binding.mentorshipRequestsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("mentorship_notifications", isChecked).apply();
            Toast.makeText(this, "Mentorship notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        binding.eventUpdatesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("event_notifications", isChecked).apply();
            Toast.makeText(this, "Event notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        binding.announcementsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("announcement_notifications", isChecked).apply();
            Toast.makeText(this, "Announcement notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Set initial state of dependent switches
        boolean allEnabled = binding.allNotificationsSwitch.isChecked();
        binding.emailNotificationsSwitch.setEnabled(allEnabled);
        binding.mentorshipRequestsSwitch.setEnabled(allEnabled);
        binding.eventUpdatesSwitch.setEnabled(allEnabled);
        binding.announcementsSwitch.setEnabled(allEnabled);
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
        int currentTheme = themeManager.getTheme();
        
        // If current theme is System Default (2), default to Light (0)
        if (currentTheme == 2) {
            currentTheme = 0;
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Theme");
        builder.setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
            int oldTheme = themeManager.getTheme();
            
            // Only apply if theme actually changed
            if (oldTheme != which) {
                // Apply the selected theme (0 = Light, 1 = Dark)
                themeManager.setTheme(which);
                
                Toast.makeText(this, "Theme changed to " + themes[which], Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                
                // Recreate all activities to apply theme
                recreate();
            } else {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void updateThemeSummary() {
        if (binding.themeSummary != null) {
            binding.themeSummary.setText(themeManager.getThemeName());
        }
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
    
    private void sendTestNotification() {
        // Check if notification permission is granted
        if (!com.namatovu.alumniportal.utils.NotificationPermissionHelper.hasNotificationPermission(this)) {
            // Request permission
            com.namatovu.alumniportal.utils.NotificationPermissionHelper.requestNotificationPermission(this);
            Toast.makeText(this, "Please grant notification permission first", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Create and show test notification
        android.app.NotificationManager notificationManager = 
            (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        String channelId = "alumni_portal_notifications";
        
        // Create notification channel for Android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                channelId,
                "Alumni Portal",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for Alumni Portal app");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
        
        // Create notification
        androidx.core.app.NotificationCompat.Builder builder = 
            new androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Test Notification")
                .setContentText("Notifications are working! 🎉")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        
        notificationManager.notify(1, builder.build());
        Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show();
    }
}
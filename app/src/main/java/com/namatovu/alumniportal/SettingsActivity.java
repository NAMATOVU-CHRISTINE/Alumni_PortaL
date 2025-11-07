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
            Toast.makeText(this, "Change Password - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.emailPreferencesOption.setOnClickListener(v -> {
            Toast.makeText(this, "Email Preferences - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // App Preferences Section
        binding.themeOption.setOnClickListener(v -> {
            Toast.makeText(this, "Theme Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.languageOption.setOnClickListener(v -> {
            Toast.makeText(this, "Language Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.fontSizeOption.setOnClickListener(v -> {
            Toast.makeText(this, "Font Size Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Privacy & Security Section
        binding.accountVisibilityOption.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.namatovu.alumniportal.activities.PrivacySettingsActivity.class);
            startActivity(intent);
        });

        binding.blockedUsersOption.setOnClickListener(v -> {
            Toast.makeText(this, "Blocked Users - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.twoFactorOption.setOnClickListener(v -> {
            Toast.makeText(this, "Two-Factor Authentication - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // App Information Section
        binding.aboutOption.setOnClickListener(v -> {
            Toast.makeText(this, "About Alumni Portal v1.0", Toast.LENGTH_LONG).show();
        });

        binding.helpSupportOption.setOnClickListener(v -> {
            Toast.makeText(this, "Help & Support - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.rateAppOption.setOnClickListener(v -> {
            Toast.makeText(this, "Rate the App - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Account Actions Section
        binding.logoutOption.setOnClickListener(v -> {
            logoutUser();
        });

        binding.deleteAccountOption.setOnClickListener(v -> {
            Toast.makeText(this, "Delete Account - Please contact support", Toast.LENGTH_LONG).show();
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
}
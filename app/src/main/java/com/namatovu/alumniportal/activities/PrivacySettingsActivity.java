package com.namatovu.alumniportal.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.namatovu.alumniportal.adapters.PrivacySettingsAdapter;
import com.namatovu.alumniportal.databinding.ActivityPrivacySettingsBinding;
import com.namatovu.alumniportal.utils.PrivacyManager;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity for managing user privacy settings
 */
public class PrivacySettingsActivity extends AppCompatActivity {
    private static final String TAG = "PrivacySettingsActivity";
    
    private ActivityPrivacySettingsBinding binding;
    private PrivacyManager privacyManager;
    private PrivacySettingsAdapter adapter;
    private List<PrivacySettingItem> privacyItems;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize privacy manager
        privacyManager = PrivacyManager.getInstance(this);
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Privacy Settings");
        }
        
        // Initialize privacy settings
        setupPrivacySettings();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup buttons
        setupButtons();
        
        // Log analytics
        AnalyticsHelper.logNavigation("PrivacySettingsActivity", "SettingsActivity");
    }
    
    private void setupPrivacySettings() {
        privacyItems = new ArrayList<>();
        
        // Profile Visibility Section
        privacyItems.add(new PrivacySettingItem(
            "Profile Visibility", "", PrivacySettingItem.Type.HEADER
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Full Name",
            "Allow other alumni to see your full name",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_FULL_NAME
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Email Address",
            "Allow verified alumni to see your email",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_EMAIL
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Phone Number",
            "Allow connections to see your phone number",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_PHONE
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Current Location",
            "Display your current city/region",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_LOCATION
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Profile Picture",
            "Display your profile photo to other users",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_PROFILE_PICTURE
        ));
        
        // Professional Information Section
        privacyItems.add(new PrivacySettingItem(
            "Professional Information", "", PrivacySettingItem.Type.HEADER
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Current Job",
            "Display your current job title",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_CURRENT_JOB
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Company",
            "Display your current company",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_COMPANY
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Skills",
            "Display your professional skills",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_SKILLS
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Social Links",
            "Display links to your social profiles",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_SOCIAL_LINKS
        ));
        
        // Academic Information Section
        privacyItems.add(new PrivacySettingItem(
            "Academic Information", "", PrivacySettingItem.Type.HEADER
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Graduation Year",
            "Display your graduation year",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_GRADUATION_YEAR
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Major",
            "Display your field of study",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_MAJOR
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Bio",
            "Display your personal bio/description",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.SHOW_BIO
        ));
        
        // Communication Preferences Section
        privacyItems.add(new PrivacySettingItem(
            "Communication Preferences", "", PrivacySettingItem.Type.HEADER
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Allow Direct Messages",
            "Let other alumni send you messages",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_DIRECT_MESSAGES
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Allow Mentor Requests",
            "Receive mentorship requests from students",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_MENTOR_REQUESTS
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Allow Job Opportunities",
            "Receive job-related messages",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_JOB_OPPORTUNITIES
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Allow Event Invites",
            "Receive invitations to alumni events",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_EVENT_INVITES
        ));
        
        // Discovery & Search Section
        privacyItems.add(new PrivacySettingItem(
            "Discovery & Search", "", PrivacySettingItem.Type.HEADER
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Include in Alumni Search",
            "Allow others to find you in search results",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_ALUMNI_SEARCH
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Allow Location Sharing",
            "Share location for nearby alumni features",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_LOCATION_SHARING
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Activity Status",
            "Display when you're online/active",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_ACTIVITY_STATUS
        ));
        
        // Advanced Privacy Section
        privacyItems.add(new PrivacySettingItem(
            "Advanced Privacy", "", PrivacySettingItem.Type.HEADER
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Show Read Receipts",
            "Let others know when you've read their messages",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_READ_RECEIPTS
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Analytics Tracking",
            "Help improve the app with usage analytics",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_ANALYTICS_TRACKING
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Marketing Emails",
            "Receive promotional emails and newsletters",
            PrivacySettingItem.Type.SWITCH,
            PrivacyManager.ALLOW_MARKETING_EMAILS
        ));
        
        // Data Rights Section
        privacyItems.add(new PrivacySettingItem(
            "Data Rights", "", PrivacySettingItem.Type.HEADER
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Export My Data",
            "Download a copy of your data",
            PrivacySettingItem.Type.BUTTON,
            "export_data"
        ));
        
        privacyItems.add(new PrivacySettingItem(
            "Delete My Account",
            "Permanently delete your account and data",
            PrivacySettingItem.Type.BUTTON,
            "delete_account"
        ));
    }
    
    private void setupRecyclerView() {
        adapter = new PrivacySettingsAdapter(privacyItems, new PrivacySettingsAdapter.OnPrivacySettingListener() {
            @Override
            public void onSwitchToggled(String settingKey, boolean isEnabled) {
                privacyManager.updatePrivacySetting(settingKey, isEnabled);
                
                // Show feedback
                String message = isEnabled ? "Setting enabled" : "Setting disabled";
                Toast.makeText(PrivacySettingsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onButtonClicked(String action) {
                handleButtonAction(action);
            }
        });
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }
    
    private void setupButtons() {
        binding.resetToDefaultsButton.setOnClickListener(v -> {
            // Reset all settings to defaults
            privacyManager.initializeDefaultSettings();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Privacy settings reset to defaults", Toast.LENGTH_SHORT).show();
        });
        
        binding.saveButton.setOnClickListener(v -> {
            // Save current settings
            Toast.makeText(this, "Privacy settings saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    
    private void handleButtonAction(String action) {
        switch (action) {
            case "export_data":
                exportUserData();
                break;
            case "delete_account":
                showDeleteAccountDialog();
                break;
        }
    }
    
    private void exportUserData() {
        privacyManager.requestDataExport(getCurrentUserId(), new PrivacyManager.DataExportCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(PrivacySettingsActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(PrivacySettingsActivity.this, "Export failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void showDeleteAccountDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                deleteUserAccount();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteUserAccount() {
        privacyManager.requestDataDeletion(getCurrentUserId(), new PrivacyManager.DataDeletionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(PrivacySettingsActivity.this, message, Toast.LENGTH_LONG).show();
                    // Logout and close app
                    finishAffinity();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(PrivacySettingsActivity.this, "Deletion failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private String getCurrentUserId() {
        // Implementation to get current user ID
        return "current_user_id"; // Placeholder
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    /**
     * Privacy setting item model
     */
    public static class PrivacySettingItem {
        public enum Type {
            HEADER,
            SWITCH,
            BUTTON
        }
        
        public String title;
        public String description;
        public Type type;
        public String key;
        public boolean isEnabled;
        
        public PrivacySettingItem(String title, String description, Type type) {
            this.title = title;
            this.description = description;
            this.type = type;
        }
        
        public PrivacySettingItem(String title, String description, Type type, String key) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.key = key;
        }
    }
}
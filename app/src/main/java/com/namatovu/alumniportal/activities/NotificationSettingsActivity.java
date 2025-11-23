package com.namatovu.alumniportal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import com.namatovu.alumniportal.utils.NotificationHelper;

public class NotificationSettingsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationSettings";
    
    private SwitchCompat switchAllNotifications;
    private SwitchCompat switchMessages;
    private SwitchCompat switchMentorship;
    private SwitchCompat switchEvents;
    private SwitchCompat switchJobs;
    private SwitchCompat switchNews;
    private MaterialCardView cardSystemSettings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        
        initViews();
        setupToolbar();
        loadCurrentSettings();
        setupListeners();
        
        // Track screen view
        AnalyticsHelper.logScreenView(this, "notification_settings");
    }
    
    private void initViews() {
        switchAllNotifications = findViewById(R.id.switchAllNotifications);
        switchMessages = findViewById(R.id.switchMessages);
        switchMentorship = findViewById(R.id.switchMentorship);
        switchEvents = findViewById(R.id.switchEvents);
        switchJobs = findViewById(R.id.switchJobs);
        switchNews = findViewById(R.id.switchNews);
        cardSystemSettings = findViewById(R.id.cardSystemSettings);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notification Settings");
        }
    }
    
    private void loadCurrentSettings() {
        // Load current notification preferences
        switchAllNotifications.setChecked(NotificationHelper.areNotificationsEnabled());
        switchMessages.setChecked(NotificationHelper.areMessageNotificationsEnabled());
        switchMentorship.setChecked(NotificationHelper.areMentorshipNotificationsEnabled());
        switchEvents.setChecked(NotificationHelper.areEventNotificationsEnabled());
        switchJobs.setChecked(NotificationHelper.areJobNotificationsEnabled());
        switchNews.setChecked(NotificationHelper.areNewsNotificationsEnabled());
        
        // Update switch states based on master switch
        updateDependentSwitches();
        
        // Check system notification settings
        updateSystemSettingsCard();
    }
    
    private void setupListeners() {
        switchAllNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setNotificationsEnabled(isChecked);
            
            // Temporarily remove listeners to avoid recursive calls
            switchMessages.setOnCheckedChangeListener(null);
            switchMentorship.setOnCheckedChangeListener(null);
            switchEvents.setOnCheckedChangeListener(null);
            switchJobs.setOnCheckedChangeListener(null);
            switchNews.setOnCheckedChangeListener(null);
            
            // Update all individual switches based on master switch
            if (isChecked) {
                // Master is ON - enable all individual notifications
                NotificationHelper.setMessageNotificationsEnabled(true);
                NotificationHelper.setMentorshipNotificationsEnabled(true);
                NotificationHelper.setEventNotificationsEnabled(true);
                NotificationHelper.setJobNotificationsEnabled(true);
                NotificationHelper.setNewsNotificationsEnabled(true);
                
                // Update UI switches - set them to checked
                switchMessages.setChecked(true);
                switchMentorship.setChecked(true);
                switchEvents.setChecked(true);
                switchJobs.setChecked(true);
                switchNews.setChecked(true);
                
                // Enable the switches
                switchMessages.setEnabled(true);
                switchMentorship.setEnabled(true);
                switchEvents.setEnabled(true);
                switchJobs.setEnabled(true);
                switchNews.setEnabled(true);
                
                Toast.makeText(this, "All notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Master is OFF - disable all individual notifications
                NotificationHelper.setMessageNotificationsEnabled(false);
                NotificationHelper.setMentorshipNotificationsEnabled(false);
                NotificationHelper.setEventNotificationsEnabled(false);
                NotificationHelper.setJobNotificationsEnabled(false);
                NotificationHelper.setNewsNotificationsEnabled(false);
                
                // Update UI switches - set them to unchecked
                switchMessages.setChecked(false);
                switchMentorship.setChecked(false);
                switchEvents.setChecked(false);
                switchJobs.setChecked(false);
                switchNews.setChecked(false);
                
                // Disable the switches
                switchMessages.setEnabled(false);
                switchMentorship.setEnabled(false);
                switchEvents.setEnabled(false);
                switchJobs.setEnabled(false);
                switchNews.setEnabled(false);
                
                Toast.makeText(this, "All notifications disabled", Toast.LENGTH_SHORT).show();
            }
            
            // Re-attach listeners
            attachSwitchListeners();
            
            updateDependentSwitches();
        });
        
        attachSwitchListeners();
        
        cardSystemSettings.setOnClickListener(v -> {
            // Open system notification settings
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
            
            AnalyticsHelper.logEvent("system_notification_settings_opened", null, null);
        });
    }
    
    private void attachSwitchListeners() {
        switchMessages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setMessageNotificationsEnabled(isChecked);
        });
        
        switchMentorship.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setMentorshipNotificationsEnabled(isChecked);
        });
        
        switchEvents.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setEventNotificationsEnabled(isChecked);
        });
        
        switchJobs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setJobNotificationsEnabled(isChecked);
        });
        
        switchNews.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setNewsNotificationsEnabled(isChecked);
        });
        
        switchAllNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setNotificationsEnabled(isChecked);
            updateDependentSwitches();
        });
    }
    
    private void updateDependentSwitches() {
        boolean masterEnabled = switchAllNotifications.isChecked();
        
        // When master is ON, hide individual switches
        // When master is OFF, show individual switches
        int visibility = masterEnabled ? View.GONE : View.VISIBLE;
        
        switchMessages.setVisibility(visibility);
        switchMentorship.setVisibility(visibility);
        switchEvents.setVisibility(visibility);
        switchJobs.setVisibility(visibility);
        switchNews.setVisibility(visibility);
        
        // Also hide the parent containers
        if (visibility == View.GONE) {
            // Hide all individual switch containers
            switchMessages.getParent().getParent().getParent().getParent().getParent();
        }
    }
    
    private void updateSystemSettingsCard() {
        // Check if system notifications are enabled
        boolean systemEnabled = NotificationHelper.areSystemNotificationsEnabled(this);
        
        if (!systemEnabled) {
            cardSystemSettings.setCardBackgroundColor(getColor(R.color.warning_background));
            Toast.makeText(this, "System notifications are disabled. Tap to enable.", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateSystemSettingsCard();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
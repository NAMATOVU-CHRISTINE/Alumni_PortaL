package com.namatovu.alumniportal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
            updateDependentSwitches();
            
            if (isChecked) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "All notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });
        
        switchMessages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchAllNotifications.isChecked()) {
                NotificationHelper.setMessageNotificationsEnabled(isChecked);
            }
        });
        
        switchMentorship.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchAllNotifications.isChecked()) {
                NotificationHelper.setMentorshipNotificationsEnabled(isChecked);
            }
        });
        
        switchEvents.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchAllNotifications.isChecked()) {
                NotificationHelper.setEventNotificationsEnabled(isChecked);
            }
        });
        
        switchJobs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchAllNotifications.isChecked()) {
                NotificationHelper.setJobNotificationsEnabled(isChecked);
            }
        });
        
        switchNews.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (switchAllNotifications.isChecked()) {
                NotificationHelper.setNewsNotificationsEnabled(isChecked);
            }
        });
        
        cardSystemSettings.setOnClickListener(v -> {
            // Open system notification settings
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
            
            AnalyticsHelper.logEvent("system_notification_settings_opened", null, null);
        });
    }
    
    private void updateDependentSwitches() {
        boolean masterEnabled = switchAllNotifications.isChecked();
        
        switchMessages.setEnabled(masterEnabled);
        switchMentorship.setEnabled(masterEnabled);
        switchEvents.setEnabled(masterEnabled);
        switchJobs.setEnabled(masterEnabled);
        switchNews.setEnabled(masterEnabled);
        
        // Reset switches to current settings if master is re-enabled
        if (masterEnabled) {
            switchMessages.setChecked(NotificationHelper.areMessageNotificationsEnabled());
            switchMentorship.setChecked(NotificationHelper.areMentorshipNotificationsEnabled());
            switchEvents.setChecked(NotificationHelper.areEventNotificationsEnabled());
            switchJobs.setChecked(NotificationHelper.areJobNotificationsEnabled());
            switchNews.setChecked(NotificationHelper.areNewsNotificationsEnabled());
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
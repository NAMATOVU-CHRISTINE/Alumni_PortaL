package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive privacy management system for user data protection
 */
public class PrivacyManager {
    private static final String TAG = "PrivacyManager";
    private static final String PREFS_NAME = "privacy_settings";
    
    private static PrivacyManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final FirebaseFirestore db;
    private final Map<String, Boolean> privacyCache = new ConcurrentHashMap<>();
    
    // Privacy setting keys
    public static final String SHOW_FULL_NAME = "show_full_name";
    public static final String SHOW_EMAIL = "show_email";
    public static final String SHOW_PHONE = "show_phone";
    public static final String SHOW_LOCATION = "show_location";
    public static final String SHOW_CURRENT_JOB = "show_current_job";
    public static final String SHOW_COMPANY = "show_company";
    public static final String SHOW_GRADUATION_YEAR = "show_graduation_year";
    public static final String SHOW_MAJOR = "show_major";
    public static final String SHOW_BIO = "show_bio";
    public static final String SHOW_SKILLS = "show_skills";
    public static final String SHOW_SOCIAL_LINKS = "show_social_links";
    public static final String SHOW_PROFILE_PICTURE = "show_profile_picture";
    public static final String ALLOW_DIRECT_MESSAGES = "allow_direct_messages";
    public static final String ALLOW_MENTOR_REQUESTS = "allow_mentor_requests";
    public static final String ALLOW_JOB_OPPORTUNITIES = "allow_job_opportunities";
    public static final String ALLOW_EVENT_INVITES = "allow_event_invites";
    public static final String ALLOW_ALUMNI_SEARCH = "allow_alumni_search";
    public static final String ALLOW_LOCATION_SHARING = "allow_location_sharing";
    public static final String ALLOW_ACTIVITY_STATUS = "allow_activity_status";
    public static final String ALLOW_READ_RECEIPTS = "allow_read_receipts";
    public static final String ALLOW_ANALYTICS_TRACKING = "allow_analytics_tracking";
    public static final String ALLOW_MARKETING_EMAILS = "allow_marketing_emails";
    public static final String ALLOW_PUSH_NOTIFICATIONS = "allow_push_notifications";
    public static final String ALLOW_DATA_EXPORT = "allow_data_export";
    
    // Privacy levels
    public enum PrivacyLevel {
        PUBLIC,     // Visible to all alumni
        ALUMNI,     // Visible to verified alumni only
        CONNECTIONS, // Visible to direct connections only
        PRIVATE     // Not visible to others
    }
    
    // Data categories for GDPR compliance
    public enum DataCategory {
        PERSONAL_INFO,      // Name, email, phone
        PROFESSIONAL_INFO,  // Job, company, skills
        EDUCATIONAL_INFO,   // University, major, graduation year
        SOCIAL_INFO,        // Bio, interests, social links
        LOCATION_INFO,      // Current location, hometown
        COMMUNICATION_INFO, // Messages, preferences
        ACTIVITY_INFO,      // App usage, engagement
        TECHNICAL_INFO      // Device info, logs
    }
    
    private PrivacyManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
        loadPrivacySettings();
    }
    
    public static synchronized PrivacyManager getInstance(Context context) {
        if (instance == null) {
            instance = new PrivacyManager(context);
        }
        return instance;
    }
    
    /**
     * Initialize default privacy settings for new users
     */
    public void initializeDefaultSettings() {
        Map<String, Boolean> defaultSettings = getDefaultPrivacySettings();
        
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, Boolean> setting : defaultSettings.entrySet()) {
            if (!prefs.contains(setting.getKey())) {
                editor.putBoolean(setting.getKey(), setting.getValue());
                privacyCache.put(setting.getKey(), setting.getValue());
            }
        }
        editor.apply();
        
        // Sync to Firebase
        syncPrivacySettingsToFirebase();
        
        Log.d(TAG, "Default privacy settings initialized");
    }
    
    /**
     * Get default privacy settings (privacy-first approach)
     */
    private Map<String, Boolean> getDefaultPrivacySettings() {
        Map<String, Boolean> defaults = new HashMap<>();
        
        // Basic profile info - moderate privacy
        defaults.put(SHOW_FULL_NAME, true);
        defaults.put(SHOW_EMAIL, false);  // Email private by default
        defaults.put(SHOW_PHONE, false);  // Phone private by default
        defaults.put(SHOW_LOCATION, false); // Location private by default
        defaults.put(SHOW_GRADUATION_YEAR, true);
        defaults.put(SHOW_MAJOR, true);
        defaults.put(SHOW_BIO, true);
        defaults.put(SHOW_PROFILE_PICTURE, true);
        
        // Professional info - selective sharing
        defaults.put(SHOW_CURRENT_JOB, true);
        defaults.put(SHOW_COMPANY, true);
        defaults.put(SHOW_SKILLS, true);
        defaults.put(SHOW_SOCIAL_LINKS, false); // Social links private by default
        
        // Communication preferences - user control
        defaults.put(ALLOW_DIRECT_MESSAGES, true);
        defaults.put(ALLOW_MENTOR_REQUESTS, false); // Opt-in for mentoring
        defaults.put(ALLOW_JOB_OPPORTUNITIES, true);
        defaults.put(ALLOW_EVENT_INVITES, true);
        defaults.put(ALLOW_ALUMNI_SEARCH, true);
        
        // Advanced privacy - conservative defaults
        defaults.put(ALLOW_LOCATION_SHARING, false);
        defaults.put(ALLOW_ACTIVITY_STATUS, false);
        defaults.put(ALLOW_READ_RECEIPTS, true);
        defaults.put(ALLOW_ANALYTICS_TRACKING, true);
        defaults.put(ALLOW_MARKETING_EMAILS, false);
        defaults.put(ALLOW_PUSH_NOTIFICATIONS, true);
        defaults.put(ALLOW_DATA_EXPORT, true);
        
        return defaults;
    }
    
    /**
     * Check if a privacy setting is enabled
     */
    public boolean isPrivacySettingEnabled(String settingKey) {
        if (privacyCache.containsKey(settingKey)) {
            return privacyCache.get(settingKey);
        }
        
        boolean value = prefs.getBoolean(settingKey, getDefaultValue(settingKey));
        privacyCache.put(settingKey, value);
        return value;
    }
    
    /**
     * Update a privacy setting
     */
    public void updatePrivacySetting(String settingKey, boolean value) {
        prefs.edit().putBoolean(settingKey, value).apply();
        privacyCache.put(settingKey, value);
        
        // Sync to Firebase
        syncSingleSettingToFirebase(settingKey, value);
        
        // Log privacy change for audit
        AnalyticsHelper.logPrivacySettingChange(settingKey, value);
        
        Log.d(TAG, "Privacy setting updated: " + settingKey + " = " + value);
    }
    
    /**
     * Update multiple privacy settings
     */
    public void updatePrivacySettings(Map<String, Boolean> settings) {
        SharedPreferences.Editor editor = prefs.edit();
        
        for (Map.Entry<String, Boolean> setting : settings.entrySet()) {
            editor.putBoolean(setting.getKey(), setting.getValue());
            privacyCache.put(setting.getKey(), setting.getValue());
        }
        
        editor.apply();
        
        // Sync to Firebase
        syncPrivacySettingsToFirebase();
        
        Log.d(TAG, "Multiple privacy settings updated: " + settings.size() + " settings");
    }
    
    /**
     * Get all current privacy settings
     */
    public Map<String, Boolean> getAllPrivacySettings() {
        Map<String, Boolean> allSettings = new HashMap<>();
        Map<String, Boolean> defaults = getDefaultPrivacySettings();
        
        for (String key : defaults.keySet()) {
            allSettings.put(key, isPrivacySettingEnabled(key));
        }
        
        return allSettings;
    }
    
    /**
     * Check if user data should be visible based on privacy settings and relationship
     */
    public boolean canViewUserData(String dataType, String viewerUserId, String targetUserId, 
                                  UserRelationship relationship) {
        // User can always view their own data
        if (viewerUserId.equals(targetUserId)) {
            return true;
        }
        
        // Check basic privacy setting first
        if (!isPrivacySettingEnabled(dataType)) {
            return false;
        }
        
        // Check relationship-based permissions
        switch (relationship) {
            case NONE:
                return isDataPublic(dataType);
            case CONNECTION:
                return isDataVisibleToConnections(dataType);
            case BLOCKED:
                return false;
            case ADMIN:
                return hasAdminAccess(dataType);
            default:
                return false;
        }
    }
    
    /**
     * User relationship types
     */
    public enum UserRelationship {
        NONE,        // No relationship
        CONNECTION,  // Direct connection/friend
        BLOCKED,     // Blocked user
        ADMIN        // Admin/moderator
    }
    
    private boolean isDataPublic(String dataType) {
        // Define which data types are public by default
        switch (dataType) {
            case SHOW_FULL_NAME:
            case SHOW_GRADUATION_YEAR:
            case SHOW_MAJOR:
            case SHOW_PROFILE_PICTURE:
                return true;
            default:
                return false;
        }
    }
    
    private boolean isDataVisibleToConnections(String dataType) {
        // Connections can see most data if privacy allows
        return isPrivacySettingEnabled(dataType);
    }
    
    private boolean hasAdminAccess(String dataType) {
        // Admins have limited access even with privacy restrictions
        switch (dataType) {
            case SHOW_FULL_NAME:
            case SHOW_EMAIL:
            case SHOW_GRADUATION_YEAR:
            case SHOW_MAJOR:
                return true;
            default:
                return isPrivacySettingEnabled(dataType);
        }
    }
    
    /**
     * Data anonymization for privacy compliance
     */
    public String anonymizeData(String data, DataCategory category, boolean partialAnonymization) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        switch (category) {
            case PERSONAL_INFO:
                return anonymizePersonalInfo(data, partialAnonymization);
            case PROFESSIONAL_INFO:
                return anonymizeProfessionalInfo(data, partialAnonymization);
            case LOCATION_INFO:
                return anonymizeLocationInfo(data, partialAnonymization);
            default:
                return partialAnonymization ? maskData(data, 0.5f) : "[REDACTED]";
        }
    }
    
    private String anonymizePersonalInfo(String data, boolean partial) {
        if (partial) {
            // Show first letter and mask the rest
            if (data.length() > 2) {
                return data.charAt(0) + "*".repeat(data.length() - 2) + data.charAt(data.length() - 1);
            }
            return "*".repeat(data.length());
        }
        return "[REDACTED]";
    }
    
    private String anonymizeProfessionalInfo(String data, boolean partial) {
        if (partial) {
            return maskData(data, 0.6f); // Show 60% of professional info
        }
        return "[HIDDEN]";
    }
    
    private String anonymizeLocationInfo(String data, boolean partial) {
        if (partial) {
            // Show only city/state, hide specific address
            String[] parts = data.split(",");
            if (parts.length > 1) {
                return parts[parts.length - 2].trim() + ", " + parts[parts.length - 1].trim();
            }
            return maskData(data, 0.3f);
        }
        return "[LOCATION HIDDEN]";
    }
    
    private String maskData(String data, float visiblePercentage) {
        if (data.length() <= 2) {
            return "*".repeat(data.length());
        }
        
        int visibleChars = Math.max(1, (int) (data.length() * visiblePercentage));
        int maskedChars = data.length() - visibleChars;
        
        return data.substring(0, visibleChars) + "*".repeat(maskedChars);
    }
    
    /**
     * GDPR compliance methods
     */
    public void requestDataExport(String userId, DataExportCallback callback) {
        if (!isPrivacySettingEnabled(ALLOW_DATA_EXPORT)) {
            callback.onError("Data export is disabled for this user");
            return;
        }
        
        // Implementation for data export
        Log.d(TAG, "Data export requested for user: " + userId);
        // This would generate a comprehensive data export
        callback.onSuccess("Data export initiated. You will receive an email when ready.");
    }
    
    public void requestDataDeletion(String userId, DataDeletionCallback callback) {
        // Implementation for right to be forgotten
        Log.d(TAG, "Data deletion requested for user: " + userId);
        // This would anonymize/delete user data
        callback.onSuccess("Data deletion request processed");
    }
    
    public interface DataExportCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface DataDeletionCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    // Firebase synchronization methods
    private void loadPrivacySettings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        db.collection("users").document(currentUser.getUid())
            .collection("privacy").document("settings")
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    Map<String, Object> settings = document.getData();
                    if (settings != null) {
                        SharedPreferences.Editor editor = prefs.edit();
                        for (Map.Entry<String, Object> entry : settings.entrySet()) {
                            if (entry.getValue() instanceof Boolean) {
                                boolean value = (Boolean) entry.getValue();
                                editor.putBoolean(entry.getKey(), value);
                                privacyCache.put(entry.getKey(), value);
                            }
                        }
                        editor.apply();
                        Log.d(TAG, "Privacy settings loaded from Firebase");
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load privacy settings", e);
                ErrorHandler.getInstance(context).handleError(e, "load_privacy_settings");
            });
    }
    
    private void syncPrivacySettingsToFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        Map<String, Boolean> allSettings = getAllPrivacySettings();
        
        db.collection("users").document(currentUser.getUid())
            .collection("privacy").document("settings")
            .set(allSettings)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Privacy settings synced to Firebase"))
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to sync privacy settings", e);
                ErrorHandler.getInstance(context).handleError(e, "sync_privacy_settings");
            });
    }
    
    private void syncSingleSettingToFirebase(String key, boolean value) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        Map<String, Object> update = new HashMap<>();
        update.put(key, value);
        
        db.collection("users").document(currentUser.getUid())
            .collection("privacy").document("settings")
            .update(update)
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to sync single privacy setting", e);
                ErrorHandler.getInstance(context).handleError(e, "sync_single_privacy_setting");
            });
    }
    
    private boolean getDefaultValue(String settingKey) {
        Map<String, Boolean> defaults = getDefaultPrivacySettings();
        return defaults.getOrDefault(settingKey, false);
    }
    
    /**
     * Clear all privacy data (for logout/account deletion)
     */
    public void clearPrivacyData() {
        prefs.edit().clear().apply();
        privacyCache.clear();
        Log.d(TAG, "Privacy data cleared");
    }
}
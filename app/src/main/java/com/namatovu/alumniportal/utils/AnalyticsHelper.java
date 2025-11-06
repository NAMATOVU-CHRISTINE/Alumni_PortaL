package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Helper class for Firebase Analytics tracking
 * Provides standardized event logging for the Alumni Portal app
 */
public class AnalyticsHelper {
    private static final String TAG = "AnalyticsHelper";
    
    private static FirebaseAnalytics mFirebaseAnalytics;
    
    /**
     * Initialize Firebase Analytics
     * Call this in Application onCreate or MainActivity onCreate
     */
    public static void initialize(Context context) {
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            Log.d(TAG, "Firebase Analytics initialized");
        }
    }
    
    /**
     * Track user login events
     */
    public static void logLogin(String method) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.METHOD, method);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            Log.d(TAG, "Login event logged: " + method);
        }
    }
    
    /**
     * Track user signup events
     */
    public static void logSignUp(String method) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.METHOD, method);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            Log.d(TAG, "Sign up event logged: " + method);
        }
    }
    
    /**
     * Track profile edit events
     */
    public static void logProfileEdit(String editType) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("edit_type", editType);
            mFirebaseAnalytics.logEvent("profile_edit", bundle);
            Log.d(TAG, "Profile edit event logged: " + editType);
        }
    }
    
    /**
     * Track navigation events
     */
    public static void logNavigation(String screenName, String fromScreen) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
            bundle.putString("from_screen", fromScreen);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Log.d(TAG, "Navigation event logged: " + fromScreen + " -> " + screenName);
        }
    }
    
    /**
     * Track alumni search events
     */
    public static void logAlumniSearch(String searchQuery, int resultCount) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchQuery);
            bundle.putInt("result_count", resultCount);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
            Log.d(TAG, "Alumni search event logged: " + searchQuery + " (" + resultCount + " results)");
        }
    }
    
    /**
     * Track job posting interactions
     */
    public static void logJobPostingView(String jobTitle, String company) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("job_title", jobTitle);
            bundle.putString("company", company);
            mFirebaseAnalytics.logEvent("job_posting_view", bundle);
            Log.d(TAG, "Job posting view event logged: " + jobTitle + " at " + company);
        }
    }
    
    /**
     * Track mentor connection events
     */
    public static void logMentorConnection(String action, String mentorId) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("action", action); // "request", "accept", "reject"
            bundle.putString("mentor_id", mentorId);
            mFirebaseAnalytics.logEvent("mentor_connection", bundle);
            Log.d(TAG, "Mentor connection event logged: " + action + " for " + mentorId);
        }
    }
    
    /**
     * Track permission request events
     */
    public static void logPermissionRequest(String permission, boolean granted) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("permission", permission);
            bundle.putBoolean("granted", granted);
            mFirebaseAnalytics.logEvent("permission_request", bundle);
            Log.d(TAG, "Permission request event logged: " + permission + " = " + granted);
        }
    }
    
    /**
     * Track error events for debugging
     */
    public static void logError(String errorType, String errorMessage, String screen) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("error_type", errorType);
            bundle.putString("error_message", errorMessage);
            bundle.putString("screen", screen);
            mFirebaseAnalytics.logEvent("app_error", bundle);
            Log.d(TAG, "Error event logged: " + errorType + " on " + screen);
        }
    }
    
    /**
     * Set user properties for analytics
     */
    public static void setUserProperties(String graduationYear, String major, String userType) {
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserProperty("graduation_year", graduationYear);
            mFirebaseAnalytics.setUserProperty("major", major);
            mFirebaseAnalytics.setUserProperty("user_type", userType); // "student", "alumni", "faculty"
            Log.d(TAG, "User properties set: " + graduationYear + ", " + major + ", " + userType);
        }
    }
    
    /**
     * Set user ID for analytics
     */
    public static void setUserId(String userId) {
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserId(userId);
            Log.d(TAG, "User ID set: " + userId);
        }
    }
    
    /**
     * Log performance issues
     */
    public static void logPerformanceIssue(String operation, long duration) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("operation", operation);
            bundle.putLong("duration_ms", duration);
            mFirebaseAnalytics.logEvent("performance_issue", bundle);
            Log.d(TAG, "Performance issue logged: " + operation + " (" + duration + "ms)");
        }
    }
    
    /**
     * Log crash data for analysis
     */
    public static void logCrash(java.util.Map<String, String> crashData) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            for (java.util.Map.Entry<String, String> entry : crashData.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
            mFirebaseAnalytics.logEvent("app_crash", bundle);
            Log.d(TAG, "Crash logged with data: " + crashData.size() + " fields");
        }
    }
    
    /**
     * Log privacy setting changes
     */
    public static void logPrivacySettingChange(String settingKey, boolean value) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("setting_key", settingKey);
            bundle.putBoolean("setting_value", value);
            mFirebaseAnalytics.logEvent("privacy_setting_changed", bundle);
            Log.d(TAG, "Privacy setting change logged: " + settingKey + " = " + value);
        }
    }
    
    /**
     * Log search events
     */
    public static void logSearch(String query, String searchType) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("search_term", query);
            bundle.putString("search_type", searchType);
            mFirebaseAnalytics.logEvent("search", bundle);
            Log.d(TAG, "Search logged: " + query + " (" + searchType + ")");
        }
    }
    
    /**
     * Generic event logging method
     */
    public static void logEvent(String eventName, Bundle params) {
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(eventName, params);
            Log.d(TAG, "Custom event logged: " + eventName);
        }
    }
    
    /**
     * Simple event logging with string parameter
     */
    public static void logEvent(String eventName, String paramName, String paramValue) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(paramName, paramValue);
            mFirebaseAnalytics.logEvent(eventName, bundle);
            Log.d(TAG, "Event logged: " + eventName + " (" + paramName + "=" + paramValue + ")");
        }
    }
    
    /**
     * Log file operations
     */
    public static void logFileUpload(String fileType, long fileSize, String shareScope) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("file_type", fileType);
            bundle.putLong("file_size", fileSize);
            bundle.putString("share_scope", shareScope);
            mFirebaseAnalytics.logEvent("file_upload", bundle);
            Log.d(TAG, "File upload logged: " + fileType + " (" + fileSize + " bytes)");
        }
    }
    
    public static void logFileDownload(String fileType, long fileSize) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("file_type", fileType);
            bundle.putLong("file_size", fileSize);
            mFirebaseAnalytics.logEvent("file_download", bundle);
            Log.d(TAG, "File download logged: " + fileType);
        }
    }
    
    /**
     * Log notification events
     */
    public static void logNotificationShown(String notificationType, String priority) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("notification_type", notificationType);
            bundle.putString("priority", priority);
            mFirebaseAnalytics.logEvent("notification_shown", bundle);
            Log.d(TAG, "Notification shown logged: " + notificationType);
        }
    }
    
    public static void logTopicNotification(String topic, String notificationType) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("topic", topic);
            bundle.putString("notification_type", notificationType);
            mFirebaseAnalytics.logEvent("topic_notification", bundle);
            Log.d(TAG, "Topic notification logged: " + topic);
        }
    }
}
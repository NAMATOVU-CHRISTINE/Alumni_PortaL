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
}
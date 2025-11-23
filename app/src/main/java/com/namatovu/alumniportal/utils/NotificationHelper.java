package com.namatovu.alumniportal.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for managing notifications and FCM tokens
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_MESSAGES_ENABLED = "messages_enabled";
    private static final String KEY_MENTORSHIP_ENABLED = "mentorship_enabled";
    private static final String KEY_EVENTS_ENABLED = "events_enabled";
    private static final String KEY_JOBS_ENABLED = "jobs_enabled";
    private static final String KEY_NEWS_ENABLED = "news_enabled";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    
    private static SharedPreferences prefs;
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    
    /**
     * Initialize notification helper
     */
    public static void initialize(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Set default notification preferences
        setDefaultPreferences();
        
        // Initialize FCM token
        initializeFCMToken();
    }
    
    /**
     * Set default notification preferences on first launch
     */
    private static void setDefaultPreferences() {
        if (!prefs.contains(KEY_NOTIFICATIONS_ENABLED)) {
            prefs.edit()
                    .putBoolean(KEY_NOTIFICATIONS_ENABLED, true)
                    .putBoolean(KEY_MESSAGES_ENABLED, true)
                    .putBoolean(KEY_MENTORSHIP_ENABLED, true)
                    .putBoolean(KEY_EVENTS_ENABLED, true)
                    .putBoolean(KEY_JOBS_ENABLED, true)
                    .putBoolean(KEY_NEWS_ENABLED, false) // News notifications off by default
                    .apply();
        }
    }
    
    /**
     * Initialize and store FCM token
     */
    private static void initializeFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    
                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);
                    
                    // Store token locally
                    prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
                    
                    // Update token in Firestore
                    updateTokenInFirestore(token);
                });
    }
    
    /**
     * Update FCM token in Firestore for the current user
     */
    public static void updateTokenInFirestore(String token) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "No authenticated user, cannot update FCM token");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("lastTokenUpdate", System.currentTimeMillis());
        
        db.collection("users").document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token updated in Firestore");
                    AnalyticsHelper.logEvent("fcm_token_updated", null, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update FCM token in Firestore", e);
                });
    }
    
    /**
     * Subscribe to topic for general notifications
     */
    public static void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed to " + topic;
                    if (!task.isSuccessful()) {
                        msg = "Failed to subscribe to " + topic;
                        Log.w(TAG, msg, task.getException());
                    } else {
                        Log.d(TAG, msg);
                        AnalyticsHelper.logEvent("notification_topic_subscribed", "topic", topic);
                    }
                });
    }
    
    /**
     * Unsubscribe from topic
     */
    public static void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(task -> {
                    String msg = "Unsubscribed from " + topic;
                    if (!task.isSuccessful()) {
                        msg = "Failed to unsubscribe from " + topic;
                        Log.w(TAG, msg, task.getException());
                    } else {
                        Log.d(TAG, msg);
                        AnalyticsHelper.logEvent("notification_topic_unsubscribed", "topic", topic);
                    }
                });
    }
    
    /**
     * Enable/disable all notifications
     */
    public static void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
        
        // Subscribe/unsubscribe from general topics
        if (enabled) {
            subscribeToTopic("general_announcements");
            subscribeToTopic("app_updates");
        } else {
            unsubscribeFromTopic("general_announcements");
            unsubscribeFromTopic("app_updates");
        }
        
        AnalyticsHelper.logEvent("notifications_toggled", "enabled", String.valueOf(enabled));
    }
    
    /**
     * Enable/disable message notifications
     */
    public static void setMessageNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MESSAGES_ENABLED, enabled).apply();
        updateNotificationPreferencesInFirestore("messagesEnabled", enabled);
        AnalyticsHelper.logEvent("message_notifications_toggled", "enabled", String.valueOf(enabled));
    }
    
    /**
     * Enable/disable mentorship notifications
     */
    public static void setMentorshipNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MENTORSHIP_ENABLED, enabled).apply();
        updateNotificationPreferencesInFirestore("mentorshipEnabled", enabled);
        AnalyticsHelper.logEvent("mentorship_notifications_toggled", "enabled", String.valueOf(enabled));
    }
    
    /**
     * Enable/disable event notifications
     */
    public static void setEventNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EVENTS_ENABLED, enabled).apply();
        updateNotificationPreferencesInFirestore("eventsEnabled", enabled);
        
        if (enabled) {
            subscribeToTopic("events");
        } else {
            unsubscribeFromTopic("events");
        }
        
        AnalyticsHelper.logEvent("event_notifications_toggled", "enabled", String.valueOf(enabled));
    }
    
    /**
     * Enable/disable job notifications
     */
    public static void setJobNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_JOBS_ENABLED, enabled).apply();
        updateNotificationPreferencesInFirestore("jobsEnabled", enabled);
        
        if (enabled) {
            subscribeToTopic("jobs");
        } else {
            unsubscribeFromTopic("jobs");
        }
        
        AnalyticsHelper.logEvent("job_notifications_toggled", "enabled", String.valueOf(enabled));
    }
    
    /**
     * Enable/disable news notifications
     */
    public static void setNewsNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NEWS_ENABLED, enabled).apply();
        
        if (enabled) {
            subscribeToTopic("news");
        } else {
            unsubscribeFromTopic("news");
        }
        
        AnalyticsHelper.logEvent("news_notifications_toggled", "enabled", String.valueOf(enabled));
    }
    
    // Getters for notification preferences
    public static boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }
    
    public static boolean areMessageNotificationsEnabled() {
        // If master is OFF, all are OFF. If master is ON, check individual setting
        if (!areNotificationsEnabled()) return false;
        return prefs.getBoolean(KEY_MESSAGES_ENABLED, true);
    }
    
    public static boolean areMentorshipNotificationsEnabled() {
        // If master is OFF, all are OFF. If master is ON, check individual setting
        if (!areNotificationsEnabled()) return false;
        return prefs.getBoolean(KEY_MENTORSHIP_ENABLED, true);
    }
    
    public static boolean areEventNotificationsEnabled() {
        // If master is OFF, all are OFF. If master is ON, check individual setting
        if (!areNotificationsEnabled()) return false;
        return prefs.getBoolean(KEY_EVENTS_ENABLED, true);
    }
    
    public static boolean areJobNotificationsEnabled() {
        // If master is OFF, all are OFF. If master is ON, check individual setting
        if (!areNotificationsEnabled()) return false;
        return prefs.getBoolean(KEY_JOBS_ENABLED, true);
    }
    
    public static boolean areNewsNotificationsEnabled() {
        // If master is OFF, all are OFF. If master is ON, check individual setting
        if (!areNotificationsEnabled()) return false;
        return prefs.getBoolean(KEY_NEWS_ENABLED, false);
    }
    
    /**
     * Get stored FCM token
     */
    public static String getFCMToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }
    
    /**
     * Clear notification data (for logout)
     */
    public static void clearNotificationData() {
        prefs.edit().clear().apply();
        
        // Unsubscribe from all topics
        unsubscribeFromTopic("general_announcements");
        unsubscribeFromTopic("app_updates");
        unsubscribeFromTopic("events");
        unsubscribeFromTopic("jobs");
        unsubscribeFromTopic("news");
    }
    
    /**
     * Check if notifications are enabled system-wide
     */
    public static boolean areSystemNotificationsEnabled(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            return notificationManager.areNotificationsEnabled();
        }
        return false;
    }
    
    /**
     * Send chat message notification to specific user
     */
    public static void sendChatNotification(String recipientUserId, String senderName, 
                                          String messageText, String chatId) {
        if (!areMessageNotificationsEnabled()) {
            return;
        }
        
        // This would typically be done on the server side
        // For demo purposes, we'll just log it
        Log.d(TAG, "Would send chat notification to " + recipientUserId + 
                   " from " + senderName + ": " + messageText);
        
        // In a real app, you would call your backend API here
        // to send the notification through FCM
    }
    
    /**
     * Send mentorship notification
     */
    public static void sendMentorshipNotification(String recipientUserId, String fromUserName, 
                                                String action, String requestId) {
        if (!areMentorshipNotificationsEnabled()) {
            return;
        }
        
        Log.d(TAG, "Would send mentorship notification to " + recipientUserId + 
                   " from " + fromUserName + " action: " + action);
    }
    
    /**
     * Send event notification
     */
    public static void sendEventNotification(String eventTitle, String action, String eventId) {
        if (!areEventNotificationsEnabled()) {
            return;
        }
        
        Log.d(TAG, "Would send event notification: " + eventTitle + " action: " + action);
    }
    
    /**
     * Send job notification
     */
    public static void sendJobNotification(String jobTitle, String company, String jobId) {
        if (!areJobNotificationsEnabled()) {
            return;
        }
        
        Log.d(TAG, "Would send job notification: " + jobTitle + " at " + company);
    }
    
    /**
     * Send event update notification to all subscribers
     */
    public static void sendEventUpdateNotification(String eventTitle, String eventId, String action, Context context) {
        if (!areEventNotificationsEnabled()) {
            return;
        }
        
        if (db == null) db = FirebaseFirestore.getInstance();
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "event_update");
        notification.put("eventId", eventId);
        notification.put("eventTitle", eventTitle);
        notification.put("action", action);
        notification.put("title", "Event Update: " + eventTitle);
        notification.put("message", "New update on " + eventTitle);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);
        
        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener(doc -> {
                Log.d(TAG, "Event update notification sent");
                AnalyticsHelper.logEvent("event_notification_sent", "eventId", eventId);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to send event notification", e));
        
        // Also queue email notification
        queueEventUpdateEmail(eventTitle, eventId, action, context);
    }
    
    /**
     * Send news/article update notification
     */
    public static void sendNewsNotification(String newsTitle, String newsId, String authorName, Context context) {
        if (!areNewsNotificationsEnabled()) {
            return;
        }
        
        if (db == null) db = FirebaseFirestore.getInstance();
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "news_update");
        notification.put("newsId", newsId);
        notification.put("newsTitle", newsTitle);
        notification.put("authorName", authorName);
        notification.put("title", "New Article: " + newsTitle);
        notification.put("message", "New article by " + authorName);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);
        
        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener(doc -> {
                Log.d(TAG, "News notification sent");
                AnalyticsHelper.logEvent("news_notification_sent", "newsId", newsId);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to send news notification", e));
        
        // Also queue email notification
        queueNewsUpdateEmail(newsTitle, newsId, authorName, context);
    }
    
    /**
     * Queue event update email for backend to send
     */
    private static void queueEventUpdateEmail(String eventTitle, String eventId, String action, Context context) {
        if (db == null) db = FirebaseFirestore.getInstance();
        
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("subject", "Event Update: " + eventTitle);
        emailData.put("body", "There's a new update on the event: " + eventTitle + "\n\n" +
                "Action: " + action + "\n\n" +
                "Check the Alumni Portal app for more details.");
        emailData.put("type", "event_update");
        emailData.put("eventId", eventId);
        emailData.put("timestamp", System.currentTimeMillis());
        emailData.put("status", "pending");
        emailData.put("sendToAll", true);
        
        db.collection("emailQueue")
            .add(emailData)
            .addOnSuccessListener(doc -> Log.d(TAG, "Event update email queued"))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to queue event email", e));
    }
    
    /**
     * Queue news/article update email for backend to send
     */
    private static void queueNewsUpdateEmail(String newsTitle, String newsId, String authorName, Context context) {
        if (db == null) db = FirebaseFirestore.getInstance();
        
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("subject", "New Article: " + newsTitle);
        emailData.put("body", "A new article has been published!\n\n" +
                "Title: " + newsTitle + "\n" +
                "Author: " + authorName + "\n\n" +
                "Read the full article in the Alumni Portal app.");
        emailData.put("type", "news_update");
        emailData.put("newsId", newsId);
        emailData.put("authorName", authorName);
        emailData.put("timestamp", System.currentTimeMillis());
        emailData.put("status", "pending");
        emailData.put("sendToAll", true);
        
        db.collection("emailQueue")
            .add(emailData)
            .addOnSuccessListener(doc -> Log.d(TAG, "News update email queued"))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to queue news email", e));
    }
}
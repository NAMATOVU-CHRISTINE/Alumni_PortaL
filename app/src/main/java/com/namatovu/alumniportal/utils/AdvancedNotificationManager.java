package com.namatovu.alumniportal.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.namatovu.alumniportal.HomeActivity;
import com.namatovu.alumniportal.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced push notification system with FCM, notification channels,
 * custom alerts, notification history, and advanced targeting
 */
public class AdvancedNotificationManager {
    private static final String TAG = "AdvancedNotificationMgr";
    private static final String PREFS_NAME = "notification_preferences";
    private static final String HISTORY_COLLECTION = "notificationHistory";
    
    // Notification channels
    public static final String CHANNEL_MESSAGES = "messages";
    public static final String CHANNEL_EVENTS = "events";
    public static final String CHANNEL_JOBS = "jobs";
    public static final String CHANNEL_MENTORSHIP = "mentorship";
    public static final String CHANNEL_NEWS = "news";
    public static final String CHANNEL_SYSTEM = "system";
    public static final String CHANNEL_PROMOTIONS = "promotions";
    
    // Notification types
    public enum NotificationType {
        MESSAGE,
        EVENT_INVITE,
        EVENT_REMINDER,
        JOB_POSTING,
        JOB_APPLICATION,
        MENTOR_REQUEST,
        MENTOR_ACCEPTED,
        NEWS_ARTICLE,
        SYSTEM_UPDATE,
        PROMOTION,
        CONNECTION_REQUEST,
        PROFILE_VIEW,
        ACHIEVEMENT,
        BIRTHDAY,
        ANNIVERSARY
    }
    
    // Notification priority levels
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
    
    private static AdvancedNotificationManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final FirebaseFirestore db;
    private final NotificationManagerCompat notificationManager;
    
    private AdvancedNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
        this.notificationManager = NotificationManagerCompat.from(context);
        
        createNotificationChannels();
        initializeFirebaseMessaging();
    }
    
    public static synchronized AdvancedNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdvancedNotificationManager(context);
        }
        return instance;
    }
    
    /**
     * Custom notification data
     */
    public static class CustomNotification {
        public String id;
        public String title;
        public String body;
        public String imageUrl;
        public NotificationType type;
        public Priority priority = Priority.NORMAL;
        public String channelId;
        public Map<String, String> data = new HashMap<>();
        public String targetUserId;
        public List<String> targetUserIds = new ArrayList<>();
        public String targetTopic;
        public boolean saveToHistory = true;
        public Date scheduledTime;
        public String actionUrl;
        public List<NotificationAction> actions = new ArrayList<>();
        
        public CustomNotification() {}
        
        public CustomNotification(String title, String body, NotificationType type) {
            this.title = title;
            this.body = body;
            this.type = type;
            this.channelId = getChannelForType(type);
        }
        
        private String getChannelForType(NotificationType type) {
            switch (type) {
                case MESSAGE:
                    return CHANNEL_MESSAGES;
                case EVENT_INVITE:
                case EVENT_REMINDER:
                    return CHANNEL_EVENTS;
                case JOB_POSTING:
                case JOB_APPLICATION:
                    return CHANNEL_JOBS;
                case MENTOR_REQUEST:
                case MENTOR_ACCEPTED:
                    return CHANNEL_MENTORSHIP;
                case NEWS_ARTICLE:
                    return CHANNEL_NEWS;
                case PROMOTION:
                    return CHANNEL_PROMOTIONS;
                default:
                    return CHANNEL_SYSTEM;
            }
        }
    }
    
    /**
     * Notification action buttons
     */
    public static class NotificationAction {
        public String id;
        public String title;
        public String action;
        public boolean autoCancel = true;
        
        public NotificationAction(String id, String title, String action) {
            this.id = id;
            this.title = title;
            this.action = action;
        }
    }
    
    /**
     * Notification history item
     */
    public static class NotificationHistoryItem {
        public String id;
        public String title;
        public String body;
        public String imageUrl;
        public NotificationType type;
        public Date receivedAt;
        public boolean isRead = false;
        public String userId;
        public Map<String, String> data = new HashMap<>();
        
        public NotificationHistoryItem() {}
        
        public NotificationHistoryItem(CustomNotification notification, String userId) {
            this.id = notification.id != null ? notification.id : String.valueOf(System.currentTimeMillis());
            this.title = notification.title;
            this.body = notification.body;
            this.imageUrl = notification.imageUrl;
            this.type = notification.type;
            this.receivedAt = new Date();
            this.userId = userId;
            this.data = notification.data;
        }
    }
    
    /**
     * Show local notification
     */
    public void showNotification(@NonNull CustomNotification notification) {
        try {
            // Check if notifications are enabled for this type
            if (!isNotificationTypeEnabled(notification.type)) {
                Log.d(TAG, "Notifications disabled for type: " + notification.type);
                return;
            }
            
            // Create notification builder
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notification.channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setPriority(getPriorityValue(notification.priority))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
            
            // Set large icon
            if (notification.imageUrl != null) {
                loadImageForNotification(notification.imageUrl, bitmap -> {
                    if (bitmap != null) {
                        builder.setLargeIcon(bitmap);
                        builder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .setBigContentTitle(notification.title));
                    }
                    displayNotification(builder, notification);
                });
            } else {
                displayNotification(builder, notification);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
            ErrorHandler.getInstance(context).handleError(e, "show_notification");
        }
    }
    
    private void displayNotification(NotificationCompat.Builder builder, CustomNotification notification) {
        // Set content intent
        Intent intent = createIntentForNotification(notification);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        
        // Add action buttons
        for (NotificationAction action : notification.actions) {
            Intent actionIntent = new Intent(action.action);
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(
                context, action.id.hashCode(), actionIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(R.drawable.ic_action, action.title, actionPendingIntent);
        }
        
        // Show notification
        int notificationId = notification.id != null ? notification.id.hashCode() : 
            (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
        
        // Save to history if enabled
        if (notification.saveToHistory) {
            saveNotificationToHistory(notification);
        }
        
        // Log analytics
        AnalyticsHelper.logNotificationShown(notification.type.toString(), notification.priority.toString());
        
        Log.d(TAG, "Notification displayed: " + notification.title);
    }
    
    /**
     * Send push notification to specific user
     */
    public void sendPushNotification(@NonNull CustomNotification notification, 
                                   @NonNull String targetUserId) {
        
        // Get user's FCM token
        db.collection("users").document(targetUserId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String fcmToken = document.getString("fcmToken");
                    if (fcmToken != null) {
                        sendToToken(notification, fcmToken);
                    } else {
                        Log.w(TAG, "No FCM token found for user: " + targetUserId);
                    }
                } else {
                    Log.w(TAG, "User not found: " + targetUserId);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user FCM token", e);
                ErrorHandler.getInstance(context).handleError(e, "get_fcm_token");
            });
    }
    
    /**
     * Send push notification to multiple users
     */
    public void sendPushNotificationToUsers(@NonNull CustomNotification notification,
                                          @NonNull List<String> targetUserIds) {
        
        // Batch get FCM tokens
        for (String userId : targetUserIds) {
            sendPushNotification(notification, userId);
        }
    }
    
    /**
     * Send push notification to topic
     */
    public void sendPushNotificationToTopic(@NonNull CustomNotification notification,
                                          @NonNull String topic) {
        
        // This would typically be done server-side using Firebase Admin SDK
        // For client-side, we can only subscribe to topics
        Log.d(TAG, "Topic notification would be sent server-side: " + topic);
        
        // Log for analytics
        AnalyticsHelper.logTopicNotification(topic, notification.type.toString());
    }
    
    /**
     * Subscribe to notification topic
     */
    public void subscribeToTopic(@NonNull String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Subscribed to topic: " + topic);
                    prefs.edit().putBoolean("topic_" + topic, true).apply();
                } else {
                    Log.e(TAG, "Failed to subscribe to topic: " + topic, task.getException());
                }
            });
    }
    
    /**
     * Unsubscribe from notification topic
     */
    public void unsubscribeFromTopic(@NonNull String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Unsubscribed from topic: " + topic);
                    prefs.edit().remove("topic_" + topic).apply();
                } else {
                    Log.e(TAG, "Failed to unsubscribe from topic: " + topic, task.getException());
                }
            });
    }
    
    /**
     * Handle incoming FCM message
     */
    public void handleRemoteMessage(@NonNull RemoteMessage remoteMessage) {
        try {
            // Extract notification data
            CustomNotification notification = new CustomNotification();
            
            // Get notification payload
            RemoteMessage.Notification remoteNotification = remoteMessage.getNotification();
            if (remoteNotification != null) {
                notification.title = remoteNotification.getTitle();
                notification.body = remoteNotification.getBody();
                notification.imageUrl = remoteNotification.getImageUrl() != null ? 
                    remoteNotification.getImageUrl().toString() : null;
            }
            
            // Get data payload
            Map<String, String> data = remoteMessage.getData();
            notification.data = data;
            
            // Parse notification type
            String typeString = data.get("type");
            if (typeString != null) {
                try {
                    notification.type = NotificationType.valueOf(typeString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    notification.type = NotificationType.SYSTEM_UPDATE;
                }
            } else {
                notification.type = NotificationType.SYSTEM_UPDATE;
            }
            
            // Parse priority
            String priorityString = data.get("priority");
            if (priorityString != null) {
                try {
                    notification.priority = Priority.valueOf(priorityString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    notification.priority = Priority.NORMAL;
                }
            }
            
            // Set channel
            notification.channelId = notification.getChannelForType(notification.type);
            
            // Show notification
            showNotification(notification);
            
            Log.d(TAG, "Handled remote message: " + notification.title);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling remote message", e);
            ErrorHandler.getInstance(context).handleError(e, "handle_remote_message");
        }
    }
    
    /**
     * Get notification history
     */
    public interface NotificationHistoryCallback {
        void onSuccess(List<NotificationHistoryItem> notifications);
        void onError(String error);
    }
    
    public void getNotificationHistory(@NonNull NotificationHistoryCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        db.collection(HISTORY_COLLECTION)
            .whereEqualTo("userId", currentUser.getUid())
            .orderBy("receivedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<NotificationHistoryItem> notifications = new ArrayList<>();
                
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    try {
                        NotificationHistoryItem item = document.toObject(NotificationHistoryItem.class);
                        if (item != null) {
                            notifications.add(item);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing notification history item", e);
                    }
                }
                
                callback.onSuccess(notifications);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting notification history", e);
                callback.onError("Failed to load notification history: " + e.getMessage());
            });
    }
    
    /**
     * Mark notification as read
     */
    public void markNotificationAsRead(@NonNull String notificationId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        db.collection(HISTORY_COLLECTION)
            .whereEqualTo("id", notificationId)
            .whereEqualTo("userId", currentUser.getUid())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    document.getReference().update("isRead", true);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error marking notification as read", e);
            });
    }
    
    /**
     * Clear notification history
     */
    public void clearNotificationHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        db.collection(HISTORY_COLLECTION)
            .whereEqualTo("userId", currentUser.getUid())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    document.getReference().delete();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error clearing notification history", e);
            });
    }
    
    // Notification preferences
    public void setNotificationTypeEnabled(@NonNull NotificationType type, boolean enabled) {
        prefs.edit().putBoolean("notif_" + type.toString(), enabled).apply();
        
        // Update topic subscriptions
        updateTopicSubscriptions();
    }
    
    public boolean isNotificationTypeEnabled(@NonNull NotificationType type) {
        return prefs.getBoolean("notif_" + type.toString(), true); // Default enabled
    }
    
    public void setQuietHours(int startHour, int endHour) {
        prefs.edit()
            .putInt("quiet_start_hour", startHour)
            .putInt("quiet_end_hour", endHour)
            .putBoolean("quiet_hours_enabled", true)
            .apply();
    }
    
    public boolean isQuietHoursEnabled() {
        return prefs.getBoolean("quiet_hours_enabled", false);
    }
    
    // Private helper methods
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Messages channel
            NotificationChannel messagesChannel = new NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            );
            messagesChannel.setDescription("Direct messages and chat notifications");
            manager.createNotificationChannel(messagesChannel);
            
            // Events channel
            NotificationChannel eventsChannel = new NotificationChannel(
                CHANNEL_EVENTS,
                "Events",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            eventsChannel.setDescription("Event invitations and reminders");
            manager.createNotificationChannel(eventsChannel);
            
            // Jobs channel
            NotificationChannel jobsChannel = new NotificationChannel(
                CHANNEL_JOBS,
                "Job Opportunities",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            jobsChannel.setDescription("Job postings and application updates");
            manager.createNotificationChannel(jobsChannel);
            
            // Mentorship channel
            NotificationChannel mentorshipChannel = new NotificationChannel(
                CHANNEL_MENTORSHIP,
                "Mentorship",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            mentorshipChannel.setDescription("Mentorship requests and updates");
            manager.createNotificationChannel(mentorshipChannel);
            
            // News channel
            NotificationChannel newsChannel = new NotificationChannel(
                CHANNEL_NEWS,
                "News & Updates",
                NotificationManager.IMPORTANCE_LOW
            );
            newsChannel.setDescription("University news and announcements");
            manager.createNotificationChannel(newsChannel);
            
            // System channel
            NotificationChannel systemChannel = new NotificationChannel(
                CHANNEL_SYSTEM,
                "System",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            systemChannel.setDescription("App updates and system notifications");
            manager.createNotificationChannel(systemChannel);
            
            // Promotions channel
            NotificationChannel promotionsChannel = new NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promotions",
                NotificationManager.IMPORTANCE_LOW
            );
            promotionsChannel.setDescription("Promotional content and offers");
            manager.createNotificationChannel(promotionsChannel);
        }
    }
    
    private void initializeFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                
                // Get new FCM registration token
                String token = task.getResult();
                Log.d(TAG, "FCM Registration Token: " + token);
                
                // Save token to user profile
                saveFCMTokenToProfile(token);
                
                // Subscribe to default topics
                subscribeToDefaultTopics();
            });
    }
    
    private void saveFCMTokenToProfile(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmToken", token);
            
            db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save FCM token", e);
                });
        }
    }
    
    private void subscribeToDefaultTopics() {
        // Subscribe to general topics
        subscribeToTopic("all_users");
        subscribeToTopic("general_announcements");
        
        // Subscribe based on user type
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Would check user profile for graduation year, major, etc.
            // and subscribe to relevant topics
        }
    }
    
    private void updateTopicSubscriptions() {
        // Update topic subscriptions based on notification preferences
        for (NotificationType type : NotificationType.values()) {
            String topic = "notif_" + type.toString().toLowerCase();
            if (isNotificationTypeEnabled(type)) {
                subscribeToTopic(topic);
            } else {
                unsubscribeFromTopic(topic);
            }
        }
    }
    
    private Intent createIntentForNotification(CustomNotification notification) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add notification data to intent
        if (notification.actionUrl != null) {
            intent.putExtra("action_url", notification.actionUrl);
        }
        intent.putExtra("notification_type", notification.type.toString());
        
        return intent;
    }
    
    private int getPriorityValue(Priority priority) {
        switch (priority) {
            case LOW:
                return NotificationCompat.PRIORITY_LOW;
            case NORMAL:
                return NotificationCompat.PRIORITY_DEFAULT;
            case HIGH:
                return NotificationCompat.PRIORITY_HIGH;
            case URGENT:
                return NotificationCompat.PRIORITY_MAX;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }
    
    private void loadImageForNotification(String imageUrl, ImageLoadCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                callback.onImageLoaded(bitmap);
            } catch (IOException e) {
                Log.w(TAG, "Failed to load notification image", e);
                callback.onImageLoaded(null);
            }
        }).start();
    }
    
    private interface ImageLoadCallback {
        void onImageLoaded(Bitmap bitmap);
    }
    
    private void sendToToken(CustomNotification notification, String token) {
        // This would typically be done server-side using Firebase Admin SDK
        Log.d(TAG, "Would send notification to token: " + token);
    }
    
    private void saveNotificationToHistory(CustomNotification notification) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        NotificationHistoryItem historyItem = new NotificationHistoryItem(notification, currentUser.getUid());
        
        db.collection(HISTORY_COLLECTION)
            .document(historyItem.id)
            .set(historyItem)
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save notification to history", e);
            });
    }
}
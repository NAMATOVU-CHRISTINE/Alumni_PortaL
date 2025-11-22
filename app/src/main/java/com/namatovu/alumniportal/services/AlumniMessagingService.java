package com.namatovu.alumniportal.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.namatovu.alumniportal.HomeActivity;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.utils.NotificationHelper;

/**
 * Firebase Cloud Messaging Service for handling push notifications
 */
public class AlumniMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "AlumniMessagingService";
    private static final String CHANNEL_ID = "alumni_notifications";
    private static final String CHANNEL_NAME = "Alumni Portal Notifications";
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());
        
        // Check if notifications are enabled
        if (!NotificationHelper.areNotificationsEnabled()) {
            Log.d(TAG, "Notifications are disabled, ignoring message");
            return;
        }
        
        // Get notification data
        String title = remoteMessage.getNotification() != null ? 
            remoteMessage.getNotification().getTitle() : "Alumni Portal";
        String body = remoteMessage.getNotification() != null ? 
            remoteMessage.getNotification().getBody() : "You have a new notification";
        
        // Check notification type and if it's enabled
        String notificationType = remoteMessage.getData().get("type");
        if (!isNotificationTypeEnabled(notificationType)) {
            Log.d(TAG, "Notification type " + notificationType + " is disabled");
            return;
        }
        
        // Send notification
        sendNotification(title, body, notificationType, remoteMessage.getData());
    }
    
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Update token in Firestore
        NotificationHelper.updateTokenInFirestore(token);
    }
    
    /**
     * Check if a specific notification type is enabled
     */
    private boolean isNotificationTypeEnabled(String type) {
        if (type == null) return true; // Allow if no type specified
        
        switch (type) {
            case "message":
                return NotificationHelper.areMessageNotificationsEnabled();
            case "mentorship":
                return NotificationHelper.areMentorshipNotificationsEnabled();
            case "event":
                return NotificationHelper.areEventNotificationsEnabled();
            case "job":
                return NotificationHelper.areJobNotificationsEnabled();
            case "news":
                return NotificationHelper.areNewsNotificationsEnabled();
            default:
                return true;
        }
    }
    
    /**
     * Send notification to device
     */
    private void sendNotification(String title, String body, String type, java.util.Map<String, String> data) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add data to intent
        if (data != null) {
            for (String key : data.keySet()) {
                intent.putExtra(key, data.get(key));
            }
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Create notification channel for Android 8+
        createNotificationChannel();
        
        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        // Add style for longer text
        if (body.length() > 50) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(body));
        }
        
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
            Log.d(TAG, "Notification sent: " + title);
        }
    }
    
    /**
     * Create notification channel for Android 8+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications from Alumni Portal");
            
            NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

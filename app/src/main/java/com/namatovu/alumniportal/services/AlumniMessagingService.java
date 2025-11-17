package com.namatovu.alumniportal.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
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
    private static final String CHANNEL_ID = "alumni_portal_notifications";
    private static final String CHANNEL_NAME = "Alumni Portal";
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);
            
            sendNotification(title, body, remoteMessage.getData());
        }
        
        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataPayload(remoteMessage.getData());
        }
    }
    
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        // Update token in Firestore
        NotificationHelper.updateTokenInFirestore(token);
    }
    
    /**
     * Handle data payload from FCM
     */
    private void handleDataPayload(java.util.Map<String, String> data) {
        String type = data.get("type");
        
        if (type == null) return;
        
        switch (type) {
            case "message":
                if (NotificationHelper.areMessageNotificationsEnabled()) {
                    String senderName = data.get("senderName");
                    String messageText = data.get("messageText");
                    sendNotification("New Message from " + senderName, messageText, data);
                }
                break;
                
            case "mentorship":
                if (NotificationHelper.areMentorshipNotificationsEnabled()) {
                    String action = data.get("action");
                    String fromUser = data.get("fromUser");
                    sendNotification("Mentorship Request", fromUser + " " + action, data);
                }
                break;
                
            case "event":
                if (NotificationHelper.areEventNotificationsEnabled()) {
                    String eventTitle = data.get("eventTitle");
                    String eventAction = data.get("action");
                    sendNotification("Event Update", eventTitle + " - " + eventAction, data);
                }
                break;
                
            case "job":
                if (NotificationHelper.areJobNotificationsEnabled()) {
                    String jobTitle = data.get("jobTitle");
                    String company = data.get("company");
                    sendNotification("New Job Posting", jobTitle + " at " + company, data);
                }
                break;
                
            case "news":
                if (NotificationHelper.areNewsNotificationsEnabled()) {
                    String newsTitle = data.get("newsTitle");
                    sendNotification("News Update", newsTitle, data);
                }
                break;
        }
    }
    
    /**
     * Create and show a notification
     */
    private void sendNotification(String title, String messageBody, java.util.Map<String, String> data) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add data to intent if available
        if (data != null) {
            for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for Alumni Portal app");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
        
        notificationManager.notify(0, notificationBuilder.build());
    }
}

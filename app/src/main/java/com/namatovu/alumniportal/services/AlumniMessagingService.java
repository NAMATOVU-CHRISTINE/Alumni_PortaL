package com.namatovu.alumniportal.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.namatovu.alumniportal.utils.NotificationHelper;

import java.util.Map;

/**
 * Service to handle incoming FCM messages
 */
public class AlumniMessagingService extends FirebaseMessagingService {
    private static final String TAG = "AlumniMessagingService";
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());
        Log.d(TAG, "Data payload size: " + remoteMessage.getData().size());
        
        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String notificationType = data.get("type");
            
            Log.d(TAG, "Notification type: " + notificationType);
            Log.d(TAG, "Full data: " + data.toString());
            
            handleNotificationByType(notificationType, data);
        } else {
            Log.w(TAG, "No data payload in message");
        }
    }
    
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New FCM token: " + token);
        
        // Update token in local storage and Firestore
        NotificationHelper.updateTokenInFirestore(token);
    }
    
    private void handleNotificationByType(String type, Map<String, String> data) {
        Log.d(TAG, "handleNotificationByType called with type: " + type);
        
        if ("message".equals(type)) {
            Log.d(TAG, "Handling message notification");
            NotificationHelper.showNotification(
                this,
                "New Message",
                data.get("senderName") + ": " + data.get("messageText"),
                data.get("chatId"),
                "message"
            );
        } else if ("event".equals(type)) {
            Log.d(TAG, "Handling event notification");
            NotificationHelper.showNotification(
                this,
                "Event Update",
                data.get("eventTitle") + " - " + data.get("action"),
                data.get("eventId"),
                "event"
            );
        } else if ("job".equals(type)) {
            Log.d(TAG, "Handling job notification");
            NotificationHelper.showNotification(
                this,
                "New Job Opportunity",
                data.get("jobTitle") + " at " + data.get("company"),
                data.get("jobId"),
                "job"
            );
        } else if ("mentorship".equals(type)) {
            Log.d(TAG, "Handling mentorship notification");
            NotificationHelper.showNotification(
                this,
                "Mentorship Update",
                data.get("fromUserName") + " - " + data.get("action"),
                data.get("requestId"),
                "mentorship"
            );
        } else if ("news".equals(type)) {
            Log.d(TAG, "Handling news notification");
            NotificationHelper.showNotification(
                this,
                "New Article",
                data.get("newsTitle") + " by " + data.get("authorName"),
                data.get("newsId"),
                "news"
            );
        } else {
            Log.w(TAG, "Unknown notification type: " + type);
        }
    }
}

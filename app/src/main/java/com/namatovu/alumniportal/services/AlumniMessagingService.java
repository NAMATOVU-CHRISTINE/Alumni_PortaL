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
        
        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String notificationType = data.get("type");
            
            Log.d(TAG, "Notification type: " + notificationType);
            
            handleNotificationByType(notificationType, data);
        }
    }
    
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New FCM token: " + token);
        
        // Update token in local storage and Firestore
        NotificationHelper.updateTokenInFirestore(token);
    }
    
    private void handleNotificationByType(String type, Map<String, String> data) {
        if ("message".equals(type)) {
            NotificationHelper.showNotification(
                this,
                "New Message",
                data.get("senderName") + ": " + data.get("messageText"),
                data.get("chatId"),
                "message"
            );
        } else if ("event".equals(type)) {
            NotificationHelper.showNotification(
                this,
                "Event Update",
                data.get("eventTitle") + " - " + data.get("action"),
                data.get("eventId"),
                "event"
            );
        } else if ("job".equals(type)) {
            NotificationHelper.showNotification(
                this,
                "New Job Opportunity",
                data.get("jobTitle") + " at " + data.get("company"),
                data.get("jobId"),
                "job"
            );
        } else if ("mentorship".equals(type)) {
            NotificationHelper.showNotification(
                this,
                "Mentorship Update",
                data.get("fromUserName") + " - " + data.get("action"),
                data.get("requestId"),
                "mentorship"
            );
        } else if ("news".equals(type)) {
            NotificationHelper.showNotification(
                this,
                "New Article",
                data.get("newsTitle") + " by " + data.get("authorName"),
                data.get("newsId"),
                "news"
            );
        }
    }
}

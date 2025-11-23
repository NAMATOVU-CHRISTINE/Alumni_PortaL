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
        Log.d(TAG, ">>> handleNotificationByType called with type: " + type);
        Log.d(TAG, "Data keys: " + data.keySet().toString());
        
        try {
            if ("message".equals(type)) {
                Log.d(TAG, ">>> Handling MESSAGE notification");
                String senderName = data.get("senderName");
                String messageText = data.get("messageText");
                String chatId = data.get("chatId");
                Log.d(TAG, "Sender: " + senderName + ", Message: " + messageText + ", ChatId: " + chatId);
                
                NotificationHelper.showNotification(
                    this,
                    "New Message",
                    senderName + ": " + messageText,
                    chatId,
                    "message"
                );
            } else if ("event".equals(type)) {
                Log.d(TAG, ">>> Handling EVENT notification");
                String eventTitle = data.get("eventTitle");
                String action = data.get("action");
                String eventId = data.get("eventId");
                Log.d(TAG, "Event: " + eventTitle + ", Action: " + action + ", EventId: " + eventId);
                
                NotificationHelper.showNotification(
                    this,
                    "Event Update",
                    eventTitle + " - " + action,
                    eventId,
                    "event"
                );
            } else if ("job".equals(type)) {
                Log.d(TAG, ">>> Handling JOB notification");
                String jobTitle = data.get("jobTitle");
                String company = data.get("company");
                String jobId = data.get("jobId");
                Log.d(TAG, "Job: " + jobTitle + " at " + company + ", JobId: " + jobId);
                
                NotificationHelper.showNotification(
                    this,
                    "New Job Opportunity",
                    jobTitle + " at " + company,
                    jobId,
                    "job"
                );
            } else if ("mentorship".equals(type)) {
                Log.d(TAG, ">>> Handling MENTORSHIP notification");
                String fromUserName = data.get("fromUserName");
                String action = data.get("action");
                String requestId = data.get("requestId");
                Log.d(TAG, "From: " + fromUserName + ", Action: " + action + ", RequestId: " + requestId);
                
                NotificationHelper.showNotification(
                    this,
                    "Mentorship Update",
                    fromUserName + " - " + action,
                    requestId,
                    "mentorship"
                );
            } else if ("news".equals(type)) {
                Log.d(TAG, ">>> Handling NEWS notification");
                String newsTitle = data.get("newsTitle");
                String authorName = data.get("authorName");
                String newsId = data.get("newsId");
                Log.d(TAG, "News: " + newsTitle + " by " + authorName + ", NewsId: " + newsId);
                
                NotificationHelper.showNotification(
                    this,
                    "New Article",
                    newsTitle + " by " + authorName,
                    newsId,
                    "news"
                );
            } else {
                Log.w(TAG, "!!! Unknown notification type: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "ERROR in handleNotificationByType: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
}

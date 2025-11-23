package com.namatovu.alumniportal.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.namatovu.alumniportal.receivers.NotificationBroadcastReceiver;
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
        
        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Log.d(TAG, "Notification title: " + notification.getTitle());
            Log.d(TAG, "Notification body: " + notification.getBody());
        }
    }
    
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New FCM token: " + token);
        
        // Update token in local storage and Firestore
        NotificationHelper.updateTokenInFirestore(token);
    }
    
    private void handleNotificationByType(String type, Map<String, String> data) {
        Intent intent = new Intent();
        
        if ("message".equals(type)) {
            intent.setAction(NotificationBroadcastReceiver.ACTION_MESSAGE_RECEIVED);
            intent.putExtra("senderId", data.get("senderId"));
            intent.putExtra("senderName", data.get("senderName"));
            intent.putExtra("messageText", data.get("messageText"));
            intent.putExtra("chatId", data.get("chatId"));
        } else if ("event".equals(type)) {
            intent.setAction(NotificationBroadcastReceiver.ACTION_EVENT_RECEIVED);
            intent.putExtra("eventId", data.get("eventId"));
            intent.putExtra("eventTitle", data.get("eventTitle"));
            intent.putExtra("action", data.get("action"));
        } else if ("job".equals(type)) {
            intent.setAction(NotificationBroadcastReceiver.ACTION_JOB_RECEIVED);
            intent.putExtra("jobId", data.get("jobId"));
            intent.putExtra("jobTitle", data.get("jobTitle"));
            intent.putExtra("company", data.get("company"));
        } else if ("mentorship".equals(type)) {
            intent.setAction(NotificationBroadcastReceiver.ACTION_MENTORSHIP_RECEIVED);
            intent.putExtra("requestId", data.get("requestId"));
            intent.putExtra("fromUserName", data.get("fromUserName"));
            intent.putExtra("action", data.get("action"));
        } else if ("news".equals(type)) {
            intent.setAction(NotificationBroadcastReceiver.ACTION_NEWS_RECEIVED);
            intent.putExtra("newsId", data.get("newsId"));
            intent.putExtra("newsTitle", data.get("newsTitle"));
            intent.putExtra("authorName", data.get("authorName"));
        }
        
        // Broadcast the intent
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

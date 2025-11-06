package com.namatovu.alumniportal.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.activities.ChatActivity;
import com.namatovu.alumniportal.activities.ChatListActivity;
import com.namatovu.alumniportal.EventsActivity;
import com.namatovu.alumniportal.JobBoardActivity;
import com.namatovu.alumniportal.MentorshipActivity;
import com.namatovu.alumniportal.NewsFeedActivity;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.Map;

public class AlumniNotificationService extends FirebaseMessagingService {
    private static final String TAG = "AlumniNotificationService";
    
    // Notification channels
    private static final String CHANNEL_MESSAGES = "messages";
    private static final String CHANNEL_MENTORSHIP = "mentorship";
    private static final String CHANNEL_EVENTS = "events";
    private static final String CHANNEL_JOBS = "jobs";
    private static final String CHANNEL_NEWS = "news";
    private static final String CHANNEL_GENERAL = "general";
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
        
        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotificationMessage(remoteMessage);
        }
        
        // Track notification received
        AnalyticsHelper.logEvent("notification_received", "notification_type", 
                getNotificationType(remoteMessage.getData()));
    }
    
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to your app server for user-specific notifications
        sendRegistrationToServer(token);
        
        // Track token refresh
        AnalyticsHelper.logEvent("fcm_token_refreshed", null, null);
    }
    
    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        String title = data.get("title");
        String body = data.get("body");
        
        if (type == null || title == null || body == null) {
            Log.w(TAG, "Invalid notification data");
            return;
        }
        
        switch (type) {
            case "chat_message":
                handleChatMessage(data);
                break;
            case "mentorship_request":
                handleMentorshipNotification(data);
                break;
            case "event_update":
                handleEventNotification(data);
                break;
            case "job_posting":
                handleJobNotification(data);
                break;
            case "news_article":
                handleNewsNotification(data);
                break;
            default:
                handleGeneralNotification(data);
                break;
        }
    }
    
    private void handleNotificationMessage(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        
        // Create a general notification if no data payload is provided
        createNotification(CHANNEL_GENERAL, title, body, null, 0);
    }
    
    private void handleChatMessage(Map<String, String> data) {
        String chatId = data.get("chatId");
        String senderId = data.get("senderId");
        String senderName = data.get("senderName");
        String messageText = data.get("messageText");
        String messageType = data.get("messageType");
        
        String title = senderName != null ? senderName : "New Message";
        String body = getMessageDisplayText(messageText, messageType);
        
        // Create intent to open chat
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", senderId);
        intent.putExtra("otherUserName", senderName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        createNotification(CHANNEL_MESSAGES, title, body, intent, 1);
    }
    
    private void handleMentorshipNotification(Map<String, String> data) {
        String requestId = data.get("requestId");
        String fromUserId = data.get("fromUserId");
        String fromUserName = data.get("fromUserName");
        String action = data.get("action"); // "request", "accept", "reject"
        
        String title, body;
        switch (action) {
            case "request":
                title = "New Mentorship Request";
                body = fromUserName + " would like you to be their mentor";
                break;
            case "accept":
                title = "Mentorship Request Accepted";
                body = fromUserName + " accepted your mentorship request";
                break;
            case "reject":
                title = "Mentorship Request Declined";
                body = fromUserName + " declined your mentorship request";
                break;
            default:
                title = "Mentorship Update";
                body = "You have a mentorship update";
                break;
        }
        
        Intent intent = new Intent(this, MentorshipActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        createNotification(CHANNEL_MENTORSHIP, title, body, intent, 2);
    }
    
    private void handleEventNotification(Map<String, String> data) {
        String eventId = data.get("eventId");
        String eventTitle = data.get("eventTitle");
        String action = data.get("action"); // "created", "updated", "reminder"
        
        String title, body;
        switch (action) {
            case "created":
                title = "New Event";
                body = "New event: " + eventTitle;
                break;
            case "updated":
                title = "Event Updated";
                body = eventTitle + " has been updated";
                break;
            case "reminder":
                title = "Event Reminder";
                body = eventTitle + " starts soon";
                break;
            default:
                title = "Event Update";
                body = "You have an event update";
                break;
        }
        
        Intent intent = new Intent(this, EventsActivity.class);
        intent.putExtra("eventId", eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        createNotification(CHANNEL_EVENTS, title, body, intent, 3);
    }
    
    private void handleJobNotification(Map<String, String> data) {
        String jobId = data.get("jobId");
        String jobTitle = data.get("jobTitle");
        String company = data.get("company");
        
        String title = "New Job Opportunity";
        String body = jobTitle + " at " + company;
        
        Intent intent = new Intent(this, JobBoardActivity.class);
        intent.putExtra("jobId", jobId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        createNotification(CHANNEL_JOBS, title, body, intent, 4);
    }
    
    private void handleNewsNotification(Map<String, String> data) {
        String articleId = data.get("articleId");
        String articleTitle = data.get("articleTitle");
        String category = data.get("category");
        
        String title = "News Update";
        String body = articleTitle;
        
        Intent intent = new Intent(this, NewsFeedActivity.class);
        intent.putExtra("articleId", articleId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        createNotification(CHANNEL_NEWS, title, body, intent, 5);
    }
    
    private void handleGeneralNotification(Map<String, String> data) {
        String title = data.get("title");
        String body = data.get("body");
        
        createNotification(CHANNEL_GENERAL, title, body, null, 6);
    }
    
    private void createNotification(String channelId, String title, String body, Intent intent, int notificationId) {
        PendingIntent pendingIntent = null;
        
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(
                this, 
                notificationId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            // Default intent to open the app
            Intent defaultIntent = new Intent(this, ChatListActivity.class);
            defaultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(
                this, 
                notificationId, 
                defaultIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        // Add action buttons for specific notification types
        if (CHANNEL_MESSAGES.equals(channelId) && intent != null) {
            // Add quick reply action for messages
            Intent replyIntent = new Intent(this, ChatActivity.class);
            replyIntent.putExtras(intent.getExtras());
            PendingIntent replyPendingIntent = PendingIntent.getActivity(
                this, 
                notificationId + 100, 
                replyIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            notificationBuilder.addAction(
                R.drawable.ic_reply, 
                "Reply", 
                replyPendingIntent
            );
        }
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
        
        // Track notification displayed
        AnalyticsHelper.logEvent("notification_displayed", "channel", channelId);
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            
            // Messages channel
            NotificationChannel messagesChannel = new NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            );
            messagesChannel.setDescription("Chat messages from other alumni");
            messagesChannel.enableVibration(true);
            notificationManager.createNotificationChannel(messagesChannel);
            
            // Mentorship channel
            NotificationChannel mentorshipChannel = new NotificationChannel(
                CHANNEL_MENTORSHIP,
                "Mentorship",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            mentorshipChannel.setDescription("Mentorship requests and updates");
            notificationManager.createNotificationChannel(mentorshipChannel);
            
            // Events channel
            NotificationChannel eventsChannel = new NotificationChannel(
                CHANNEL_EVENTS,
                "Events",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            eventsChannel.setDescription("Event updates and reminders");
            notificationManager.createNotificationChannel(eventsChannel);
            
            // Jobs channel
            NotificationChannel jobsChannel = new NotificationChannel(
                CHANNEL_JOBS,
                "Job Opportunities",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            jobsChannel.setDescription("New job postings and opportunities");
            notificationManager.createNotificationChannel(jobsChannel);
            
            // News channel
            NotificationChannel newsChannel = new NotificationChannel(
                CHANNEL_NEWS,
                "News",
                NotificationManager.IMPORTANCE_LOW
            );
            newsChannel.setDescription("Alumni news and updates");
            notificationManager.createNotificationChannel(newsChannel);
            
            // General channel
            NotificationChannel generalChannel = new NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("General app notifications");
            notificationManager.createNotificationChannel(generalChannel);
        }
    }
    
    private String getMessageDisplayText(String messageText, String messageType) {
        if (messageText == null) messageText = "";
        
        switch (messageType) {
            case "image":
                return "📷 Photo";
            case "file":
                return "📎 File";
            case "location":
                return "📍 Location";
            default:
                return messageText.length() > 50 ? 
                       messageText.substring(0, 47) + "..." : messageText;
        }
    }
    
    private String getNotificationType(Map<String, String> data) {
        String type = data.get("type");
        return type != null ? type : "general";
    }
    
    private void sendRegistrationToServer(String token) {
        // TODO: Send token to your server
        // This would typically involve calling your backend API
        // to associate the FCM token with the current user
        Log.d(TAG, "Sending token to server: " + token);
        
        // For now, just store it locally
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .apply();
    }
}
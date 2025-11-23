package com.namatovu.alumniportal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.namatovu.alumniportal.utils.NotificationHelper;

/**
 * BroadcastReceiver for handling notification events
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    
    public static final String ACTION_MESSAGE_RECEIVED = "com.namatovu.alumniportal.MESSAGE_RECEIVED";
    public static final String ACTION_EVENT_RECEIVED = "com.namatovu.alumniportal.EVENT_RECEIVED";
    public static final String ACTION_JOB_RECEIVED = "com.namatovu.alumniportal.JOB_RECEIVED";
    public static final String ACTION_MENTORSHIP_RECEIVED = "com.namatovu.alumniportal.MENTORSHIP_RECEIVED";
    public static final String ACTION_NEWS_RECEIVED = "com.namatovu.alumniportal.NEWS_RECEIVED";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        
        String action = intent.getAction();
        Log.d(TAG, "Broadcast received: " + action);
        
        if (action == null) return;
        
        switch (action) {
            case ACTION_MESSAGE_RECEIVED:
                handleMessageNotification(context, intent);
                break;
            case ACTION_EVENT_RECEIVED:
                handleEventNotification(context, intent);
                break;
            case ACTION_JOB_RECEIVED:
                handleJobNotification(context, intent);
                break;
            case ACTION_MENTORSHIP_RECEIVED:
                handleMentorshipNotification(context, intent);
                break;
            case ACTION_NEWS_RECEIVED:
                handleNewsNotification(context, intent);
                break;
        }
    }
    
    private void handleMessageNotification(Context context, Intent intent) {
        if (!NotificationHelper.areMessageNotificationsEnabled()) {
            Log.d(TAG, "Message notifications disabled");
            return;
        }
        
        String senderId = intent.getStringExtra("senderId");
        String senderName = intent.getStringExtra("senderName");
        String messageText = intent.getStringExtra("messageText");
        String chatId = intent.getStringExtra("chatId");
        
        Log.d(TAG, "Message from " + senderName + ": " + messageText);
        
        // Display notification
        NotificationHelper.showNotification(
            context,
            "New Message",
            senderName + ": " + messageText,
            chatId,
            "message"
        );
    }
    
    private void handleEventNotification(Context context, Intent intent) {
        if (!NotificationHelper.areEventNotificationsEnabled()) {
            Log.d(TAG, "Event notifications disabled");
            return;
        }
        
        String eventId = intent.getStringExtra("eventId");
        String eventTitle = intent.getStringExtra("eventTitle");
        String action = intent.getStringExtra("action");
        
        Log.d(TAG, "Event notification: " + eventTitle + " - " + action);
        
        NotificationHelper.showNotification(
            context,
            "Event Update",
            eventTitle + " - " + action,
            eventId,
            "event"
        );
    }
    
    private void handleJobNotification(Context context, Intent intent) {
        if (!NotificationHelper.areJobNotificationsEnabled()) {
            Log.d(TAG, "Job notifications disabled");
            return;
        }
        
        String jobId = intent.getStringExtra("jobId");
        String jobTitle = intent.getStringExtra("jobTitle");
        String company = intent.getStringExtra("company");
        
        Log.d(TAG, "Job notification: " + jobTitle + " at " + company);
        
        NotificationHelper.showNotification(
            context,
            "New Job Opportunity",
            jobTitle + " at " + company,
            jobId,
            "job"
        );
    }
    
    private void handleMentorshipNotification(Context context, Intent intent) {
        if (!NotificationHelper.areMentorshipNotificationsEnabled()) {
            Log.d(TAG, "Mentorship notifications disabled");
            return;
        }
        
        String requestId = intent.getStringExtra("requestId");
        String fromUserName = intent.getStringExtra("fromUserName");
        String action = intent.getStringExtra("action");
        
        Log.d(TAG, "Mentorship notification from " + fromUserName + ": " + action);
        
        NotificationHelper.showNotification(
            context,
            "Mentorship Update",
            fromUserName + " - " + action,
            requestId,
            "mentorship"
        );
    }
    
    private void handleNewsNotification(Context context, Intent intent) {
        if (!NotificationHelper.areNewsNotificationsEnabled()) {
            Log.d(TAG, "News notifications disabled");
            return;
        }
        
        String newsId = intent.getStringExtra("newsId");
        String newsTitle = intent.getStringExtra("newsTitle");
        String authorName = intent.getStringExtra("authorName");
        
        Log.d(TAG, "News notification: " + newsTitle + " by " + authorName);
        
        NotificationHelper.showNotification(
            context,
            "New Article",
            newsTitle + " by " + authorName,
            newsId,
            "news"
        );
    }
}

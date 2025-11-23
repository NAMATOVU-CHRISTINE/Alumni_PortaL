package com.namatovu.alumniportal.services;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending both push notifications and emails
 * Handles mentorship requests, event updates, and news notifications
 */
public class NotificationService {
    private static final String TAG = "NotificationService";
    private FirebaseFirestore db;
    private Context context;
    
    public NotificationService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Send mentorship request notification (push + email)
     */
    public void sendMentorshipRequestNotification(String mentorId, String mentorEmail, String mentorName,
                                                  String menteeId, String menteeName, String connectionId) {
        // Send push notification
        sendPushNotification(
            mentorId,
            "New Mentorship Request",
            menteeName + " has sent you a mentorship request",
            "mentorship_request",
            connectionId
        );
        
        // Send email
        sendEmail(
            mentorEmail,
            "New Mentorship Request from " + menteeName,
            "Hi " + mentorName + ",\n\n" +
            menteeName + " has sent you a mentorship request.\n\n" +
            "Please check the Alumni Portal app to review and respond to this request.\n\n" +
            "Best regards,\nAlumni Portal Team",
            "mentorship_request",
            connectionId
        );
        
        Log.d(TAG, "Mentorship request notification sent to " + mentorName);
    }
    
    /**
     * Send event update notification (push + email to all subscribers)
     */
    public void sendEventUpdateNotification(String eventTitle, String eventId, String action, String eventDescription) {
        // Send push notification to all users
        sendBroadcastPushNotification(
            "Event Update: " + eventTitle,
            "New update on " + eventTitle + " - " + action,
            "event_update",
            eventId
        );
        
        // Queue email to all subscribers
        sendBroadcastEmail(
            "Event Update: " + eventTitle,
            "There's a new update on the event: " + eventTitle + "\n\n" +
            "Update: " + action + "\n\n" +
            (eventDescription != null ? "Details: " + eventDescription + "\n\n" : "") +
            "Check the Alumni Portal app for more details.",
            "event_update",
            eventId
        );
        
        Log.d(TAG, "Event update notification sent for: " + eventTitle);
        AnalyticsHelper.logEvent("event_notification_sent", "eventId", eventId);
    }
    
    /**
     * Send news/article notification (push + email to all subscribers)
     */
    public void sendNewsNotification(String newsTitle, String newsId, String authorName, String newsDescription) {
        // Send push notification to all users
        sendBroadcastPushNotification(
            "New Article: " + newsTitle,
            "New article by " + authorName,
            "news_update",
            newsId
        );
        
        // Queue email to all subscribers
        sendBroadcastEmail(
            "New Article: " + newsTitle,
            "A new article has been published!\n\n" +
            "Title: " + newsTitle + "\n" +
            "Author: " + authorName + "\n\n" +
            (newsDescription != null ? "Summary: " + newsDescription + "\n\n" : "") +
            "Read the full article in the Alumni Portal app.",
            "news_update",
            newsId
        );
        
        Log.d(TAG, "News notification sent for: " + newsTitle);
        AnalyticsHelper.logEvent("news_notification_sent", "newsId", newsId);
    }
    
    /**
     * Send mentorship status update notification (push + email)
     */
    public void sendMentorshipStatusNotification(String recipientId, String recipientEmail, String recipientName,
                                                 String senderName, String status, String connectionId) {
        String statusMessage = getStatusMessage(status);
        
        // Send push notification
        sendPushNotification(
            recipientId,
            "Mentorship Request " + statusMessage,
            senderName + " has " + statusMessage.toLowerCase() + " your mentorship request",
            "mentorship_status",
            connectionId
        );
        
        // Send email
        sendEmail(
            recipientEmail,
            "Mentorship Request " + statusMessage,
            "Hi " + recipientName + ",\n\n" +
            senderName + " has " + statusMessage.toLowerCase() + " your mentorship request.\n\n" +
            getStatusEmailBody(status) +
            "\nBest regards,\nAlumni Portal Team",
            "mentorship_status",
            connectionId
        );
        
        Log.d(TAG, "Mentorship status notification sent to " + recipientName);
    }
    
    /**
     * Send message sent notification (local notification to sender)
     */
    public void sendMessageSentNotification(String messageText, String recipientName) {
        // Send local notification to confirm message was sent
        sendPushNotification(
            "local",
            "Message Sent",
            "Your message to " + recipientName + " was sent",
            "message_sent",
            recipientName
        );
        
        Log.d(TAG, "Message sent notification created for: " + recipientName);
    }
    
    /**
     * Send incoming message notification (to recipient)
     */
    public void sendIncomingMessageNotification(String recipientId, String senderName, String messagePreview) {
        // Send push notification to recipient
        sendPushNotification(
            recipientId,
            "New Message from " + senderName,
            messagePreview,
            "incoming_message",
            senderName
        );
        
        Log.d(TAG, "Incoming message notification sent to: " + recipientId);
    }
    
    /**
     * Send push notification to specific user
     */
    private void sendPushNotification(String userId, String title, String message, String type, String referenceId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("type", type);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("referenceId", referenceId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);
        
        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener(doc -> Log.d(TAG, "Push notification created: " + doc.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to create push notification", e));
    }
    
    /**
     * Send broadcast push notification to all users
     */
    private void sendBroadcastPushNotification(String title, String message, String type, String referenceId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("referenceId", referenceId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);
        notification.put("broadcast", true);
        
        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener(doc -> Log.d(TAG, "Broadcast notification created: " + doc.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to create broadcast notification", e));
    }
    
    /**
     * Queue email for backend to send
     */
    private void sendEmail(String recipientEmail, String subject, String body, String type, String referenceId) {
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("to", recipientEmail);
        emailData.put("subject", subject);
        emailData.put("body", body);
        emailData.put("type", type);
        emailData.put("referenceId", referenceId);
        emailData.put("timestamp", System.currentTimeMillis());
        emailData.put("status", "pending");
        
        db.collection("emailQueue")
            .add(emailData)
            .addOnSuccessListener(doc -> Log.d(TAG, "Email queued: " + doc.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to queue email", e));
    }
    
    /**
     * Queue broadcast email for backend to send to all users
     */
    private void sendBroadcastEmail(String subject, String body, String type, String referenceId) {
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("subject", subject);
        emailData.put("body", body);
        emailData.put("type", type);
        emailData.put("referenceId", referenceId);
        emailData.put("timestamp", System.currentTimeMillis());
        emailData.put("status", "pending");
        emailData.put("sendToAll", true);
        
        db.collection("emailQueue")
            .add(emailData)
            .addOnSuccessListener(doc -> Log.d(TAG, "Broadcast email queued: " + doc.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "Failed to queue broadcast email", e));
    }
    
    private String getStatusMessage(String status) {
        switch (status) {
            case "accepted":
                return "Accepted";
            case "rejected":
                return "Rejected";
            case "pending":
                return "Pending";
            default:
                return "Updated";
        }
    }
    
    private String getStatusEmailBody(String status) {
        switch (status) {
            case "accepted":
                return "Great news! Your mentorship request has been accepted.\n\n" +
                       "You can now start chatting with your mentor through the Alumni Portal app.";
            case "rejected":
                return "Unfortunately, your mentorship request has been rejected.\n\n" +
                       "Feel free to reach out to other mentors or try again later.";
            default:
                return "Your mentorship request has been updated.\n\n" +
                       "Check the Alumni Portal app for more details.";
        }
    }
}

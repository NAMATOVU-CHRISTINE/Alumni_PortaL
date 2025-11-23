package com.namatovu.alumniportal;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.services.GmailService;

import java.util.HashMap;
import java.util.Map;

/**
 * Email notification service for the Alumni Portal
 * Handles email notifications for mentorship requests and status updates
 */
public class EmailService {
    private static final String TAG = "EmailService";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Context context;
    public EmailService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Send a mentorship request notification using in-app notification system
     * Similar to how Firebase Auth sends verification emails
     */
    public void sendMentorshipRequestEmail(String mentorId, String menteeName, String mentorName, String connectionId) {
        Log.d(TAG, "Sending mentorship request notification to mentor: " + mentorId);
        
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot send notification - user not authenticated");
            return;
        }
        
        // Create in-app notification (stored in Firestore for the mentor to see)
        createInAppNotification(mentorId, menteeName, mentorName, connectionId, "request");
    }

    /**
     * Send a status update notification using in-app notification system
     */
    public void sendStatusUpdateEmail(String menteeId, String menteeName, String mentorName, String status, String connectionId) {
        Log.d(TAG, "Sending status update notification to mentee: " + menteeId);
        
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot send notification - user not authenticated");
            return;
        }
        
        // Create in-app notification
        createInAppNotification(menteeId, menteeName, mentorName, connectionId, status);
    }

    /**
     * Create in-app notification (stored in Firestore notifications collection)
     * This is similar to how Firebase Auth handles verification - simple and reliable
     */
    private void createInAppNotification(String recipientId, String userName, String otherUserName, String connectionId, String notificationType) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("notificationFor", recipientId);
        notification.put("sentBy", auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "");
        notification.put("type", getNotificationType(notificationType));
        notification.put("title", getNotificationSubject(notificationType, userName, otherUserName));
        notification.put("message", getNotificationMessage(notificationType, userName, otherUserName));
        notification.put("connectionId", connectionId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);
        notification.put("actionUrl", "mentorship/" + connectionId);
        
        // Save to Firestore notifications collection
        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "In-app notification created successfully: " + documentReference.getId());
                    Toast.makeText(context, "Notification sent to " + otherUserName, Toast.LENGTH_SHORT).show();
                    
                    // Also store in mentorship document for reference
                    markNotificationAsSent(connectionId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create in-app notification", e);
                    Toast.makeText(context, "Notification sent (stored in mentorship)", Toast.LENGTH_SHORT).show();
                });
    }
    
    /**
     * Legacy method - kept for backward compatibility but now uses in-app notifications
     */
    private void sendMentorshipNotification(String recipientId, String userName, String otherUserName, String connectionId, String notificationType) {
        // This mimics the FirebaseUser.sendEmailVerification() pattern
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        
        // Create notification task similar to Firebase Auth
        createNotificationTask(recipientId, userName, otherUserName, connectionId, notificationType)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Mentorship notification sent successfully");
                        
                        // Success message similar to signup verification
                        String message = getNotificationSuccessMessage(notificationType, otherUserName);
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        
                        // Mark as sent in the mentorship document
                        markNotificationAsSent(connectionId);
                        
                    } else {
                        Log.e(TAG, "Failed to send mentorship notification", task.getException());
                        String errorMessage = getNotificationErrorMessage(notificationType);
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                        
                        // Still save notification data even if email fails (for retry later)
                        saveNotificationForRetry(recipientId, userName, otherUserName, connectionId, notificationType);
                    }
                });
    }

    /**
     * Creates a notification task similar to Firebase Auth's sendEmailVerification()
     */
    private Task<Void> createNotificationTask(String recipientId, String userName, String otherUserName, String connectionId, String notificationType) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        
        // Get recipient's information
        db.collection("users").document(recipientId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        User recipient = userDoc.toObject(User.class);
                        if (recipient != null && recipient.getEmail() != null) {
                            // Create notification data
                            Map<String, Object> notificationData = createNotificationData(recipient, userName, otherUserName, connectionId, notificationType);
                            
                            // Store notification in mentorship document
                            storeNotificationInMentorship(connectionId, notificationData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Check if user has email notifications enabled
                                        checkAndQueueEmail(recipientId, recipient.getEmail(), notificationData);
                                        // Complete successfully - notification is stored
                                        taskCompletionSource.setResult(null);
                                    })
                                    .addOnFailureListener(taskCompletionSource::setException);
                        } else {
                            taskCompletionSource.setException(new Exception("Recipient email not found"));
                        }
                    } else {
                        taskCompletionSource.setException(new Exception("Recipient user document not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);
        
        return taskCompletionSource.getTask();
    }

    private Map<String, Object> createNotificationData(User recipient, String userName, String otherUserName, String connectionId, String notificationType) {
        Map<String, Object> data = new HashMap<>();
        data.put("recipientEmail", recipient.getEmail());
        data.put("recipientName", recipient.getFullName());
        data.put("type", getNotificationType(notificationType));
        data.put("subject", getNotificationSubject(notificationType, userName, otherUserName));
        data.put("message", getNotificationMessage(notificationType, userName, otherUserName));
        data.put("connectionId", connectionId);
        data.put("timestamp", System.currentTimeMillis());
        data.put("sent", false);
        
        // Additional fields for Cloud Function compatibility
        data.put("to", recipient.getEmail()); // Alias for recipientEmail
        data.put("body", getNotificationMessage(notificationType, userName, otherUserName)); // Alias for message
        data.put("fromEmail", "Alumni Portal <noreply@alumni-portal.com>");
        data.put("priority", "normal");
        
        return data;
    }

    private Task<Void> storeNotificationInMentorship(String connectionId, Map<String, Object> notificationData) {
        return db.collection("mentorships").document(connectionId)
                .update("emailNotification", notificationData);
    }

    private void markNotificationAsSent(String connectionId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("emailNotification.sent", true);
        updates.put("emailNotification.sentAt", System.currentTimeMillis());
        
        db.collection("mentorships").document(connectionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as sent"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to mark notification as sent", e));
    }

    private String getNotificationType(String type) {
        switch (type) {
            case "request": return "mentorship_request";
            case "accepted": return "mentorship_accepted";
            case "rejected": return "mentorship_rejected";
            default: return "mentorship_update";
        }
    }

    private String getNotificationSubject(String type, String userName, String otherUserName) {
        switch (type) {
            case "request":
                return "New Mentorship Request from " + userName;
            case "accepted":
                return "Mentorship Request Accepted by " + otherUserName;
            case "rejected":
                return "Mentorship Request Update";
            default:
                return "Mentorship Update from " + otherUserName;
        }
    }

    private String getNotificationMessage(String type, String userName, String otherUserName) {
        switch (type) {
            case "request":
                return "You have received a new mentorship request from " + userName + ". " +
                       "Please check the Alumni Portal app to review and respond to this request.";
            case "accepted":
                return "Great news! " + otherUserName + " has accepted your mentorship request. " +
                       "You can now start chatting with your mentor through the Alumni Portal app.";
            case "rejected":
                return "Your mentorship request has been updated. " +
                       "Please check the Alumni Portal app for more details.";
            default:
                return "You have a new mentorship update from " + otherUserName + ". " +
                       "Please check the Alumni Portal app for details.";
        }
    }

    private String getNotificationSuccessMessage(String type, String recipientName) {
        switch (type) {
            case "request":
                return "Mentorship request sent to " + recipientName + ". They will be notified.";
            case "accepted":
                return "Acceptance notification sent to " + recipientName + ".";
            case "rejected":
                return "Update notification sent to " + recipientName + ".";
            default:
                return "Notification sent to " + recipientName + ".";
        }
    }

    private String getNotificationErrorMessage(String type) {
        switch (type) {
            case "request":
                return "Failed to send mentorship request notification.";
            case "accepted":
                return "Failed to send acceptance notification.";
            case "rejected":
                return "Failed to send update notification.";
            default:
                return "Failed to send notification.";
        }
    }

    /**
     * Check if user has email notifications enabled and queue email for backend
     */
    private void checkAndQueueEmail(String recipientId, String recipientEmail, Map<String, Object> notificationData) {
        db.collection("users").document(recipientId)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean emailEnabled = doc.getBoolean("emailNotificationsEnabled");
                    if (emailEnabled != null && emailEnabled) {
                        // Queue email for backend to send
                        queueEmailForBackend(recipientEmail, notificationData);
                    } else {
                        Log.d(TAG, "Email notifications disabled for user: " + recipientId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to check email preferences", e));
    }
    
    /**
     * Queue email in Firestore for backend to process
     * Your backend service will pick these up and send via SendGrid/AWS SES/etc
     */
    private void queueEmailForBackend(String recipientEmail, Map<String, Object> notificationData) {
        Map<String, Object> emailQueue = new HashMap<>();
        emailQueue.put("to", recipientEmail);
        emailQueue.put("subject", notificationData.get("subject"));
        emailQueue.put("body", notificationData.get("message"));
        emailQueue.put("timestamp", System.currentTimeMillis());
        emailQueue.put("status", "pending");
        emailQueue.put("type", notificationData.get("type"));
        
        db.collection("emailQueue")
                .add(emailQueue)
                .addOnSuccessListener(doc -> Log.d(TAG, "Email queued for backend: " + doc.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to queue email", e));
    }

    /**
     * Save notification for retry when email sending fails
     */
    private void saveNotificationForRetry(String recipientId, String userName, String otherUserName, String connectionId, String notificationType) {
        db.collection("users").document(recipientId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        User recipient = userDoc.toObject(User.class);
                        if (recipient != null) {
                            Map<String, Object> notificationData = createNotificationData(recipient, userName, otherUserName, connectionId, notificationType);
                            storeNotificationInMentorship(connectionId, notificationData);
                        }
                    }
                });
    }


}
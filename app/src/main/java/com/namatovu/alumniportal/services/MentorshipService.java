package com.namatovu.alumniportal.services;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.models.MentorshipConnection;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import com.namatovu.alumniportal.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing mentorship connections and notifications
 */
public class MentorshipService {
    private static final String TAG = "MentorshipService";
    private static final String MENTORSHIP_COLLECTION = "mentorshipConnections";
    
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;
    
    public interface MentorshipCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface ConnectionsCallback {
        void onConnectionsLoaded(List<MentorshipConnection> connections);
        void onError(String error);
    }
    
    public MentorshipService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        // Initialize NotificationHelper with context
        NotificationHelper.initialize(context);
    }
    
    /**
     * Send a mentorship request
     */
    public void sendMentorshipRequest(String mentorId, String mentorName, String mentorshipType, 
                                     String message, String duration, MentorshipCallback callback) {
        try {
            String currentUserId = auth.getCurrentUser().getUid();
            
            MentorshipConnection connection = new MentorshipConnection();
            connection.setMentorId(mentorId);
            connection.setMenteeId(currentUserId);
            connection.setMentorName(mentorName);
            connection.setMessage(message);
            connection.setMentorshipType(mentorshipType);
            connection.setDuration(duration);
            connection.setStatus("pending");
            connection.setRequestedAt(System.currentTimeMillis());
            
            db.collection(MENTORSHIP_COLLECTION)
                    .add(connection.toMap())
                    .addOnSuccessListener(documentReference -> {
                        String connectionId = documentReference.getId();
                        Log.d(TAG, "Mentorship request sent: " + connectionId);
                        
                        // Send notification to mentor
                        sendMentorNotification(mentorId, mentorName, currentUserId, "request");
                        
                        // Log analytics
                        AnalyticsHelper.logMentorConnection("request_sent", mentorId);
                        
                        if (callback != null) {
                            callback.onSuccess("Mentorship request sent successfully!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error sending mentorship request", e);
                        if (callback != null) {
                            callback.onError("Failed to send request: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in sendMentorshipRequest", e);
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Accept a mentorship request
     */
    public void acceptMentorshipRequest(String connectionId, MentorshipCallback callback) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "accepted");
            updates.put("acceptedAt", System.currentTimeMillis());
            
            db.collection(MENTORSHIP_COLLECTION)
                    .document(connectionId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Mentorship request accepted: " + connectionId);
                        
                        // Get connection details to send notification
                        getConnectionDetails(connectionId, connection -> {
                            if (connection != null) {
                                // Send notification to mentee
                                sendMenteeNotification(connection.getMenteeId(), 
                                    connection.getMentorName(), "accepted");
                                
                                // Log analytics
                                AnalyticsHelper.logMentorConnection("request_accepted", 
                                    connection.getMenteeId());
                            }
                        });
                        
                        if (callback != null) {
                            callback.onSuccess("Mentorship request accepted!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error accepting mentorship request", e);
                        if (callback != null) {
                            callback.onError("Failed to accept request: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in acceptMentorshipRequest", e);
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Reject a mentorship request
     */
    public void rejectMentorshipRequest(String connectionId, MentorshipCallback callback) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "rejected");
            
            db.collection(MENTORSHIP_COLLECTION)
                    .document(connectionId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Mentorship request rejected: " + connectionId);
                        if (callback != null) {
                            callback.onSuccess("Mentorship request rejected");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error rejecting mentorship request", e);
                        if (callback != null) {
                            callback.onError("Failed to reject request: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in rejectMentorshipRequest", e);
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get all connections for current user
     */
    public void getMyConnections(ConnectionsCallback callback) {
        try {
            String currentUserId = auth.getCurrentUser().getUid();
            
            db.collection(MENTORSHIP_COLLECTION)
                    .whereIn("status", List.of("accepted", "active"))
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<MentorshipConnection> connections = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            MentorshipConnection connection = document.toObject(MentorshipConnection.class);
                            connection.setConnectionId(document.getId());
                            
                            // Include if user is mentor or mentee
                            if (connection.getMentorId().equals(currentUserId) || 
                                connection.getMenteeId().equals(currentUserId)) {
                                connections.add(connection);
                            }
                        }
                        
                        Log.d(TAG, "Loaded " + connections.size() + " connections");
                        if (callback != null) {
                            callback.onConnectionsLoaded(connections);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading connections", e);
                        if (callback != null) {
                            callback.onError("Failed to load connections: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getMyConnections", e);
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get pending requests for current user (as mentor)
     */
    public void getPendingRequests(ConnectionsCallback callback) {
        try {
            String currentUserId = auth.getCurrentUser().getUid();
            
            db.collection(MENTORSHIP_COLLECTION)
                    .whereEqualTo("mentorId", currentUserId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<MentorshipConnection> requests = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            MentorshipConnection connection = document.toObject(MentorshipConnection.class);
                            connection.setConnectionId(document.getId());
                            requests.add(connection);
                        }
                        
                        Log.d(TAG, "Loaded " + requests.size() + " pending requests");
                        if (callback != null) {
                            callback.onConnectionsLoaded(requests);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading pending requests", e);
                        if (callback != null) {
                            callback.onError("Failed to load requests: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getPendingRequests", e);
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Check if user is connected with someone
     */
    public void isConnectedWith(String otherUserId, ConnectionCheckCallback callback) {
        try {
            String currentUserId = auth.getCurrentUser().getUid();
            
            db.collection(MENTORSHIP_COLLECTION)
                    .whereIn("status", List.of("accepted", "active"))
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean isConnected = false;
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            MentorshipConnection connection = document.toObject(MentorshipConnection.class);
                            
                            if ((connection.getMentorId().equals(currentUserId) && 
                                 connection.getMenteeId().equals(otherUserId)) ||
                                (connection.getMentorId().equals(otherUserId) && 
                                 connection.getMenteeId().equals(currentUserId))) {
                                isConnected = true;
                                break;
                            }
                        }
                        
                        if (callback != null) {
                            callback.onResult(isConnected);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking connection", e);
                        if (callback != null) {
                            callback.onError("Error: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in isConnectedWith", e);
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get connection details
     */
    private void getConnectionDetails(String connectionId, ConnectionDetailsCallback callback) {
        try {
            db.collection(MENTORSHIP_COLLECTION)
                    .document(connectionId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            MentorshipConnection connection = documentSnapshot.toObject(MentorshipConnection.class);
                            connection.setConnectionId(documentSnapshot.getId());
                            if (callback != null) {
                                callback.onDetails(connection);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting connection details", e);
                        if (callback != null) {
                            callback.onDetails(null);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getConnectionDetails", e);
            if (callback != null) {
                callback.onDetails(null);
            }
        }
    }
    
    /**
     * Send notification to mentor about new request
     */
    private void sendMentorNotification(String mentorId, String mentorName, String menteeId, String action) {
        try {
            // Get mentee details
            db.collection("users")
                    .document(menteeId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String menteeName = documentSnapshot.getString("fullName");
                            String menteeEmail = documentSnapshot.getString("email");
                            
                            // Send in-app notification
                            String title = "New Mentorship Request";
                            String message = menteeName + " wants to connect with you as a mentor";
                            notificationHelper.sendNotification(title, message, "mentorship_request", menteeId);
                            
                            // Send email notification
                            sendEmailNotification(menteeEmail, menteeName, mentorName, "request");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error sending mentor notification", e);
        }
    }
    
    /**
     * Send notification to mentee about request acceptance
     */
    private void sendMenteeNotification(String menteeId, String mentorName, String action) {
        try {
            // Get mentee details
            db.collection("users")
                    .document(menteeId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String menteeEmail = documentSnapshot.getString("email");
                            
                            // Send in-app notification
                            String title = "Mentorship Request Accepted!";
                            String message = mentorName + " accepted your mentorship request";
                            notificationHelper.sendNotification(title, message, "mentorship_accepted", mentorName);
                            
                            // Send email notification
                            sendEmailNotification(menteeEmail, null, mentorName, "accepted");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error sending mentee notification", e);
        }
    }
    
    /**
     * Send email notification
     */
    private void sendEmailNotification(String recipientEmail, String senderName, String mentorName, String action) {
        try {
            if (recipientEmail == null || recipientEmail.isEmpty()) {
                return;
            }
            
            String subject;
            String body;
            
            if ("request".equals(action)) {
                subject = "New Mentorship Request from " + senderName;
                body = "Hi " + mentorName + ",\n\n" +
                       senderName + " has sent you a mentorship request.\n\n" +
                       "Log in to the Alumni Portal to view and respond to the request.\n\n" +
                       "Best regards,\nAlumni Portal Team";
            } else if ("accepted".equals(action)) {
                subject = "Your Mentorship Request Has Been Accepted!";
                body = "Hi,\n\n" +
                       "Great news! " + mentorName + " has accepted your mentorship request.\n\n" +
                       "You can now start your mentorship journey. Log in to the Alumni Portal to connect.\n\n" +
                       "Best regards,\nAlumni Portal Team";
            } else {
                return;
            }
            
            // Create email intent
            EmailService emailService = new EmailService();
            emailService.sendEmail(recipientEmail, subject, body);
            
            Log.d(TAG, "Email notification sent to: " + recipientEmail);
        } catch (Exception e) {
            Log.e(TAG, "Error sending email notification", e);
        }
    }
    
    // Callback interfaces
    public interface ConnectionCheckCallback {
        void onResult(boolean isConnected);
        void onError(String error);
    }
    
    public interface ConnectionDetailsCallback {
        void onDetails(MentorshipConnection connection);
    }
}

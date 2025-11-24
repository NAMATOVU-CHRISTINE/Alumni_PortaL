package com.namatovu.alumniportal.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.namatovu.alumniportal.HomeActivity;
import com.namatovu.alumniportal.R;

/**
 * Service to listen for real-time notifications from Firestore
 * Displays push notifications when mentorship requests and other events occur
 */
public class NotificationListenerService {
    private static final String TAG = "NotificationListenerService";
    private static final String CHANNEL_ID = "alumni_notifications";
    private static final int NOTIFICATION_ID = 1001;
    
    private FirebaseFirestore db;
    private FirebaseDatabase realtimeDb;
    private FirebaseAuth mAuth;
    private Context context;
    private ListenerRegistration listenerRegistration;
    private ValueEventListener realtimeListener;
    private static int notificationCounter = 0;
    
    public NotificationListenerService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.realtimeDb = FirebaseDatabase.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Start listening for notifications for the current user
     */
    public void startListening() {
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "No user logged in, cannot start listening");
            return;
        }
        
        String currentUserId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Starting notification listener for user: " + currentUserId);
        
        // Create notification channel for Android 8+
        createNotificationChannel();
        
        // Try Firestore first, fallback to Realtime Database
        try {
            // Listen for new notifications for this user in Firestore
            // Only listen for ADDED changes to avoid processing old notifications
            listenerRegistration = db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("read", false)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for Firestore notifications", error);
                        return;
                    }
                    
                    if (querySnapshot != null && !querySnapshot.getDocumentChanges().isEmpty()) {
                        Log.d(TAG, "Received " + querySnapshot.getDocumentChanges().size() + " notification changes");
                        querySnapshot.getDocumentChanges().forEach(change -> {
                            try {
                                if (change.getType().toString().equals("ADDED")) {
                                    // New notification received
                                    String title = change.getDocument().getString("title");
                                    String message = change.getDocument().getString("message");
                                    String type = change.getDocument().getString("type");
                                    String referenceId = change.getDocument().getString("referenceId");
                                    
                                    Log.d(TAG, "New Firestore notification: " + title + " - " + message);
                                    
                                    // Show notification
                                    showNotification(title, message, type, referenceId);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing notification change", e);
                            }
                        });
                    }
                });
            Log.d(TAG, "Firestore listener registered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firestore listener", e);
        }
    }
    
    /**
     * Start listening using Realtime Database as fallback
     */
    private void startRealtimeListener(String currentUserId) {
        DatabaseReference notificationsRef = realtimeDb.getReference("notifications").child(currentUserId);
        
        realtimeListener = notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);
                    String type = snapshot.child("type").getValue(String.class);
                    String referenceId = snapshot.child("referenceId").getValue(String.class);
                    Boolean read = snapshot.child("read").getValue(Boolean.class);
                    
                    if (read == null || !read) {
                        Log.d(TAG, "New Realtime DB notification: " + title + " - " + message);
                        showNotification(title, message, type, referenceId);
                        
                        // Mark as read
                        snapshot.getRef().child("read").setValue(true);
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error listening for Realtime DB notifications", error.toException());
            }
        });
    }
    
    /**
     * Stop listening for notifications
     */
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            Log.d(TAG, "Stopped listening for notifications");
        }
    }
    
    /**
     * Show a push notification
     */
    private void showNotification(String title, String message, String type, String referenceId) {
        try {
            // Validate and set defaults for null values
            if (title == null || title.isEmpty()) {
                title = "Alumni Portal";
            }
            if (message == null || message.isEmpty()) {
                message = "You have a new notification";
            }
            if (type == null || type.isEmpty()) {
                type = "general";
            }
            
            // Ensure notification channel is created
            createNotificationChannel();
            
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager is null");
                return;
            }
            
            // Create intent based on notification type
            Intent intent;
            
            if ("message".equalsIgnoreCase(type) || "chat".equalsIgnoreCase(type)) {
                // For message notifications, open ChatActivity directly
                intent = new Intent(context, com.namatovu.alumniportal.activities.ChatActivity.class);
                intent.putExtra("chatId", referenceId);
                intent.putExtra("otherUserId", referenceId);
            } else if ("mentorship_request".equalsIgnoreCase(type)) {
                // For mentorship requests, open MentorshipActivity
                intent = new Intent(context, com.namatovu.alumniportal.MentorshipActivity.class);
                intent.putExtra("mentorship_id", referenceId);
            } else {
                // Default: open HomeActivity
                intent = new Intent(context, HomeActivity.class);
                intent.putExtra("notification_type", type);
                intent.putExtra("reference_id", referenceId);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Use a unique request code based on referenceId to ensure proper intent handling
            int requestCode = referenceId != null ? referenceId.hashCode() : (int) System.currentTimeMillis();
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Build notification with all required fields
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            // Set notification color
            try {
                builder.setColor(context.getResources().getColor(R.color.must_green, null));
            } catch (Exception e) {
                Log.w(TAG, "Could not set notification color", e);
            }
            
            // Use a stable notification ID based on type and referenceId to prevent duplicates
            int notificationId = Math.abs((type + referenceId).hashCode() % 10000);
            
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification displayed: " + title + " (type: " + type + ", id: " + notificationId + ")");
            
            // Save notification to Firestore so it persists
            saveNotificationToFirestore(title, message, type, referenceId);
        } catch (Exception e) {
            Log.e(TAG, "Error in showNotification", e);
        }
    }
    
    /**
     * Save notification to Firestore for persistence
     */
    private void saveNotificationToFirestore(String title, String message, String type, String referenceId) {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        
        java.util.Map<String, Object> notificationData = new java.util.HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("referenceId", referenceId);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("read", false);
        
        db.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "Notification saved to Firestore: " + docRef.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving notification to Firestore", e);
            });
    }
    
    /**
     * Create notification channel for Android 8+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alumni Portal Notifications";
            String description = "Notifications for mentorship requests and updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

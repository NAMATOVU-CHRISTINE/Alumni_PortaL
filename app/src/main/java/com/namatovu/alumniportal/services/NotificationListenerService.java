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
    private FirebaseAuth mAuth;
    private Context context;
    private ListenerRegistration listenerRegistration;
    private static int notificationCounter = 0;
    
    public NotificationListenerService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
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
        
        // Listen for new notifications for this user
        listenerRegistration = db.collection("notifications")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("read", false)
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening for notifications", error);
                    return;
                }
                
                if (querySnapshot != null) {
                    querySnapshot.getDocumentChanges().forEach(change -> {
                        if (change.getType().toString().equals("ADDED")) {
                            // New notification received
                            String title = change.getDocument().getString("title");
                            String message = change.getDocument().getString("message");
                            String type = change.getDocument().getString("type");
                            String referenceId = change.getDocument().getString("referenceId");
                            
                            Log.d(TAG, "New notification: " + title + " - " + message);
                            
                            // Show notification
                            showNotification(title, message, type, referenceId);
                        }
                    });
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
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create intent to open app when notification is clicked
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra("notification_type", type);
        intent.putExtra("reference_id", referenceId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            (int) System.currentTimeMillis(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        
        // Set notification color
        builder.setColor(context.getResources().getColor(R.color.must_green, null));
        
        // Show notification
        notificationCounter++;
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID + notificationCounter, builder.build());
            Log.d(TAG, "Notification displayed: " + title);
        }
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

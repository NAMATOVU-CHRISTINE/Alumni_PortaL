package com.namatovu.alumniportal.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.database.AlumniDatabase;
import com.namatovu.alumniportal.database.entities.UserEntity;
import com.namatovu.alumniportal.models.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSyncBackgroundService extends Service {
    private static final String TAG = "DataSyncService";
    private ExecutorService executorService;
    private FirebaseFirestore db;
    private AlumniDatabase localDb;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DataSyncBackgroundService created");
        executorService = Executors.newSingleThreadExecutor();
        db = FirebaseFirestore.getInstance();
        localDb = AlumniDatabase.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting background data sync");
        
        // Run sync in background thread
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(() -> {
                syncUserData();
                syncEventsData();
                // Add a small delay before stopping to allow Firebase callbacks to complete
                try {
                    Thread.sleep(2000); // 2 second delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Stop service when done
                stopSelf(startId);
            });
        } else {
            Log.w(TAG, "ExecutorService is not available, stopping service");
            stopSelf(startId);
        }
        
        return START_NOT_STICKY; // Don't restart if killed
    }

    private void syncUserData() {
        try {
            Log.d(TAG, "Syncing user data from Firebase to local database");
            
            db.collection("users")
                .limit(50) // Limit for performance
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if executor is still available before using it
                    if (executorService != null && !executorService.isShutdown()) {
                        executorService.execute(() -> {
                            try {
                                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    User user = doc.toObject(User.class);
                                    user.setUserId(doc.getId());
                                    
                                    // Convert to local entity
                                    UserEntity userEntity = new UserEntity();
                                    userEntity.userId = user.getUserId();
                                    userEntity.fullName = user.getFullName();
                                    userEntity.email = user.getEmail();
                                    userEntity.major = user.getMajor();
                                    userEntity.graduationYear = user.getGraduationYear();
                                    userEntity.currentJob = user.getCurrentJob();
                                    userEntity.company = user.getCompany();
                                    userEntity.profileImageUrl = user.getProfileImageUrl();
                                    userEntity.lastSynced = System.currentTimeMillis();
                                    
                                    // Insert or update in local database
                                    localDb.userDao().insertOrUpdate(userEntity);
                                }
                                Log.d(TAG, "User data sync completed");
                            } catch (Exception e) {
                                Log.e(TAG, "Error syncing user data to local DB", e);
                            }
                        });
                    } else {
                        Log.w(TAG, "ExecutorService is shutdown, skipping user data sync");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching users from Firebase", e);
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in syncUserData", e);
        }
    }

    private void syncEventsData() {
        try {
            Log.d(TAG, "Syncing events data");
            
            db.collection("events")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Events sync completed - " + queryDocumentSnapshots.size() + " events");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error syncing events", e);
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in syncEventsData", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DataSyncBackgroundService destroyed");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                // Wait a bit for tasks to complete
                if (!executorService.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
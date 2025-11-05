package com.namatovu.alumniportal;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.FirebaseApp;

/**
 * Minimal Application class to initialize Firebase and provide a hook for background sync.
 */
public class AlumniApplication extends Application {

    private static final String TAG = "AlumniApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase (safe to call even if google-services.json is missing/wrong)
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized");
        } catch (Exception e) {
            Log.w(TAG, "Firebase initialization failed", e);
        }
    }

    /**
     * Called after successful login to schedule any periodic data syncs. This is a safe stub
     * so calls from activities won't crash the app. Implement WorkManager scheduling here if
     * desired.
     */
    public void scheduleDataSync() {
        Log.d(TAG, "scheduleDataSync() called — scheduling periodic work");
        try {
            androidx.work.PeriodicWorkRequest request = new androidx.work.PeriodicWorkRequest.Builder(
                    DataSyncWorker.class,
                    java.time.Duration.ofHours(12))
                    .build();
            androidx.work.WorkManager.getInstance(this).enqueue(request);
        } catch (Exception e) {
            Log.w(TAG, "Failed to schedule data sync", e);
        }
    }
}

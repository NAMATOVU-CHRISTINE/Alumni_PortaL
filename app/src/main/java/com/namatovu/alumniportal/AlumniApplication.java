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
        // Constraints: require network
        androidx.work.Constraints constraints = new androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build();

        // One-time immediate sync to refresh cache right away
        androidx.work.OneTimeWorkRequest oneTime = new androidx.work.OneTimeWorkRequest.Builder(
            DataSyncCoroutineWorker.class)
            .setConstraints(constraints)
            .build();

        // Periodic sync every 12 hours (use unique name to avoid duplicate scheduling)
        androidx.work.PeriodicWorkRequest periodic = new androidx.work.PeriodicWorkRequest.Builder(
            DataSyncCoroutineWorker.class,
            java.time.Duration.ofHours(12))
            .setConstraints(constraints)
            .build();

        androidx.work.WorkManager wm = androidx.work.WorkManager.getInstance(this);
        // Enqueue unique one-time (will run immediately)
        wm.enqueueUniqueWork("alumni_data_sync_once", androidx.work.ExistingWorkPolicy.KEEP, oneTime);
        // Enqueue or replace periodic work
        wm.enqueueUniquePeriodicWork("alumni_data_sync_periodic", androidx.work.ExistingPeriodicWorkPolicy.KEEP, periodic);
        } catch (Exception e) {
            Log.w(TAG, "Failed to schedule data sync", e);
        }
    }
}

package com.namatovu.alumniportal.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.database.AlumniDatabase;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages offline/online synchronization for the Alumni Portal
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    
    // Work tags
    private static final String SYNC_WORK_TAG = "sync_work";
    private static final String PERIODIC_SYNC_WORK_NAME = "periodic_sync";
    private static final String IMMEDIATE_SYNC_WORK_NAME = "immediate_sync";
    
    // Sync intervals
    private static final long PERIODIC_SYNC_INTERVAL_HOURS = 2;
    private static final long FLEX_TIME_MINUTES = 30;
    
    private static SyncManager instance;
    private Context context;
    private WorkManager workManager;
    private AppDatabase database;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private ConnectivityManager connectivityManager;
    private final Executor executor;
    
    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.workManager = WorkManager.getInstance(context);
        this.database = AppDatabase.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public static SyncManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null) {
                    instance = new SyncManager(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize sync manager and start periodic sync
     */
    public void initialize() {
        Log.d(TAG, "Initializing SyncManager");
        
        // Start periodic sync
        startPeriodicSync();
        
        // Perform initial sync if online
        if (isNetworkAvailable()) {
            triggerImmediateSync();
        }
        
        AnalyticsHelper.logEvent("sync_manager_initialized", null, null);
    }
    
    /**
     * Start periodic background sync
     */
    public void startPeriodicSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        
        PeriodicWorkRequest periodicSyncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                PERIODIC_SYNC_INTERVAL_HOURS,
                TimeUnit.HOURS,
                FLEX_TIME_MINUTES,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();
        
        workManager.enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
        );
        
        Log.d(TAG, "Periodic sync started");
    }
    
    /**
     * Trigger immediate sync
     */
    public void triggerImmediateSync() {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "Network not available, skipping immediate sync");
            return;
        }
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest immediateSyncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build();
        
        workManager.enqueueUniqueWork(
                IMMEDIATE_SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateSyncRequest
        );
        
        Log.d(TAG, "Immediate sync triggered");
        AnalyticsHelper.logEvent("immediate_sync_triggered", null, null);
    }
    
    /**
     * Force sync specific data type
     */
    public void forceSyncDataType(SyncDataType dataType) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "Network not available, cannot force sync for " + dataType.name());
            return;
        }
        
        OneTimeWorkRequest forceSyncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setInputData(SyncWorker.createInputData(dataType))
                .addTag(SYNC_WORK_TAG)
                .build();
        
        workManager.enqueueUniqueWork(
                "force_sync_" + dataType.name(),
                ExistingWorkPolicy.REPLACE,
                forceSyncRequest
        );
        
        Log.d(TAG, "Force sync triggered for " + dataType.name());
        AnalyticsHelper.logEvent("force_sync_triggered", "data_type", dataType.name());
    }
    
    /**
     * Stop all sync operations
     */
    public void stopAllSync() {
        workManager.cancelAllWorkByTag(SYNC_WORK_TAG);
        Log.d(TAG, "All sync operations stopped");
    }
    
    /**
     * Resume sync operations
     */
    public void resumeSync() {
        startPeriodicSync();
        if (isNetworkAvailable()) {
            triggerImmediateSync();
        }
        Log.d(TAG, "Sync operations resumed");
    }
    
    /**
     * Check if network is available
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null && 
               (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }
    
    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
    
    /**
     * Mark data as needing sync
     */
    public void markDataForSync(SyncDataType dataType, String itemId) {
        // This could be implemented to mark specific items for priority sync
        Log.d(TAG, "Marked " + dataType.name() + " item " + itemId + " for sync");
        
        // Trigger immediate sync if network is available
        if (isNetworkAvailable()) {
            triggerImmediateSync();
        }
    }
    
    /**
     * Clear all offline data
     */
    public void clearOfflineData() {
        executor.execute(() -> {
            database.clearAllTables();
        });
        Log.d(TAG, "All offline data cleared");
        AnalyticsHelper.logEvent("offline_data_cleared", null, null);
    }
    
    /**
     * Get sync status summary
     */
    public void getSyncStatus(SyncStatusCallback callback) {
        // This would check the sync status of various data types
        // For now, just return basic network status
        SyncStatus status = new SyncStatus();
        status.isNetworkAvailable = isNetworkAvailable();
        status.isUserAuthenticated = isUserAuthenticated();
        status.lastSyncTime = System.currentTimeMillis(); // Placeholder
        
        callback.onSyncStatus(status);
    }
    
    /**
     * Sync status callback interface
     */
    public interface SyncStatusCallback {
        void onSyncStatus(SyncStatus status);
    }
    
    /**
     * Sync status data class
     */
    public static class SyncStatus {
        public boolean isNetworkAvailable;
        public boolean isUserAuthenticated;
        public long lastSyncTime;
        public int pendingUploadCount;
        public int pendingDownloadCount;
        public String lastSyncError;
    }
    
    /**
     * Enum for different data types that can be synced
     */
    public enum SyncDataType {
        CHAT_MESSAGES,
        USERS,
        JOB_POSTINGS,
        EVENTS,
        ALL
    }
}
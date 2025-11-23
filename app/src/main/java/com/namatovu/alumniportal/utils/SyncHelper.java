package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.namatovu.alumniportal.services.DataSyncService;

/**
 * Helper class to manage data synchronization
 */
public class SyncHelper {
    
    private static final String TAG = "SyncHelper";
    
    /**
     * Start the data sync service
     * @param context Application context
     */
    public static void startSync(Context context) {
        try {
            Intent syncIntent = new Intent(context, DataSyncService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(syncIntent);
            } else {
                context.startService(syncIntent);
            }
            
            Log.d(TAG, "Data sync service started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting sync service", e);
        }
    }
    
    /**
     * Stop the data sync service
     * @param context Application context
     */
    public static void stopSync(Context context) {
        try {
            Intent syncIntent = new Intent(context, DataSyncService.class);
            context.stopService(syncIntent);
            Log.d(TAG, "Data sync service stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping sync service", e);
        }
    }
}

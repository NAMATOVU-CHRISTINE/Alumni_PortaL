package com.namatovu.alumniportal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.namatovu.alumniportal.services.DataSyncService;

/**
 * Broadcast Receiver to start services when device boots up
 * Ensures data stays synced even after device restart
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootCompletedReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device boot completed - Starting sync service");
            
            // Start data sync service after boot
            Intent syncIntent = new Intent(context, DataSyncService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(syncIntent);
            } else {
                context.startService(syncIntent);
            }
        }
    }
}
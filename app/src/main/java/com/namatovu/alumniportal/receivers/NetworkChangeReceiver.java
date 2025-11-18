package com.namatovu.alumniportal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.namatovu.alumniportal.services.DataSyncService;

/**
 * Broadcast Receiver to detect network connectivity changes
 * Automatically triggers data sync when network becomes available
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    
    private static final String TAG = "NetworkChangeReceiver";
    private static boolean wasConnected = true;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network state changed");
        
        if (isNetworkAvailable(context)) {
            Log.d(TAG, "Network is now available");
            
            // Only show toast and sync if we were previously disconnected
            if (!wasConnected) {
                Toast.makeText(context, "Internet connected - Syncing data...", Toast.LENGTH_SHORT).show();
                
                // Start data sync service when network becomes available
                Intent syncIntent = new Intent(context, com.namatovu.alumniportal.services.DataSyncBackgroundService.class);
                context.startService(syncIntent);
            }
            
            wasConnected = true;
        } else {
            Log.d(TAG, "Network is not available");
            
            if (wasConnected) {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            }
            
            wasConnected = false;
        }
    }
    
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        
        return false;
    }
}

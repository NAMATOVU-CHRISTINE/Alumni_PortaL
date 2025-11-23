package com.namatovu.alumniportal.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class to request notification permission for Android 13+
 */
public class NotificationPermissionHelper {
    
    private static final String TAG = "NotificationPermission";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    
    /**
     * Check if notification permission is granted
     */
    public static boolean hasNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                activity, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        // For Android 12 and below, notifications are enabled by default
        return true;
    }
    
    /**
     * Request notification permission for Android 13+
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                Log.d(TAG, "Requesting notification permission");
                ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE
                );
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        }
    }
    
    /**
     * Check if we should show rationale for notification permission
     */
    public static boolean shouldShowRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            );
        }
        return false;
    }
}

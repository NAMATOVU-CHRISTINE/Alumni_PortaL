package com.namatovu.alumniportal.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

public class PermissionHelper {
    
    public static final int CAMERA_PERMISSION_CODE = 100;
    public static final int STORAGE_PERMISSION_CODE = 101;
    public static final int LOCATION_PERMISSION_CODE = 102;
    
    // Required permissions for different features
    public static final String[] CAMERA_PERMISSIONS = {
        Manifest.permission.CAMERA
    };
    
    public static final String[] STORAGE_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    public static final String[] LOCATION_PERMISSIONS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * Check if camera permission is granted
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if storage permissions are granted
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses different permissions
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Check if location permissions are granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request camera permission with explanation
     */
    public static void requestCameraPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            showPermissionExplanation(activity, 
                "Camera Permission Required", 
                "This app needs camera access to take profile photos and share images.",
                () -> ActivityCompat.requestPermissions(activity, CAMERA_PERMISSIONS, CAMERA_PERMISSION_CODE));
        } else {
            ActivityCompat.requestPermissions(activity, CAMERA_PERMISSIONS, CAMERA_PERMISSION_CODE);
        }
    }

    /**
     * Request storage permission with explanation
     */
    public static void requestStoragePermission(Activity activity) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = STORAGE_PERMISSIONS;
        }
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
            showPermissionExplanation(activity, 
                "Storage Permission Required", 
                "This app needs storage access to save and load profile photos and documents.",
                () -> ActivityCompat.requestPermissions(activity, permissions, STORAGE_PERMISSION_CODE));
        } else {
            ActivityCompat.requestPermissions(activity, permissions, STORAGE_PERMISSION_CODE);
        }
    }

    /**
     * Request location permission with explanation
     */
    public static void requestLocationPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionExplanation(activity, 
                "Location Permission Required", 
                "This app needs location access to show nearby alumni and events in your area.",
                () -> ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, LOCATION_PERMISSION_CODE));
        } else {
            ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, LOCATION_PERMISSION_CODE);
        }
    }

    /**
     * Show permission explanation dialog
     */
    private static void showPermissionExplanation(Activity activity, String title, String message, Runnable onPositive) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Grant Permission", (dialog, which) -> onPositive.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Handle permission request results
     */
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case CAMERA_PERMISSION_CODE:
                    return grantResults[0] == PackageManager.PERMISSION_GRANTED;
                case STORAGE_PERMISSION_CODE:
                    return grantResults[0] == PackageManager.PERMISSION_GRANTED;
                case LOCATION_PERMISSION_CODE:
                    return grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }
}
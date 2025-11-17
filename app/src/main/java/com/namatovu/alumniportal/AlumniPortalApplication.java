package com.namatovu.alumniportal;

import android.app.Application;
import com.namatovu.alumniportal.utils.ErrorHandler;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import com.namatovu.alumniportal.utils.ThemeManager;

/**
 * Application class for global initialization
 */
public class AlumniPortalApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply saved theme before any activity is created
        ThemeManager.getInstance(this).applySavedTheme();
        
        // Initialize global error handler
        ErrorHandler.getInstance(this);
        
        // Initialize analytics
        AnalyticsHelper.initialize(this);
        
        // Initialize notification helper
        com.namatovu.alumniportal.utils.NotificationHelper.initialize(this);
        
        // Check for unsaved data from previous crashes
        ErrorHandler errorHandler = ErrorHandler.getInstance(this);
        if (errorHandler.hasUnsavedData()) {
            // Handle recovery from previous crash
            handleCrashRecovery();
        }
    }
    
    private void handleCrashRecovery() {
        // Implement crash recovery logic
        // This could include restoring unsaved data, showing recovery dialog, etc.
        ErrorHandler.getInstance(this).clearUnsavedDataFlag();
    }
}
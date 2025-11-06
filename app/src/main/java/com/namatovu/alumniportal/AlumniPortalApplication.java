package com.namatovu.alumniportal;

import android.app.Application;
import com.namatovu.alumniportal.utils.ErrorHandler;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

/**
 * Application class for global initialization
 */
public class AlumniPortalApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize global error handler
        ErrorHandler.getInstance(this);
        
        // Initialize analytics
        AnalyticsHelper.initialize(this);
        
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
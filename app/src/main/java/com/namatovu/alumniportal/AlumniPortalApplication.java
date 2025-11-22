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
        
        // Automatically scrape and load news from MUST website
        autoLoadNews();
        
        // Check for unsaved data from previous crashes
        ErrorHandler errorHandler = ErrorHandler.getInstance(this);
        if (errorHandler.hasUnsavedData()) {
            // Handle recovery from previous crash
            handleCrashRecovery();
        }
    }
    
    /**
     * Automatically scrape news from MUST website on app startup
     */
    private void autoLoadNews() {
        // Check if news was last updated today
        android.content.SharedPreferences prefs = getSharedPreferences("news_prefs", android.content.Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("last_news_update", 0);
        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        
        // Update news if it's been more than 24 hours
        if (now - lastUpdate > dayInMillis) {
            com.namatovu.alumniportal.services.MUSTNewsScraper.scrapeAndSaveNews(
                new com.namatovu.alumniportal.services.MUSTNewsScraper.ScraperCallback() {
                    @Override
                    public void onSuccess(int newsCount) {
                        android.util.Log.d("AlumniPortalApp", "News updated: " + newsCount + " items");
                        // Update last update time
                        prefs.edit().putLong("last_news_update", System.currentTimeMillis()).apply();
                    }
                    
                    @Override
                    public void onError(String error) {
                        android.util.Log.e("AlumniPortalApp", "Error updating news: " + error);
                    }
                }
            );
        }
    }
    
    private void handleCrashRecovery() {
        // Implement crash recovery logic
        // This could include restoring unsaved data, showing recovery dialog, etc.
        ErrorHandler.getInstance(this).clearUnsavedDataFlag();
    }
}
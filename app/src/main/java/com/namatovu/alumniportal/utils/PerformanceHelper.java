package com.namatovu.alumniportal.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance monitoring and optimization utility
 */
public class PerformanceHelper {
    private static final String TAG = "PerformanceHelper";
    
    private static PerformanceHelper instance;
    private final Map<String, Long> operationStartTimes = new ConcurrentHashMap<>();
    private final Map<String, PerformanceMetrics> performanceMetrics = new ConcurrentHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private PerformanceHelper() {}
    
    public static synchronized PerformanceHelper getInstance() {
        if (instance == null) {
            instance = new PerformanceHelper();
        }
        return instance;
    }
    
    /**
     * Performance metrics holder
     */
    public static class PerformanceMetrics {
        public long totalTime = 0;
        public long averageTime = 0;
        public long minTime = Long.MAX_VALUE;
        public long maxTime = 0;
        public int callCount = 0;
        public long lastCallTime = 0;
        
        void updateMetrics(long duration) {
            callCount++;
            totalTime += duration;
            averageTime = totalTime / callCount;
            minTime = Math.min(minTime, duration);
            maxTime = Math.max(maxTime, duration);
            lastCallTime = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format(
                "Calls: %d, Avg: %dms, Min: %dms, Max: %dms, Total: %dms",
                callCount, averageTime, minTime, maxTime, totalTime
            );
        }
    }
    
    /**
     * Start timing an operation
     */
    public void startTiming(String operationName) {
        operationStartTimes.put(operationName, System.currentTimeMillis());
    }
    
    /**
     * End timing an operation and log results
     */
    public long endTiming(String operationName) {
        Long startTime = operationStartTimes.remove(operationName);
        if (startTime == null) {
            Log.w(TAG, "No start time found for operation: " + operationName);
            return 0;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Update metrics
        PerformanceMetrics metrics = performanceMetrics.computeIfAbsent(
            operationName, k -> new PerformanceMetrics()
        );
        metrics.updateMetrics(duration);
        
        // Log if operation is slow
        if (duration > 1000) { // More than 1 second
            Log.w(TAG, String.format("Slow operation '%s' took %dms", operationName, duration));
            AnalyticsHelper.logPerformanceIssue(operationName, duration);
        } else {
            Log.d(TAG, String.format("Operation '%s' completed in %dms", operationName, duration));
        }
        
        return duration;
    }
    
    /**
     * Get performance metrics for an operation
     */
    public PerformanceMetrics getMetrics(String operationName) {
        return performanceMetrics.get(operationName);
    }
    
    /**
     * Log all performance metrics
     */
    public void logAllMetrics() {
        Log.i(TAG, "=== Performance Metrics ===");
        for (Map.Entry<String, PerformanceMetrics> entry : performanceMetrics.entrySet()) {
            Log.i(TAG, entry.getKey() + ": " + entry.getValue().toString());
        }
        Log.i(TAG, "=========================");
    }
    
    /**
     * Clear performance metrics
     */
    public void clearMetrics() {
        performanceMetrics.clear();
        operationStartTimes.clear();
    }
    
    /**
     * Monitor memory usage
     */
    public static class MemoryMonitor {
        private final Context context;
        private final WeakReference<Activity> activityRef;
        
        public MemoryMonitor(Context context) {
            this.context = context.getApplicationContext();
            this.activityRef = context instanceof Activity ? 
                new WeakReference<>((Activity) context) : null;
        }
        
        public MemoryInfo getMemoryInfo() {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            return new MemoryInfo(
                maxMemory, totalMemory, usedMemory, freeMemory,
                memoryInfo.availMem, memoryInfo.totalMem, memoryInfo.lowMemory
            );
        }
        
        public void logMemoryUsage() {
            MemoryInfo info = getMemoryInfo();
            Log.d(TAG, "Memory Usage: " + info.toString());
            
            // Warn if memory usage is high
            double memoryUsagePercent = (double) info.usedMemory / info.maxMemory * 100;
            if (memoryUsagePercent > 80) {
                Log.w(TAG, String.format("High memory usage: %.1f%%", memoryUsagePercent));
                AnalyticsHelper.logPerformanceIssue("high_memory_usage", (long) memoryUsagePercent);
            }
            
            if (info.systemLowMemory) {
                Log.w(TAG, "System is low on memory!");
                AnalyticsHelper.logPerformanceIssue("system_low_memory", 1);
            }
        }
        
        public boolean shouldTrimMemory() {
            MemoryInfo info = getMemoryInfo();
            double usagePercent = (double) info.usedMemory / info.maxMemory * 100;
            return usagePercent > 75 || info.systemLowMemory;
        }
        
        public void trimMemory() {
            Activity activity = activityRef != null ? activityRef.get() : null;
            if (activity != null) {
                activity.onTrimMemory(ActivityManager.TRIM_MEMORY_RUNNING_LOW);
            }
            
            // Clear image cache if memory is low
            ImageLoadingHelper.clearCache(context);
            
            // Force garbage collection
            System.gc();
            
            Log.i(TAG, "Memory trimmed");
        }
    }
    
    /**
     * Memory information holder
     */
    public static class MemoryInfo {
        public final long maxMemory;
        public final long totalMemory;
        public final long usedMemory;
        public final long freeMemory;
        public final long systemAvailMemory;
        public final long systemTotalMemory;
        public final boolean systemLowMemory;
        
        MemoryInfo(long maxMemory, long totalMemory, long usedMemory, long freeMemory,
                  long systemAvailMemory, long systemTotalMemory, boolean systemLowMemory) {
            this.maxMemory = maxMemory;
            this.totalMemory = totalMemory;
            this.usedMemory = usedMemory;
            this.freeMemory = freeMemory;
            this.systemAvailMemory = systemAvailMemory;
            this.systemTotalMemory = systemTotalMemory;
            this.systemLowMemory = systemLowMemory;
        }
        
        @Override
        public String toString() {
            return String.format(
                "App: %dMB/%dMB (%.1f%%), System: %dMB/%dMB, Low: %s",
                usedMemory / (1024 * 1024), maxMemory / (1024 * 1024),
                (double) usedMemory / maxMemory * 100,
                systemAvailMemory / (1024 * 1024), systemTotalMemory / (1024 * 1024),
                systemLowMemory
            );
        }
    }
    
    /**
     * RecyclerView scroll performance optimizer
     */
    public static class RecyclerViewOptimizer extends RecyclerView.OnScrollListener {
        private final Context context;
        private boolean isPaused = false;
        
        public RecyclerViewOptimizer(Context context) {
            this.context = context;
        }
        
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            
            switch (newState) {
                case RecyclerView.SCROLL_STATE_DRAGGING:
                case RecyclerView.SCROLL_STATE_SETTLING:
                    if (!isPaused) {
                        // Pause image loading during scroll for better performance
                        ImageLoadingHelper.pauseRequests(context);
                        isPaused = true;
                    }
                    break;
                    
                case RecyclerView.SCROLL_STATE_IDLE:
                    if (isPaused) {
                        // Resume image loading when scroll stops
                        ImageLoadingHelper.resumeRequests(context);
                        isPaused = false;
                    }
                    break;
            }
        }
    }
    
    /**
     * Database operation optimizer
     */
    public static class DatabaseOptimizer {
        
        public static void optimizeQuery(String query) {
            PerformanceHelper.getInstance().startTiming("db_query_" + query.hashCode());
        }
        
        public static void queryCompleted(String query) {
            long duration = PerformanceHelper.getInstance().endTiming("db_query_" + query.hashCode());
            
            // Log slow queries
            if (duration > 500) { // More than 500ms
                Log.w(TAG, "Slow database query: " + query + " (" + duration + "ms)");
            }
        }
    }
    
    /**
     * Network operation optimizer
     */
    public static class NetworkOptimizer {
        private static final Map<String, Long> requestStartTimes = new ConcurrentHashMap<>();
        
        public static void startRequest(String requestId) {
            requestStartTimes.put(requestId, System.currentTimeMillis());
            PerformanceHelper.getInstance().startTiming("network_" + requestId);
        }
        
        public static void completeRequest(String requestId, boolean success) {
            Long startTime = requestStartTimes.remove(requestId);
            long duration = PerformanceHelper.getInstance().endTiming("network_" + requestId);
            
            if (startTime != null) {
                if (duration > 5000) { // More than 5 seconds
                    Log.w(TAG, "Slow network request: " + requestId + " (" + duration + "ms)");
                    AnalyticsHelper.logPerformanceIssue("slow_network_request", duration);
                }
                
                if (!success) {
                    AnalyticsHelper.logError("network_request_failed", "Request failed", requestId);
                }
            }
        }
    }
    
    /**
     * UI performance monitor
     */
    public static class UIPerformanceMonitor {
        
        public static void monitorFrameRate(Activity activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // Monitor for frame drops
                activity.getWindow().getDecorView().getViewTreeObserver()
                    .addOnDrawListener(() -> {
                        // Check for frame drops (this is a simplified implementation)
                        long frameTime = System.nanoTime();
                        // Implementation would track frame timing here
                    });
            }
        }
        
        public static void logLayoutTime(String layoutName, long duration) {
            if (duration > 16) { // More than 16ms (60fps threshold)
                Log.w(TAG, "Slow layout: " + layoutName + " (" + duration + "ms)");
                AnalyticsHelper.logPerformanceIssue("slow_layout", duration);
            }
        }
    }
    
    /**
     * Battery optimization helper
     */
    public static class BatteryOptimizer {
        
        public static void optimizeForBattery(Context context) {
            // Reduce background processing
            // Pause non-essential network requests
            // Lower animation frame rates
            Log.i(TAG, "Optimizing for battery usage");
        }
        
        public static void normalPerformance(Context context) {
            // Resume normal operations
            Log.i(TAG, "Resuming normal performance");
        }
    }
}
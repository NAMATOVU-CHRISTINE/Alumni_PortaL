package com.namatovu.alumniportal.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Modern thread management utility to replace AsyncTask
 * Uses ExecutorService with Handler for background operations
 */
public class ThreadManager {
    private static ThreadManager instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    private ThreadManager() {
        executorService = Executors.newFixedThreadPool(4); // 4 background threads
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }
    
    /**
     * Execute background task with UI callback
     */
    public <T> void executeAsync(BackgroundTask<T> backgroundTask, UICallback<T> uiCallback) {
        executorService.execute(() -> {
            try {
                T result = backgroundTask.doInBackground();
                
                // Post result to main thread
                mainHandler.post(() -> {
                    if (uiCallback != null) {
                        uiCallback.onSuccess(result);
                    }
                });
                
            } catch (Exception e) {
                // Post error to main thread
                mainHandler.post(() -> {
                    if (uiCallback != null) {
                        uiCallback.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * Execute simple background task without return value
     */
    public void executeAsync(Runnable backgroundTask, Runnable uiCallback) {
        executorService.execute(() -> {
            try {
                backgroundTask.run();
                
                if (uiCallback != null) {
                    mainHandler.post(uiCallback);
                }
                
            } catch (Exception e) {
                mainHandler.post(() -> {
                    // Handle error on main thread
                    android.util.Log.e("ThreadManager", "Background task failed", e);
                });
            }
        });
    }
    
    /**
     * Post task to main UI thread
     */
    public void runOnMainThread(Runnable task) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            mainHandler.post(task);
        }
    }
    
    /**
     * Shutdown thread pool (call in Application.onTerminate)
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // Interfaces for callbacks
    public interface BackgroundTask<T> {
        T doInBackground() throws Exception;
    }
    
    public interface UICallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}
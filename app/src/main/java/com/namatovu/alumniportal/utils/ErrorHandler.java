package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.namatovu.alumniportal.LoginActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global error handling utility with user-friendly messages, retry mechanisms, and crash reporting
 */
public class ErrorHandler {
    private static final String TAG = "ErrorHandler";
    private static final String PREFS_NAME = "error_handler_prefs";
    private static final String KEY_CRASH_COUNT = "crash_count";
    private static final String KEY_LAST_CRASH_TIME = "last_crash_time";
    private static final long CRASH_RESET_TIME = 24 * 60 * 60 * 1000; // 24 hours
    private static final int MAX_CRASHES_PER_DAY = 5;
    
    private static ErrorHandler instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, RetryAttempt> retryAttempts = new ConcurrentHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private ErrorHandler(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        setupGlobalExceptionHandler();
    }
    
    public static synchronized ErrorHandler getInstance(Context context) {
        if (instance == null) {
            instance = new ErrorHandler(context);
        }
        return instance;
    }
    
    /**
     * Error types for categorization
     */
    public enum ErrorType {
        NETWORK_ERROR,
        AUTHENTICATION_ERROR,
        DATABASE_ERROR,
        VALIDATION_ERROR,
        PERMISSION_ERROR,
        FILE_ERROR,
        UNKNOWN_ERROR
    }
    
    /**
     * Error severity levels
     */
    public enum ErrorSeverity {
        LOW,      // Logging only
        MEDIUM,   // Show user message
        HIGH,     // Show message + retry option
        CRITICAL  // Show message + potential logout/restart
    }
    
    /**
     * Retry attempt tracking
     */
    private static class RetryAttempt {
        int count = 0;
        long lastAttemptTime = 0;
        long backoffDelay = 1000; // Start with 1 second
        
        boolean canRetry() {
            return count < 3 && (System.currentTimeMillis() - lastAttemptTime) > backoffDelay;
        }
        
        void incrementAttempt() {
            count++;
            lastAttemptTime = System.currentTimeMillis();
            backoffDelay = Math.min(backoffDelay * 2, 30000); // Max 30 seconds
        }
    }
    
    /**
     * Error result class
     */
    public static class ErrorResult {
        public final ErrorType type;
        public final ErrorSeverity severity;
        public final String userMessage;
        public final String technicalMessage;
        public final boolean shouldRetry;
        public final boolean shouldLogout;
        public final Throwable exception;
        
        public ErrorResult(ErrorType type, ErrorSeverity severity, String userMessage, 
                          String technicalMessage, boolean shouldRetry, boolean shouldLogout, 
                          Throwable exception) {
            this.type = type;
            this.severity = severity;
            this.userMessage = userMessage;
            this.technicalMessage = technicalMessage;
            this.shouldRetry = shouldRetry;
            this.shouldLogout = shouldLogout;
            this.exception = exception;
        }
    }
    
    /**
     * Retry callback interface
     */
    public interface RetryCallback {
        void retry();
    }
    
    /**
     * Main error handling method
     */
    public ErrorResult handleError(@NonNull Throwable throwable, @Nullable String operation) {
        return handleError(throwable, operation, null);
    }
    
    public ErrorResult handleError(@NonNull Throwable throwable, @Nullable String operation, 
                                  @Nullable RetryCallback retryCallback) {
        ErrorResult result = analyzeError(throwable, operation);
        
        // Log the error
        logError(result, operation);
        
        // Show user feedback based on severity
        showUserFeedback(result, operation, retryCallback);
        
        // Handle critical errors
        if (result.severity == ErrorSeverity.CRITICAL) {
            handleCriticalError(result);
        }
        
        return result;
    }
    
    /**
     * Analyze exception and return appropriate error result
     */
    private ErrorResult analyzeError(Throwable throwable, String operation) {
        if (throwable instanceof FirebaseAuthException) {
            return handleAuthException((FirebaseAuthException) throwable);
        } else if (throwable instanceof FirebaseFirestoreException) {
            return handleFirestoreException((FirebaseFirestoreException) throwable);
        } else if (throwable instanceof FirebaseNetworkException) {
            return handleNetworkException(throwable);
        } else if (isTimeoutException(throwable)) {
            return handleTimeoutException(throwable);
        } else if (isNetworkError(throwable)) {
            return handleNetworkException(throwable);
        } else {
            return handleGenericException(throwable, operation);
        }
    }
    
    private ErrorResult handleAuthException(FirebaseAuthException exception) {
        String errorCode = exception.getErrorCode();
        
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.MEDIUM,
                    "Please enter a valid email address", exception.getMessage(), false, false, exception);
                    
            case "ERROR_WRONG_PASSWORD":
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.MEDIUM,
                    "Incorrect password. Please try again.", exception.getMessage(), false, false, exception);
                    
            case "ERROR_USER_NOT_FOUND":
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.MEDIUM,
                    "No account found with this email address", exception.getMessage(), false, false, exception);
                    
            case "ERROR_USER_DISABLED":
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.HIGH,
                    "Your account has been disabled. Please contact support.", exception.getMessage(), false, false, exception);
                    
            case "ERROR_TOO_MANY_REQUESTS":
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.HIGH,
                    "Too many failed attempts. Please try again later.", exception.getMessage(), true, false, exception);
                    
            case "ERROR_OPERATION_NOT_ALLOWED":
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.CRITICAL,
                    "This operation is not allowed. Please contact support.", exception.getMessage(), false, false, exception);
                    
            case "ERROR_WEAK_PASSWORD":
                return new ErrorResult(ErrorType.VALIDATION_ERROR, ErrorSeverity.MEDIUM,
                    "Password is too weak. Please choose a stronger password.", exception.getMessage(), false, false, exception);
                    
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.MEDIUM,
                    "An account with this email already exists", exception.getMessage(), false, false, exception);
                    
            default:
                return new ErrorResult(ErrorType.AUTHENTICATION_ERROR, ErrorSeverity.HIGH,
                    "Authentication failed. Please try again.", exception.getMessage(), true, false, exception);
        }
    }
    
    private ErrorResult handleFirestoreException(FirebaseFirestoreException exception) {
        FirebaseFirestoreException.Code code = exception.getCode();
        
        switch (code) {
            case PERMISSION_DENIED:
                return new ErrorResult(ErrorType.PERMISSION_ERROR, ErrorSeverity.HIGH,
                    "Access denied. Please check your permissions.", exception.getMessage(), false, true, exception);
                    
            case NOT_FOUND:
                return new ErrorResult(ErrorType.DATABASE_ERROR, ErrorSeverity.MEDIUM,
                    "The requested data was not found", exception.getMessage(), false, false, exception);
                    
            case ALREADY_EXISTS:
                return new ErrorResult(ErrorType.DATABASE_ERROR, ErrorSeverity.MEDIUM,
                    "This data already exists", exception.getMessage(), false, false, exception);
                    
            case RESOURCE_EXHAUSTED:
                return new ErrorResult(ErrorType.DATABASE_ERROR, ErrorSeverity.HIGH,
                    "Service temporarily unavailable. Please try again later.", exception.getMessage(), true, false, exception);
                    
            case FAILED_PRECONDITION:
                return new ErrorResult(ErrorType.DATABASE_ERROR, ErrorSeverity.MEDIUM,
                    "Operation failed due to invalid state", exception.getMessage(), false, false, exception);
                    
            case ABORTED:
                return new ErrorResult(ErrorType.DATABASE_ERROR, ErrorSeverity.HIGH,
                    "Operation was interrupted. Please try again.", exception.getMessage(), true, false, exception);
                    
            case UNAVAILABLE:
                return new ErrorResult(ErrorType.NETWORK_ERROR, ErrorSeverity.HIGH,
                    "Service temporarily unavailable. Please check your connection.", exception.getMessage(), true, false, exception);
                    
            case DEADLINE_EXCEEDED:
                return new ErrorResult(ErrorType.NETWORK_ERROR, ErrorSeverity.HIGH,
                    "Operation timed out. Please try again.", exception.getMessage(), true, false, exception);
                    
            default:
                return new ErrorResult(ErrorType.DATABASE_ERROR, ErrorSeverity.HIGH,
                    "Database operation failed. Please try again.", exception.getMessage(), true, false, exception);
        }
    }
    
    private ErrorResult handleNetworkException(Throwable exception) {
        if (!isNetworkAvailable()) {
            return new ErrorResult(ErrorType.NETWORK_ERROR, ErrorSeverity.HIGH,
                "No internet connection. Please check your network settings.", exception.getMessage(), true, false, exception);
        } else {
            return new ErrorResult(ErrorType.NETWORK_ERROR, ErrorSeverity.HIGH,
                "Network error occurred. Please try again.", exception.getMessage(), true, false, exception);
        }
    }
    
    private ErrorResult handleTimeoutException(Throwable exception) {
        return new ErrorResult(ErrorType.NETWORK_ERROR, ErrorSeverity.HIGH,
            "Request timed out. Please check your connection and try again.", exception.getMessage(), true, false, exception);
    }
    
    private ErrorResult handleGenericException(Throwable exception, String operation) {
        // Check for common patterns
        String message = exception.getMessage();
        if (message != null) {
            message = message.toLowerCase();
            
            if (message.contains("network") || message.contains("connection")) {
                return handleNetworkException(exception);
            } else if (message.contains("permission") || message.contains("denied")) {
                return new ErrorResult(ErrorType.PERMISSION_ERROR, ErrorSeverity.HIGH,
                    "Permission denied. Please check your access rights.", exception.getMessage(), false, false, exception);
            } else if (message.contains("file") || message.contains("storage")) {
                return new ErrorResult(ErrorType.FILE_ERROR, ErrorSeverity.MEDIUM,
                    "File operation failed. Please try again.", exception.getMessage(), true, false, exception);
            }
        }
        
        return new ErrorResult(ErrorType.UNKNOWN_ERROR, ErrorSeverity.HIGH,
            "An unexpected error occurred. Please try again.", exception.getMessage(), true, false, exception);
    }
    
    private void logError(ErrorResult result, String operation) {
        String logMessage = String.format("Error in %s: %s (Type: %s, Severity: %s)", 
            operation != null ? operation : "Unknown operation", 
            result.technicalMessage, 
            result.type, 
            result.severity);
            
        switch (result.severity) {
            case LOW:
                Log.d(TAG, logMessage);
                break;
            case MEDIUM:
                Log.w(TAG, logMessage, result.exception);
                break;
            case HIGH:
            case CRITICAL:
                Log.e(TAG, logMessage, result.exception);
                break;
        }
        
        // Send to analytics/crash reporting
        AnalyticsHelper.logError(result.type.toString(), result.technicalMessage, operation);
        
        // Report crash for critical errors
        if (result.severity == ErrorSeverity.CRITICAL) {
            reportCrash(result, operation);
        }
    }
    
    private void showUserFeedback(ErrorResult result, String operation, RetryCallback retryCallback) {
        if (result.severity == ErrorSeverity.LOW) {
            return; // No user feedback for low severity
        }
        
        mainHandler.post(() -> {
            switch (result.severity) {
                case MEDIUM:
                    Toast.makeText(context, result.userMessage, Toast.LENGTH_LONG).show();
                    break;
                    
                case HIGH:
                    if (result.shouldRetry && retryCallback != null) {
                        showRetrySnackbar(result.userMessage, operation, retryCallback);
                    } else {
                        Toast.makeText(context, result.userMessage, Toast.LENGTH_LONG).show();
                    }
                    break;
                    
                case CRITICAL:
                    Toast.makeText(context, result.userMessage + " The app may need to restart.", Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
    
    private void showRetrySnackbar(String message, String operation, RetryCallback retryCallback) {
        // This would typically be shown in the current activity
        // For now, we'll use a toast with retry logic
        Toast.makeText(context, message + " Tap to retry.", Toast.LENGTH_LONG).show();
        
        // Implement automatic retry with exponential backoff
        scheduleRetry(operation, retryCallback);
    }
    
    private void scheduleRetry(String operation, RetryCallback retryCallback) {
        if (operation == null) return;
        
        RetryAttempt attempt = retryAttempts.get(operation);
        if (attempt == null) {
            attempt = new RetryAttempt();
            retryAttempts.put(operation, attempt);
        }
        
        if (attempt.canRetry()) {
            attempt.incrementAttempt();
            
            mainHandler.postDelayed(() -> {
                try {
                    retryCallback.retry();
                } catch (Exception e) {
                    handleError(e, operation + "_retry");
                }
            }, attempt.backoffDelay);
        } else {
            retryAttempts.remove(operation);
        }
    }
    
    private void handleCriticalError(ErrorResult result) {
        incrementCrashCount();
        
        if (shouldForceLogout(result)) {
            forceLogout();
        }
        
        if (getCrashCount() >= MAX_CRASHES_PER_DAY) {
            // Disable certain features or force app restart
            Toast.makeText(context, "Multiple critical errors detected. Please restart the app.", Toast.LENGTH_LONG).show();
        }
    }
    
    private boolean shouldForceLogout(ErrorResult result) {
        return result.shouldLogout || 
               result.type == ErrorType.AUTHENTICATION_ERROR && result.severity == ErrorSeverity.CRITICAL ||
               result.type == ErrorType.PERMISSION_ERROR && result.severity == ErrorSeverity.CRITICAL;
    }
    
    private void forceLogout() {
        // Clear user session and redirect to login
        try {
            SecurityHelper.clearUserSession(context);
            
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to force logout", e);
        }
    }
    
    private void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            Log.e(TAG, "Uncaught exception in thread " + thread.getName(), exception);
            
            // Log to crash reporting
            reportCrash(new ErrorResult(ErrorType.UNKNOWN_ERROR, ErrorSeverity.CRITICAL,
                "App crashed unexpectedly", exception.getMessage(), false, false, exception), "UncaughtException");
            
            // Try to save any critical data
            saveCriticalData();
            
            // Let the system handle the crash
            System.exit(1);
        });
    }
    
    private void reportCrash(ErrorResult result, String operation) {
        try {
            Map<String, String> crashData = new HashMap<>();
            crashData.put("operation", operation != null ? operation : "unknown");
            crashData.put("error_type", result.type.toString());
            crashData.put("error_message", result.technicalMessage != null ? result.technicalMessage : "null");
            crashData.put("stack_trace", getStackTrace(result.exception));
            crashData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            // Send to crash reporting service (Firebase Crashlytics would be ideal)
            AnalyticsHelper.logCrash(crashData);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to report crash", e);
        }
    }
    
    private void saveCriticalData() {
        try {
            // Save any unsaved user data or state
            prefs.edit()
                .putLong("last_crash_time", System.currentTimeMillis())
                .putBoolean("has_unsaved_data", true)
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save critical data", e);
        }
    }
    
    // Utility methods
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isNetworkError(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && (
            message.contains("UnknownHostException") ||
            message.contains("ConnectException") ||
            message.contains("SocketTimeoutException") ||
            message.contains("IOException")
        );
    }
    
    private boolean isTimeoutException(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && (
            message.contains("timeout") ||
            message.contains("TimeoutException") ||
            throwable.getClass().getSimpleName().contains("Timeout")
        );
    }
    
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return "null";
        
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Failed to get stack trace: " + e.getMessage();
        }
    }
    
    private void incrementCrashCount() {
        long lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastCrashTime > CRASH_RESET_TIME) {
            // Reset crash count after 24 hours
            prefs.edit()
                .putInt(KEY_CRASH_COUNT, 1)
                .putLong(KEY_LAST_CRASH_TIME, currentTime)
                .apply();
        } else {
            int crashCount = prefs.getInt(KEY_CRASH_COUNT, 0);
            prefs.edit()
                .putInt(KEY_CRASH_COUNT, crashCount + 1)
                .putLong(KEY_LAST_CRASH_TIME, currentTime)
                .apply();
        }
    }
    
    private int getCrashCount() {
        long lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastCrashTime > CRASH_RESET_TIME) {
            return 0; // Reset after 24 hours
        }
        
        return prefs.getInt(KEY_CRASH_COUNT, 0);
    }
    
    /**
     * Clear retry attempts for an operation
     */
    public void clearRetryAttempts(String operation) {
        retryAttempts.remove(operation);
    }
    
    /**
     * Check if there's unsaved data from a previous crash
     */
    public boolean hasUnsavedData() {
        return prefs.getBoolean("has_unsaved_data", false);
    }
    
    /**
     * Clear unsaved data flag
     */
    public void clearUnsavedDataFlag() {
        prefs.edit().remove("has_unsaved_data").apply();
    }
}
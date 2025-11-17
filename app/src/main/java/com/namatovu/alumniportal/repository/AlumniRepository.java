package com.namatovu.alumniportal.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.namatovu.alumniportal.database.AlumniDatabase;
import com.namatovu.alumniportal.database.entities.EventEntity;
import com.namatovu.alumniportal.database.entities.JobEntity;
import com.namatovu.alumniportal.database.entities.UserEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class demonstrating proper use of ExecutorService for database operations
 * Handles all data operations with proper threading
 */
public class AlumniRepository {
    
    private static final String TAG = "AlumniRepository";
    private static AlumniRepository instance;
    
    private final AlumniDatabase database;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    private AlumniRepository(Context context) {
        database = AlumniDatabase.getInstance(context.getApplicationContext());
        executorService = Executors.newFixedThreadPool(4); // Thread pool for database operations
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized AlumniRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AlumniRepository(context);
        }
        return instance;
    }
    
    // ==================== USER OPERATIONS ====================
    
    public void insertUser(UserEntity user, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                database.userDao().insertUser(user);
                Log.d(TAG, "User inserted: " + user.getUserId());
                notifySuccess(listener);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting user", e);
                notifyError(listener, e);
            }
        });
    }
    
    public void getUserById(String userId, OnUserLoadedListener listener) {
        executorService.execute(() -> {
            try {
                UserEntity user = database.userDao().getUserById(userId);
                mainHandler.post(() -> listener.onUserLoaded(user));
            } catch (Exception e) {
                Log.e(TAG, "Error loading user", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    public void getAllUsers(OnUsersLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<UserEntity> users = database.userDao().getAllUsers();
                mainHandler.post(() -> listener.onUsersLoaded(users));
            } catch (Exception e) {
                Log.e(TAG, "Error loading users", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    public void updateUserOnlineStatus(String userId, boolean isOnline, long lastSeen) {
        executorService.execute(() -> {
            try {
                database.userDao().updateUserOnlineStatus(userId, isOnline, lastSeen);
                Log.d(TAG, "User online status updated: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating user status", e);
            }
        });
    }
    
    // ==================== JOB OPERATIONS ====================
    
    public void insertJob(JobEntity job, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                database.jobDao().insertJob(job);
                Log.d(TAG, "Job inserted: " + job.getJobId());
                notifySuccess(listener);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting job", e);
                notifyError(listener, e);
            }
        });
    }
    
    public void getAllJobs(OnJobsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<JobEntity> jobs = database.jobDao().getAllJobs();
                mainHandler.post(() -> listener.onJobsLoaded(jobs));
            } catch (Exception e) {
                Log.e(TAG, "Error loading jobs", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    public void getSavedJobs(OnJobsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<JobEntity> jobs = database.jobDao().getSavedJobs();
                mainHandler.post(() -> listener.onJobsLoaded(jobs));
            } catch (Exception e) {
                Log.e(TAG, "Error loading saved jobs", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    public void searchJobs(String query, OnJobsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<JobEntity> jobs = database.jobDao().searchJobs(query);
                mainHandler.post(() -> listener.onJobsLoaded(jobs));
            } catch (Exception e) {
                Log.e(TAG, "Error searching jobs", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    public void updateJobSavedStatus(String jobId, boolean isSaved) {
        executorService.execute(() -> {
            try {
                database.jobDao().updateJobSavedStatus(jobId, isSaved);
                Log.d(TAG, "Job saved status updated: " + jobId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating job saved status", e);
            }
        });
    }
    
    // ==================== EVENT OPERATIONS ====================
    
    public void insertEvent(EventEntity event, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                database.eventDao().insertEvent(event);
                Log.d(TAG, "Event inserted: " + event.getEventId());
                notifySuccess(listener);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting event", e);
                notifyError(listener, e);
            }
        });
    }
    
    public void getAllEvents(OnEventsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<EventEntity> events = database.eventDao().getAllEvents();
                mainHandler.post(() -> listener.onEventsLoaded(events));
            } catch (Exception e) {
                Log.e(TAG, "Error loading events", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    public void getUpcomingEvents(OnEventsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                List<EventEntity> events = database.eventDao().getUpcomingEvents(currentTime);
                mainHandler.post(() -> listener.onEventsLoaded(events));
            } catch (Exception e) {
                Log.e(TAG, "Error loading upcoming events", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    public void searchEvents(String query, OnEventsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<EventEntity> events = database.eventDao().searchEvents(query);
                mainHandler.post(() -> listener.onEventsLoaded(events));
            } catch (Exception e) {
                Log.e(TAG, "Error searching events", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
    
    // ==================== UTILITY METHODS ====================
    
    public void clearAllData(OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                database.clearAllTables();
                Log.d(TAG, "All data cleared");
                notifySuccess(listener);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing data", e);
                notifyError(listener, e);
            }
        });
    }
    
    private void notifySuccess(OnOperationCompleteListener listener) {
        if (listener != null) {
            mainHandler.post(listener::onSuccess);
        }
    }
    
    private void notifyError(OnOperationCompleteListener listener, Exception e) {
        if (listener != null) {
            mainHandler.post(() -> listener.onError(e));
        }
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // ==================== CALLBACK INTERFACES ====================
    
    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(Exception e);
    }
    
    public interface OnUserLoadedListener {
        void onUserLoaded(UserEntity user);
        void onError(Exception e);
    }
    
    public interface OnUsersLoadedListener {
        void onUsersLoaded(List<UserEntity> users);
        void onError(Exception e);
    }
    
    public interface OnJobsLoadedListener {
        void onJobsLoaded(List<JobEntity> jobs);
        void onError(Exception e);
    }
    
    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<EventEntity> events);
        void onError(Exception e);
    }
}

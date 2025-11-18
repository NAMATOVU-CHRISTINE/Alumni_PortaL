package com.namatovu.alumniportal.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.database.AlumniDatabase;
import com.namatovu.alumniportal.database.entities.EventEntity;
import com.namatovu.alumniportal.database.entities.JobEntity;
import com.namatovu.alumniportal.database.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background Service for syncing data from Firebase to local Room database
 * Demonstrates: Service, ExecutorService for threading, Room database operations
 */
public class DataSyncService extends Service {
    
    private static final String TAG = "DataSyncService";
    private static final String CHANNEL_ID = "DataSyncChannel";
    private static final int NOTIFICATION_ID = 1001;
    
    private ExecutorService executorService;
    private Handler mainHandler;
    private FirebaseFirestore db;
    private AlumniDatabase localDb;
    private boolean isSyncing = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DataSyncService created");
        
        // Initialize ExecutorService for background threading
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize databases
        db = FirebaseFirestore.getInstance();
        localDb = AlumniDatabase.getInstance(getApplicationContext());
        
        // Create notification channel
        createNotificationChannel();
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification("Initializing sync..."));
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "DataSyncService started");
        
        if (!isSyncing) {
            isSyncing = true;
            startDataSync();
        }
        
        // Service will be restarted if killed by system
        return START_STICKY;
    }
    
    private void startDataSync() {
        updateNotification("Syncing data from cloud...");
        
        // Sync users in background thread
        executorService.execute(() -> {
            Log.d(TAG, "Starting user sync...");
            syncUsers();
        });
        
        // Sync jobs in background thread
        executorService.execute(() -> {
            Log.d(TAG, "Starting job sync...");
            syncJobs();
        });
        
        // Sync events in background thread
        executorService.execute(() -> {
            Log.d(TAG, "Starting event sync...");
            syncEvents();
        });
        
        // Sync mentors in background thread
        executorService.execute(() -> {
            Log.d(TAG, "Starting mentor sync...");
            syncMentors();
        });
    }
    
    private void syncUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<UserEntity> users = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    UserEntity user = new UserEntity();
                    user.userId = document.getId();
                    user.fullName = document.getString("fullName");
                    user.email = document.getString("email");
                    user.profileImageUrl = document.getString("profileImageUrl");
                    user.graduationYear = document.getString("graduationYear");
                    user.major = document.getString("major");
                    user.currentJob = document.getString("currentJob");
                    user.company = document.getString("company");
                    user.lastSynced = System.currentTimeMillis();
                    
                    users.add(user);
                }
                
                // Insert into Room database in background thread
                executorService.execute(() -> {
                    localDb.userDao().insertUsers(users);
                    Log.d(TAG, "Synced " + users.size() + " users to local database");
                    checkSyncComplete();
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error syncing users", e);
                checkSyncComplete();
            });
    }
    
    private void syncJobs() {
        db.collection("jobs")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<JobEntity> jobs = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    JobEntity job = new JobEntity();
                    job.setJobId(document.getId());
                    job.setTitle(document.getString("title"));
                    job.setCompany(document.getString("company"));
                    job.setDescription(document.getString("description"));
                    job.setLocation(document.getString("location"));
                    job.setJobType(document.getString("jobType"));
                    job.setExperienceLevel(document.getString("experienceLevel"));
                    job.setSalary(document.getString("salary"));
                    job.setApplicationUrl(document.getString("applicationUrl"));
                    job.setPosterId(document.getString("posterId"));
                    job.setPosterName(document.getString("posterName"));
                    
                    Long postedDate = document.getLong("postedDate");
                    job.setPostedDate(postedDate != null ? postedDate : 0);
                    
                    Long deadline = document.getLong("deadline");
                    job.setDeadline(deadline != null ? deadline : 0);
                    
                    job.setLastSyncTime(System.currentTimeMillis());
                    jobs.add(job);
                }
                
                // Insert into Room database in background thread
                executorService.execute(() -> {
                    localDb.jobDao().insertJobs(jobs);
                    Log.d(TAG, "Synced " + jobs.size() + " jobs to local database");
                    checkSyncComplete();
                });
            })
            .addOnFailureListener(e -> {
                if (e instanceof FirebaseFirestoreException) {
                    FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                    if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Permission denied for jobs collection - skipping sync");
                    } else {
                        Log.e(TAG, "Error syncing jobs", e);
                    }
                } else {
                    Log.e(TAG, "Error syncing jobs", e);
                }
                checkSyncComplete();
            });
    }
    
    private void syncEvents() {
        db.collection("events")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<EventEntity> events = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    EventEntity event = new EventEntity();
                    event.setEventId(document.getId());
                    event.setTitle(document.getString("title"));
                    event.setDescription(document.getString("description"));
                    event.setLocation(document.getString("location"));
                    event.setImageUrl(document.getString("imageUrl"));
                    event.setOrganizerId(document.getString("organizerId"));
                    event.setOrganizerName(document.getString("organizerName"));
                    event.setCategory(document.getString("category"));
                    
                    // Handle eventDate - could be Long or Timestamp
                    Long eventDate = document.getLong("eventDate");
                    if (eventDate == null && document.contains("eventDate")) {
                        com.google.firebase.Timestamp timestamp = document.getTimestamp("eventDate");
                        eventDate = timestamp != null ? timestamp.toDate().getTime() : 0L;
                    }
                    event.setEventDate(eventDate != null ? eventDate : 0);
                    
                    // Handle createdAt - could be Long or Timestamp
                    Long createdAt = null;
                    if (document.contains("createdAt")) {
                        try {
                            createdAt = document.getLong("createdAt");
                        } catch (RuntimeException e) {
                            com.google.firebase.Timestamp timestamp = document.getTimestamp("createdAt");
                            createdAt = timestamp != null ? timestamp.toDate().getTime() : 0L;
                        }
                    }
                    event.setCreatedAt(createdAt != null ? createdAt : 0);
                    
                    Long attendeeCount = document.getLong("attendeeCount");
                    event.setAttendeeCount(attendeeCount != null ? attendeeCount.intValue() : 0);
                    
                    event.setLastSyncTime(System.currentTimeMillis());
                    events.add(event);
                }
                
                // Insert into Room database in background thread
                executorService.execute(() -> {
                    localDb.eventDao().insertEvents(events);
                    Log.d(TAG, "Synced " + events.size() + " events to local database");
                    checkSyncComplete();
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error syncing events", e);
                checkSyncComplete();
            });
    }
    
    private int syncCompletedCount = 0;
    private final int TOTAL_SYNC_TASKS = 4;
    
    private void syncMentors() {
        db.collection("mentors")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<com.namatovu.alumniportal.database.entities.MentorEntity> mentors = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    com.namatovu.alumniportal.database.entities.MentorEntity mentor = 
                        new com.namatovu.alumniportal.database.entities.MentorEntity();
                    mentor.setMentorId(document.getId());
                    mentor.setFullName(document.getString("fullName"));
                    mentor.setEmail(document.getString("email"));
                    mentor.setProfileImageUrl(document.getString("profileImageUrl"));
                    mentor.setCurrentJob(document.getString("currentJob"));
                    mentor.setCompany(document.getString("company"));
                    mentor.setExpertise(document.getString("expertise"));
                    mentor.setCategory(document.getString("category"));
                    mentor.setBio(document.getString("bio"));
                    mentor.setGraduationYear(document.getString("graduationYear"));
                    mentor.setCourse(document.getString("course"));
                    
                    Long yearsExp = document.getLong("yearsOfExperience");
                    mentor.setYearsOfExperience(yearsExp != null ? yearsExp.intValue() : 0);
                    
                    Long menteeCount = document.getLong("menteeCount");
                    mentor.setMenteeCount(menteeCount != null ? menteeCount.intValue() : 0);
                    
                    Double rating = document.getDouble("rating");
                    mentor.setRating(rating != null ? rating : 0.0);
                    
                    Boolean available = document.getBoolean("isAvailable");
                    mentor.setAvailable(available != null && available);
                    
                    mentor.setLastSyncTime(System.currentTimeMillis());
                    mentors.add(mentor);
                }
                
                // Insert into Room database in background thread
                executorService.execute(() -> {
                    localDb.mentorDao().insertMentors(mentors);
                    Log.d(TAG, "Synced " + mentors.size() + " mentors to local database");
                    checkSyncComplete();
                });
            })
            .addOnFailureListener(e -> {
                if (e instanceof FirebaseFirestoreException) {
                    FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                    if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Permission denied for mentors collection - skipping sync");
                    } else {
                        Log.e(TAG, "Error syncing mentors", e);
                    }
                } else {
                    Log.e(TAG, "Error syncing mentors", e);
                }
                checkSyncComplete();
            });
    }
    
    private synchronized void checkSyncComplete() {
        syncCompletedCount++;
        
        if (syncCompletedCount >= TOTAL_SYNC_TASKS) {
            Log.d(TAG, "All sync tasks completed");
            updateNotification("Sync completed successfully");
            
            // Stop service after a delay
            mainHandler.postDelayed(() -> {
                isSyncing = false;
                syncCompletedCount = 0;
                stopForeground(true);
                stopSelf();
            }, 2000);
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Data Sync Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background data synchronization");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alumni Portal")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }
    
    private void updateNotification(String message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(message));
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DataSyncService destroyed");
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                // Wait a bit for tasks to complete
                if (!executorService.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

package com.namatovu.alumniportal.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.namatovu.alumniportal.database.AlumniDatabase;
import com.namatovu.alumniportal.database.entities.ChatMessageEntity;
import com.namatovu.alumniportal.database.entities.EventEntity;
import com.namatovu.alumniportal.database.entities.JobPostingEntity;
import com.namatovu.alumniportal.database.entities.UserEntity;
import com.namatovu.alumniportal.models.AlumniEvent;
import com.namatovu.alumniportal.models.ChatMessage;
import com.namatovu.alumniportal.models.JobPosting;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Worker class that performs background synchronization
 */
public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private static final String KEY_SYNC_TYPE = "sync_type";
    private static final int SYNC_TIMEOUT_SECONDS = 300; // 5 minutes
    
    private AlumniDatabase database;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUserId;
    
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        
        database = AlumniDatabase.getInstance(context);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting sync work");
        
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, skipping sync");
            return Result.failure();
        }
        
        try {
            // Get sync type from input data
            String syncTypeString = getInputData().getString(KEY_SYNC_TYPE);
            SyncManager.SyncDataType syncType = syncTypeString != null ? 
                    SyncManager.SyncDataType.valueOf(syncTypeString) : 
                    SyncManager.SyncDataType.ALL;
            
            boolean syncSuccess = performSync(syncType);
            
            if (syncSuccess) {
                Log.d(TAG, "Sync completed successfully");
                AnalyticsHelper.logEvent("sync_completed", "sync_type", syncType.name());
                return Result.success();
            } else {
                Log.w(TAG, "Sync completed with errors");
                return Result.retry();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during sync", e);
            AnalyticsHelper.logEvent("sync_failed", "error", e.getMessage());
            return Result.failure();
        }
    }
    
    /**
     * Perform synchronization based on type
     */
    private boolean performSync(SyncManager.SyncDataType syncType) {
        boolean success = true;
        
        try {
            switch (syncType) {
                case CHAT_MESSAGES:
                    success = syncChatMessages();
                    break;
                case USERS:
                    success = syncUsers();
                    break;
                case JOB_POSTINGS:
                    success = syncJobPostings();
                    break;
                case EVENTS:
                    success = syncEvents();
                    break;
                case ALL:
                default:
                    success = syncUsers() && 
                             syncJobPostings() && 
                             syncEvents() && 
                             syncChatMessages();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in performSync", e);
            success = false;
        }
        
        return success;
    }
    
    /**
     * Sync chat messages
     */
    private boolean syncChatMessages() {
        Log.d(TAG, "Syncing chat messages");
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {true};
            
            // Get user's chats first
            firestore.collection("chats")
                    .whereArrayContains("participantIds", currentUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot chatDoc : task.getResult().getDocuments()) {
                                    String chatId = chatDoc.getId();
                                    syncMessagesForChat(chatId);
                                }
                            } else {
                                Log.e(TAG, "Error fetching chats", task.getException());
                                success[0] = false;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing chats", e);
                            success[0] = false;
                        } finally {
                            latch.countDown();
                        }
                    });
            
            // Wait for completion
            latch.await(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return success[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing chat messages", e);
            return false;
        }
    }
    
    /**
     * Sync messages for a specific chat
     */
    private void syncMessagesForChat(String chatId) {
        try {
            // Get last sync timestamp for this chat
            long lastSyncTime = getLastSyncTime("chat_messages_" + chatId);
            
            firestore.collection("chats").document(chatId)
                    .collection("messages")
                    .whereGreaterThan("timestamp", lastSyncTime)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<ChatMessageEntity> messages = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            try {
                                ChatMessage chatMessage = doc.toObject(ChatMessage.class);
                                if (chatMessage != null) {
                                    chatMessage.setMessageId(doc.getId());
                                    ChatMessageEntity entity = convertToEntity(chatMessage);
                                    entity.syncStatus = "synced";
                                    messages.add(entity);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting message", e);
                            }
                        }
                        
                        if (!messages.isEmpty()) {
                            database.chatMessageDao().insertMessages(messages);
                            updateLastSyncTime("chat_messages_" + chatId, System.currentTimeMillis());
                            Log.d(TAG, "Synced " + messages.size() + " messages for chat " + chatId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error syncing messages for chat " + chatId, e));
                    
        } catch (Exception e) {
            Log.e(TAG, "Error in syncMessagesForChat", e);
        }
    }
    
    /**
     * Sync users
     */
    private boolean syncUsers() {
        Log.d(TAG, "Syncing users");
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {true};
            
            long lastSyncTime = getLastSyncTime("users");
            
            firestore.collection("users")
                    .whereGreaterThan("updatedAt", lastSyncTime)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful() && task.getResult() != null) {
                                List<UserEntity> users = new ArrayList<>();
                                
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    try {
                                        User user = doc.toObject(User.class);
                                        if (user != null) {
                                            user.setUserId(doc.getId());
                                            UserEntity entity = convertToEntity(user);
                                            entity.syncStatus = "synced";
                                            users.add(entity);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error converting user", e);
                                    }
                                }
                                
                                if (!users.isEmpty()) {
                                    database.userDao().insertUsers(users);
                                    updateLastSyncTime("users", System.currentTimeMillis());
                                    Log.d(TAG, "Synced " + users.size() + " users");
                                }
                            } else {
                                Log.e(TAG, "Error fetching users", task.getException());
                                success[0] = false;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing users", e);
                            success[0] = false;
                        } finally {
                            latch.countDown();
                        }
                    });
            
            latch.await(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return success[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing users", e);
            return false;
        }
    }
    
    /**
     * Sync job postings
     */
    private boolean syncJobPostings() {
        Log.d(TAG, "Syncing job postings");
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {true};
            
            long lastSyncTime = getLastSyncTime("job_postings");
            
            firestore.collection("job_postings")
                    .whereGreaterThan("updatedAt", lastSyncTime)
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful() && task.getResult() != null) {
                                List<JobPostingEntity> jobs = new ArrayList<>();
                                
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    try {
                                        JobPosting job = doc.toObject(JobPosting.class);
                                        if (job != null) {
                                            job.setJobId(doc.getId());
                                            JobPostingEntity entity = convertToEntity(job);
                                            entity.syncStatus = "synced";
                                            jobs.add(entity);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error converting job posting", e);
                                    }
                                }
                                
                                if (!jobs.isEmpty()) {
                                    database.jobPostingDao().insertJobs(jobs);
                                    updateLastSyncTime("job_postings", System.currentTimeMillis());
                                    Log.d(TAG, "Synced " + jobs.size() + " job postings");
                                }
                            } else {
                                Log.e(TAG, "Error fetching job postings", task.getException());
                                success[0] = false;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing job postings", e);
                            success[0] = false;
                        } finally {
                            latch.countDown();
                        }
                    });
            
            latch.await(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return success[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing job postings", e);
            return false;
        }
    }
    
    /**
     * Sync events
     */
    private boolean syncEvents() {
        Log.d(TAG, "Syncing events");
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {true};
            
            long lastSyncTime = getLastSyncTime("events");
            
            firestore.collection("alumni_events")
                    .whereGreaterThan("updatedAt", lastSyncTime)
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful() && task.getResult() != null) {
                                List<EventEntity> events = new ArrayList<>();
                                
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    try {
                                        AlumniEvent event = doc.toObject(AlumniEvent.class);
                                        if (event != null) {
                                            event.setEventId(doc.getId());
                                            EventEntity entity = convertToEntity(event);
                                            entity.syncStatus = "synced";
                                            events.add(entity);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error converting event", e);
                                    }
                                }
                                
                                if (!events.isEmpty()) {
                                    database.eventDao().insertEvents(events);
                                    updateLastSyncTime("events", System.currentTimeMillis());
                                    Log.d(TAG, "Synced " + events.size() + " events");
                                }
                            } else {
                                Log.e(TAG, "Error fetching events", task.getException());
                                success[0] = false;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing events", e);
                            success[0] = false;
                        } finally {
                            latch.countDown();
                        }
                    });
            
            latch.await(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return success[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing events", e);
            return false;
        }
    }
    
    // Helper methods for conversion
    private ChatMessageEntity convertToEntity(ChatMessage message) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.messageId = message.getMessageId();
        entity.chatId = message.getChatId();
        entity.senderId = message.getSenderId();
        entity.senderName = message.getSenderName();
        entity.content = message.getContent();
        entity.messageType = message.getMessageType();
        entity.fileUrl = message.getFileUrl();
        entity.fileName = message.getFileName();
        entity.fileSize = message.getFileSize();
        entity.timestamp = message.getTimestamp();
        entity.readStatus = message.getReadStatus();
        entity.readTimestamp = message.getReadTimestamp();
        entity.replyToMessageId = message.getReplyToMessageId();
        entity.isEdited = message.isEdited();
        entity.editTimestamp = message.getEditTimestamp();
        entity.isDeleted = message.isDeleted();
        entity.deleteTimestamp = message.getDeleteTimestamp();
        entity.updatedAt = System.currentTimeMillis();
        return entity;
    }
    
    private UserEntity convertToEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.userId = user.getUserId();
        entity.email = user.getEmail();
        entity.fullName = user.getFullName();
        entity.profileImageUrl = user.getProfileImageUrl();
        entity.bio = user.getBio();
        entity.graduationYear = user.getGraduationYear();
        entity.major = user.getMajor();
        entity.currentJobTitle = user.getCurrentJobTitle();
        entity.currentCompany = user.getCurrentCompany();
        entity.location = user.getLocation();
        entity.skills = user.getSkills();
        entity.linkedinUrl = user.getLinkedinUrl();
        entity.githubUrl = user.getGithubUrl();
        entity.websiteUrl = user.getWebsiteUrl();
        entity.isMentor = user.isMentor();
        entity.mentorExpertise = user.getMentorExpertise();
        entity.isOnline = user.isOnline();
        entity.lastSeen = user.getLastSeen();
        entity.privacyProfileVisibility = user.getPrivacyProfileVisibility();
        entity.privacyContactVisibility = user.getPrivacyContactVisibility();
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();
        entity.lastSync = System.currentTimeMillis();
        return entity;
    }
    
    private JobPostingEntity convertToEntity(JobPosting job) {
        JobPostingEntity entity = new JobPostingEntity();
        entity.jobId = job.getJobId();
        entity.company = job.getCompany();
        entity.position = job.getPosition();
        entity.description = job.getDescription();
        entity.requirements = job.getRequirements();
        entity.location = job.getLocation();
        entity.jobType = job.getJobType();
        entity.experienceLevel = job.getExperienceLevel();
        entity.salaryRange = job.getSalaryRange();
        entity.applicationDeadline = job.getApplicationDeadline();
        entity.applicationUrl = job.getApplicationUrl();
        entity.postedByUserId = job.getPostedByUserId();
        entity.postedByName = job.getPostedByName();
        entity.postedAt = job.getPostedAt();
        entity.isActive = job.isActive();
        entity.tags = job.getTags();
        entity.createdAt = job.getCreatedAt();
        entity.updatedAt = job.getUpdatedAt();
        entity.lastSync = System.currentTimeMillis();
        return entity;
    }
    
    private EventEntity convertToEntity(AlumniEvent event) {
        EventEntity entity = new EventEntity();
        entity.eventId = event.getEventId();
        entity.title = event.getTitle();
        entity.description = event.getDescription();
        entity.category = event.getCategory();
        entity.dateTime = event.getDateTime();
        entity.endDateTime = event.getEndDateTime();
        entity.location = event.getLocation();
        entity.venue = event.getVenue();
        entity.isVirtual = event.isVirtual();
        entity.meetingLink = event.getMeetingLink();
        entity.maxAttendees = event.getMaxAttendees();
        entity.currentAttendees = event.getCurrentAttendees();
        entity.registrationDeadline = event.getRegistrationDeadline();
        entity.isPaid = event.isPaid();
        entity.price = event.getPrice();
        entity.currency = event.getCurrency();
        entity.organizerId = event.getOrganizerId();
        entity.organizerName = event.getOrganizerName();
        entity.contactEmail = event.getContactEmail();
        entity.contactPhone = event.getContactPhone();
        entity.imageUrl = event.getImageUrl();
        entity.tags = event.getTags();
        entity.isActive = event.isActive();
        entity.createdAt = event.getCreatedAt();
        entity.updatedAt = event.getUpdatedAt();
        entity.lastSync = System.currentTimeMillis();
        return entity;
    }
    
    private long getLastSyncTime(String key) {
        return getApplicationContext()
                .getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                .getLong("last_sync_" + key, 0);
    }
    
    private void updateLastSyncTime(String key, long timestamp) {
        getApplicationContext()
                .getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_sync_" + key, timestamp)
                .apply();
    }
    
    /**
     * Create input data for specific sync type
     */
    public static Data createInputData(SyncManager.SyncDataType syncType) {
        return new Data.Builder()
                .putString(KEY_SYNC_TYPE, syncType.name())
                .build();
    }
}
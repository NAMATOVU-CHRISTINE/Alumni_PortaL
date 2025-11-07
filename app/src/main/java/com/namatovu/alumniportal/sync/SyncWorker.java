package com.namatovu.alumniportal.sync;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.namatovu.alumniportal.database.AppDatabase;
import com.namatovu.alumniportal.database.entities.ChatMessageEntity;
import com.namatovu.alumniportal.database.entities.EventEntity;
import com.namatovu.alumniportal.database.entities.JobPostingEntity;
import com.namatovu.alumniportal.database.entities.UserEntity;
import com.namatovu.alumniportal.models.AlumniEvent;
import com.namatovu.alumniportal.models.ChatMessage;
import com.namatovu.alumniportal.models.JobPosting;
import com.namatovu.alumniportal.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private static final String KEY_SYNC_TYPE = "SYNC_TYPE";
    private static final int SYNC_TIMEOUT_SECONDS = 60;
    private final FirebaseFirestore firestore;
    private final AppDatabase database;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        firestore = FirebaseFirestore.getInstance();
        database = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        String syncType = getInputData().getString(KEY_SYNC_TYPE);
        if (syncType == null) {
            Log.e(TAG, "Sync type not specified. Aborting.");
            return ListenableWorker.Result.failure();
        }

        Log.d(TAG, "Starting sync for type: " + syncType);

        boolean success;
        try {
            switch (SyncManager.SyncDataType.valueOf(syncType)) {
                case ALL:
                    success = syncAll();
                    break;
                case CHATS:
                    success = syncAllChats();
                    break;
                case USERS:
                    success = syncUsers();
                    break;
                case JOBS:
                    success = syncJobPostings();
                    break;
                case EVENTS:
                    success = syncEvents();
                    break;
                default:
                    Log.w(TAG, "Unknown sync type: " + syncType);
                    success = false;
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Sync failed with exception", e);
            success = false;
        }

        Log.d(TAG, "Sync finished with result: " + (success ? "SUCCESS" : "FAILURE"));
        return success ? ListenableWorker.Result.success() : ListenableWorker.Result.failure();
    }

    private boolean syncAll() {
        boolean usersSynced = syncUsers();
        boolean jobsSynced = syncJobPostings();
        boolean eventsSynced = syncEvents();
        boolean chatsSynced = syncAllChats();
        return usersSynced && jobsSynced && eventsSynced && chatsSynced;
    }

    private boolean syncAllChats() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Log.w(TAG, "No user logged in, cannot sync chats.");
            return false;
        }

        Log.d(TAG, "Starting to sync all chats for user: " + currentUserId);
        final boolean[] overallSuccess = {true};
        CountDownLatch latch = new CountDownLatch(1);

        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<DocumentSnapshot> chatDocs = task.getResult().getDocuments();
                    if (chatDocs.isEmpty()) {
                        Log.d(TAG, "No chats found for the current user.");
                        latch.countDown();
                        return;
                    }

                    CountDownLatch chatLatch = new CountDownLatch(chatDocs.size());
                    for (DocumentSnapshot chatDoc : chatDocs) {
                        String chatId = chatDoc.getId();
                        syncMessagesForChat(chatId, new SyncCallback() {
                            @Override
                            public void onSyncComplete(boolean success) {
                                if (!success) {
                                    overallSuccess[0] = false;
                                }
                                chatLatch.countDown();
                            }
                        });
                    }

                    try {
                        if (!chatLatch.await(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                            Log.e(TAG, "Timeout waiting for individual chat syncs to complete.");
                            overallSuccess[0] = false;
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Chat sync interrupted", e);
                        Thread.currentThread().interrupt();
                        overallSuccess[0] = false;
                    }
                } else {
                    Log.e(TAG, "Error fetching chat list", task.getException());
                    overallSuccess[0] = false;
                }
                latch.countDown();
            });

        try {
            if (!latch.await(SYNC_TIMEOUT_SECONDS + 5, TimeUnit.SECONDS)) {
                Log.e(TAG, "Timeout waiting for the chat list fetch to complete.");
                return false;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Main chat sync interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }

        return overallSuccess[0];
    }

    private void syncMessagesForChat(String chatId, SyncCallback callback) {
        if (chatId == null || chatId.isEmpty()) {
            Log.w(TAG, "Chat ID is null or empty, cannot sync.");
            callback.onSyncComplete(false);
            return;
        }
        
        Log.d(TAG, "Syncing messages for chat ID: " + chatId);

        long lastSyncTime = getLastSyncTime("chat_" + chatId);
        Query query = firestore.collection("chats").document(chatId).collection("messages")
                             .whereGreaterThan("timestamp", lastSyncTime)
                             .orderBy("timestamp", Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    List<ChatMessageEntity> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        try {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            if (message != null) {
                                message.setMessageId(doc.getId());
                                ChatMessageEntity entity = convertToEntity(message);
                                entity.syncStatus = "synced";
                                messages.add(entity);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting message", e);
                        }
                    }
                    
                    if (!messages.isEmpty()) {
                        database.chatMessageDao().insertMessages(messages);
                        long newLastSyncTime = messages.get(messages.size() - 1).timestamp;
                        updateLastSyncTime("chat_" + chatId, newLastSyncTime);
                        Log.d(TAG, "Synced " + messages.size() + " messages for chat " + chatId);
                    }
                    callback.onSyncComplete(true);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing messages for chat " + chatId, e);
                    callback.onSyncComplete(false);
                }
            } else {
                Log.e(TAG, "Error fetching messages for chat " + chatId, task.getException());
                callback.onSyncComplete(false);
            }
        });
    }
    
    interface SyncCallback {
        void onSyncComplete(boolean success);
    }

    /**
     * Sync all users
     */
    private boolean syncUsers() {
        Log.d(TAG, "Syncing users");
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {true};
            
            long lastSyncTime = getLastSyncTime("users");
            
            firestore.collection("users")
                    .whereGreaterThan("lastActive", lastSyncTime)
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
        entity.content = message.getMessageText();
        entity.messageType = message.getMessageType();
        entity.fileUrl = message.getFileUrl();
        entity.fileName = message.getFileName();
        entity.fileSize = message.getFileSizeBytes();
        entity.timestamp = message.getTimestamp();
        entity.readStatus = message.isRead();
        entity.readTimestamp = message.getTimestamp(); // Assuming read at sync time for simplicity
        entity.replyToMessageId = message.getReplyToMessageId();
        entity.isEdited = message.isEdited();
        entity.editTimestamp = message.getEditedAt();
        entity.isDeleted = message.isDeleted();
        entity.deleteTimestamp = message.getDeletedAt();
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
        entity.currentJobTitle = user.getCurrentJob();
        entity.currentCompany = user.getCompany();
        entity.location = user.getLocation();
        entity.skills = user.getSkillsAsString();
        entity.linkedinUrl = user.getSocialLink("linkedin");
        entity.githubUrl = user.getSocialLink("github");
        entity.websiteUrl = user.getSocialLink("website");
        entity.isMentor = user.getPrivacySetting("allowMentorRequests");
        entity.mentorExpertise = ""; // Not directly available in the new model
        entity.isOnline = user.getLastActive() > (System.currentTimeMillis() - 5 * 60 * 1000); // 5 minutes
        entity.lastSeen = user.getLastActive();
        entity.privacyProfileVisibility = user.getPrivacySetting("showInDirectory");
        entity.privacyContactVisibility = user.getPrivacySetting("showEmail"); // Or a combination
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = System.currentTimeMillis(); // New field in User model is lastActive
        entity.lastSync = System.currentTimeMillis();
        return entity;
    }
    
    private JobPostingEntity convertToEntity(JobPosting job) {
        JobPostingEntity entity = new JobPostingEntity();
        entity.jobId = job.getJobId();
        entity.company = job.getCompany();
        entity.position = job.getTitle();
        entity.description = job.getDescription();
        entity.requirements = String.join(", ", job.getRequirements());
        entity.location = job.getLocation();
        entity.jobType = job.getEmploymentType();
        entity.experienceLevel = job.getExperienceLevel();
        entity.salaryRange = job.getSalaryRange();
        entity.applicationDeadline = job.getDeadline();
        entity.applicationUrl = job.getApplicationUrl();
        entity.postedByUserId = job.getPostedBy();
        entity.postedByName = ""; // Not available in new model
        entity.postedAt = job.getPostedAt();
        entity.isActive = job.isActive();
        entity.tags = String.join(", ", job.getTags());
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
        entity.dateTime = event.getStartDateTime();
        entity.endDateTime = event.getEndDateTime();
        entity.location = event.getLocation();
        entity.venue = event.getVenue();
        entity.isVirtual = event.isVirtual();
        entity.meetingLink = event.getMeetingUrl();
        entity.maxAttendees = event.getMaxCapacity();
        entity.currentAttendees = event.getAttendees().size();
        entity.registrationDeadline = event.getRegistrationDeadline();
        entity.isPaid = event.isPaid();
        entity.price = event.getCost();
        entity.currency = event.getCurrency();
        entity.organizerId = event.getOrganizerId();
        entity.organizerName = event.getOrganizerName();
        entity.contactEmail = event.getContactEmail();
        entity.contactPhone = event.getContactPhone();
        entity.imageUrl = event.getImageUrl();
        entity.tags = String.join(", ", event.getTags());
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

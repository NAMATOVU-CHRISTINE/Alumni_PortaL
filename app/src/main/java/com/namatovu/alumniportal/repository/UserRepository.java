package com.namatovu.alumniportal.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.database.AlumniDatabase;
import com.namatovu.alumniportal.database.dao.UserDao;
import com.namatovu.alumniportal.database.entities.UserEntity;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.sync.SyncManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for User data with offline-first approach
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    
    private static UserRepository instance;
    private UserDao userDao;
    private FirebaseFirestore firestore;
    private SyncManager syncManager;
    private Executor executor;
    
    private UserRepository(Context context) {
        AlumniDatabase database = AlumniDatabase.getInstance(context);
        userDao = database.userDao();
        firestore = FirebaseFirestore.getInstance();
        syncManager = SyncManager.getInstance(context);
        executor = Executors.newFixedThreadPool(4);
    }
    
    public static UserRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }
    
    /**
     * Get all users with offline-first approach
     */
    public LiveData<List<User>> getAllUsers() {
        MediatorLiveData<List<User>> result = new MediatorLiveData<>();
        
        // First, get data from local database
        LiveData<List<UserEntity>> localData = userDao.getAllUsers();
        result.addSource(localData, userEntities -> {
            if (userEntities != null) {
                List<User> users = convertToUsers(userEntities);
                result.setValue(users);
            }
        });
        
        // Then, sync with remote if network is available
        if (syncManager.isNetworkAvailable()) {
            syncManager.forceSyncDataType(SyncManager.SyncDataType.USERS);
        }
        
        return result;
    }
    
    /**
     * Get user by ID with offline-first approach
     */
    public LiveData<User> getUserById(String userId) {
        MediatorLiveData<User> result = new MediatorLiveData<>();
        
        // Get from local database first
        LiveData<UserEntity> localData = userDao.getUserById(userId);
        result.addSource(localData, userEntity -> {
            if (userEntity != null) {
                result.setValue(convertToUser(userEntity));
            }
        });
        
        // Sync with remote if network is available
        if (syncManager.isNetworkAvailable()) {
            executor.execute(() -> {
                try {
                    firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null) {
                                        user.setUserId(documentSnapshot.getId());
                                        saveUserLocally(user);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error fetching user from Firestore", e));
                } catch (Exception e) {
                    Log.e(TAG, "Error in getUserById sync", e);
                }
            });
        }
        
        return result;
    }
    
    /**
     * Search users with offline-first approach
     */
    public LiveData<List<User>> searchUsers(String query) {
        MediatorLiveData<List<User>> result = new MediatorLiveData<>();
        
        executor.execute(() -> {
            try {
                List<UserEntity> localResults = userDao.searchUsers(query);
                List<User> users = convertToUsers(localResults);
                result.postValue(users);
            } catch (Exception e) {
                Log.e(TAG, "Error searching users locally", e);
            }
        });
        
        return result;
    }
    
    /**
     * Get users by graduation year
     */
    public LiveData<List<User>> getUsersByGraduationYear(int year) {
        MediatorLiveData<List<User>> result = new MediatorLiveData<>();
        
        executor.execute(() -> {
            try {
                List<UserEntity> localResults = userDao.getUsersByGraduationYear(year);
                List<User> users = convertToUsers(localResults);
                result.postValue(users);
            } catch (Exception e) {
                Log.e(TAG, "Error getting users by graduation year", e);
            }
        });
        
        return result;
    }
    
    /**
     * Get users by major
     */
    public LiveData<List<User>> getUsersByMajor(String major) {
        MediatorLiveData<List<User>> result = new MediatorLiveData<>();
        
        executor.execute(() -> {
            try {
                List<UserEntity> localResults = userDao.getUsersByMajor(major);
                List<User> users = convertToUsers(localResults);
                result.postValue(users);
            } catch (Exception e) {
                Log.e(TAG, "Error getting users by major", e);
            }
        });
        
        return result;
    }
    
    /**
     * Get mentors
     */
    public LiveData<List<User>> getMentors() {
        MediatorLiveData<List<User>> result = new MediatorLiveData<>();
        
        executor.execute(() -> {
            try {
                List<UserEntity> localResults = userDao.getMentors();
                List<User> users = convertToUsers(localResults);
                result.postValue(users);
            } catch (Exception e) {
                Log.e(TAG, "Error getting mentors", e);
            }
        });
        
        return result;
    }
    
    /**
     * Save user with sync
     */
    public void saveUser(User user) {
        executor.execute(() -> {
            try {
                // Save locally first
                saveUserLocally(user);
                
                // Sync to remote if network is available
                if (syncManager.isNetworkAvailable()) {
                    firestore.collection("users").document(user.getUserId())
                            .set(user.toMap())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User saved to Firestore successfully");
                                updateUserSyncStatus(user.getUserId(), "synced");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving user to Firestore", e);
                                updateUserSyncStatus(user.getUserId(), "failed");
                            });
                } else {
                    updateUserSyncStatus(user.getUserId(), "pending");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving user", e);
            }
        });
    }
    
    /**
     * Delete user
     */
    public void deleteUser(String userId) {
        executor.execute(() -> {
            try {
                // Delete locally
                userDao.deleteUserById(userId);
                
                // Delete from remote if network is available
                if (syncManager.isNetworkAvailable()) {
                    firestore.collection("users").document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User deleted from Firestore"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error deleting user from Firestore", e));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user", e);
            }
        });
    }
    
    /**
     * Update user online status
     */
    public void updateOnlineStatus(String userId, boolean isOnline) {
        executor.execute(() -> {
            try {
                long lastSeen = System.currentTimeMillis();
                
                // Update locally
                userDao.updateOnlineStatus(userId, isOnline, lastSeen);
                
                // Update remotely if network is available
                if (syncManager.isNetworkAvailable()) {
                    firestore.collection("users").document(userId)
                            .update("isOnline", isOnline, "lastSeen", lastSeen)
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating online status", e));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating online status", e);
            }
        });
    }
    
    // Helper methods
    private void saveUserLocally(User user) {
        try {
            UserEntity entity = convertToEntity(user);
            entity.syncStatus = syncManager.isNetworkAvailable() ? "synced" : "pending";
            userDao.insertUser(entity);
        } catch (Exception e) {
            Log.e(TAG, "Error saving user locally", e);
        }
    }
    
    private void updateUserSyncStatus(String userId, String syncStatus) {
        try {
            userDao.updateSyncStatus(userId, syncStatus, System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Error updating user sync status", e);
        }
    }
    
    private User convertToUser(UserEntity entity) {
        User user = new User();
        user.setUserId(entity.userId);
        user.setEmail(entity.email);
        user.setFullName(entity.fullName);
        user.setProfileImageUrl(entity.profileImageUrl);
        user.setBio(entity.bio);
        user.setGraduationYear(entity.graduationYear);
        user.setMajor(entity.major);
        user.setCurrentJobTitle(entity.currentJobTitle);
        user.setCurrentCompany(entity.currentCompany);
        user.setLocation(entity.location);
        user.setSkills(entity.skills);
        user.setLinkedinUrl(entity.linkedinUrl);
        user.setGithubUrl(entity.githubUrl);
        user.setWebsiteUrl(entity.websiteUrl);
        user.setMentor(entity.isMentor);
        user.setMentorExpertise(entity.mentorExpertise);
        user.setOnline(entity.isOnline);
        user.setLastSeen(entity.lastSeen);
        user.setPrivacyProfileVisibility(entity.privacyProfileVisibility);
        user.setPrivacyContactVisibility(entity.privacyContactVisibility);
        user.setCreatedAt(entity.createdAt);
        user.setUpdatedAt(entity.updatedAt);
        return user;
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
    
    private List<User> convertToUsers(List<UserEntity> entities) {
        List<User> users = new ArrayList<>();
        for (UserEntity entity : entities) {
            users.add(convertToUser(entity));
        }
        return users;
    }
}
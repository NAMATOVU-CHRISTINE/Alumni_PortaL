package com.namatovu.alumniportal.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.database.AppDatabase;
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
        AppDatabase database = AppDatabase.getInstance(context);
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
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving user to Firestore", e);
                            });
                } else {
                    // TODO: Mark for later sync
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving user", e);
            }
        });
    }
    
    // Helper methods
    private void saveUserLocally(User user) {
        try {
            UserEntity entity = convertToEntity(user);
            userDao.insertUsers(new ArrayList<UserEntity>() {{ add(entity); }});
        } catch (Exception e) {
            Log.e(TAG, "Error saving user locally", e);
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
        user.setCurrentJob(entity.currentJobTitle);
        user.setCompany(entity.currentCompany);
        user.setLocation(entity.location);
        user.setSkillsFromString(entity.skills);
        user.setSocialLink("linkedin", entity.linkedinUrl);
        user.setSocialLink("github", entity.githubUrl);
        user.setSocialLink("website", entity.websiteUrl);
        user.setPrivacySetting("allowMentorRequests", entity.isMentor);
        // user.setMentorExpertise(entity.mentorExpertise);
        user.setPrivacySetting("showInDirectory", entity.privacyProfileVisibility);
        user.setPrivacySetting("showEmail", entity.privacyContactVisibility);
        user.setCreatedAt(entity.createdAt);
        user.updateLastActive();
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
        entity.currentJobTitle = user.getCurrentJob();
        entity.currentCompany = user.getCompany();
        entity.location = user.getLocation();
        entity.skills = user.getSkillsAsString();
        entity.linkedinUrl = user.getSocialLink("linkedin");
        entity.githubUrl = user.getSocialLink("github");
        entity.websiteUrl = user.getSocialLink("website");
        entity.isMentor = user.getPrivacySetting("allowMentorRequests");
        // entity.mentorExpertise = user.getMentorExpertise();
        entity.privacyProfileVisibility = user.getPrivacySetting("showInDirectory");
        entity.privacyContactVisibility = user.getPrivacySetting("showEmail");
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getLastActive();
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
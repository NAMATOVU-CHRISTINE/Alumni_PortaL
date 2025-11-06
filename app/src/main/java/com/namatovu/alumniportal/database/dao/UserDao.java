package com.namatovu.alumniportal.database.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.namatovu.alumniportal.database.entities.UserEntity;
import java.util.List;

/**
 * Data Access Object for users
 */
@Dao
public interface UserDao {
    
    @Query("SELECT * FROM users ORDER BY full_name ASC")
    LiveData<List<UserEntity>> getAllUsers();
    
    @Query("SELECT * FROM users ORDER BY full_name ASC")
    List<UserEntity> getAllUsersSync();
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<UserEntity> getUserById(String userId);
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    UserEntity getUserByIdSync(String userId);
    
    @Query("SELECT * FROM users WHERE email = :email")
    UserEntity getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE graduation_year = :year ORDER BY full_name ASC")
    List<UserEntity> getUsersByGraduationYear(int year);
    
    @Query("SELECT * FROM users WHERE major = :major ORDER BY full_name ASC")
    List<UserEntity> getUsersByMajor(String major);
    
    @Query("SELECT * FROM users WHERE is_mentor = 1 ORDER BY full_name ASC")
    List<UserEntity> getMentors();
    
    @Query("SELECT * FROM users WHERE full_name LIKE '%' || :query || '%' OR bio LIKE '%' || :query || '%' OR skills LIKE '%' || :query || '%' ORDER BY full_name ASC")
    List<UserEntity> searchUsers(String query);
    
    @Query("SELECT * FROM users WHERE sync_status = :syncStatus")
    List<UserEntity> getUsersBySyncStatus(String syncStatus);
    
    @Query("SELECT * FROM users WHERE sync_status = 'pending' OR sync_status = 'failed'")
    List<UserEntity> getUnsyncedUsers();
    
    @Query("SELECT * FROM users WHERE last_sync < :timestamp")
    List<UserEntity> getUsersNeedingSync(long timestamp);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<UserEntity> users);
    
    @Update
    void updateUser(UserEntity user);
    
    @Query("UPDATE users SET sync_status = :syncStatus, last_sync = :lastSync WHERE user_id = :userId")
    void updateSyncStatus(String userId, String syncStatus, long lastSync);
    
    @Query("UPDATE users SET is_online = :isOnline, last_seen = :lastSeen WHERE user_id = :userId")
    void updateOnlineStatus(String userId, boolean isOnline, long lastSeen);
    
    @Delete
    void deleteUser(UserEntity user);
    
    @Query("DELETE FROM users WHERE user_id = :userId")
    void deleteUserById(String userId);
    
    @Query("DELETE FROM users")
    void deleteAllUsers();
}
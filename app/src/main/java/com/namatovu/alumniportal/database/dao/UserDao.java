package com.namatovu.alumniportal.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.namatovu.alumniportal.database.entities.UserEntity;
import java.util.List;

@Dao
public interface UserDao {
    
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    UserEntity getUserById(String userId);
    
    @Query("SELECT * FROM users WHERE major = :major")
    List<UserEntity> getUsersByMajor(String major);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<UserEntity> users);
    
    @Update
    void updateUser(UserEntity user);
    
    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteUser(String userId);
    
    @Query("DELETE FROM users")
    void deleteAllUsers();
    
    // Insert or update method
    default void insertOrUpdate(UserEntity user) {
        UserEntity existing = getUserById(user.userId);
        if (existing != null) {
            updateUser(user);
        } else {
            insertUser(user);
        }
    }
    
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
    
    @Query("SELECT * FROM users WHERE lastSynced > :timestamp")
    List<UserEntity> getRecentlySyncedUsers(long timestamp);
}
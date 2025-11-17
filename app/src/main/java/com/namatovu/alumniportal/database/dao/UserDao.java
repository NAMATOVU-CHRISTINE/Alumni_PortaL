package com.namatovu.alumniportal.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.namatovu.alumniportal.database.entities.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<UserEntity> users);
    
    @Update
    void updateUser(UserEntity user);
    
    @Delete
    void deleteUser(UserEntity user);
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    UserEntity getUserById(String userId);
    
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    List<UserEntity> getAllUsers();
    
    @Query("SELECT * FROM users WHERE isOnline = 1 ORDER BY fullName ASC")
    List<UserEntity> getOnlineUsers();
    
    @Query("SELECT * FROM users WHERE graduationYear = :year ORDER BY fullName ASC")
    List<UserEntity> getUsersByGraduationYear(String year);
    
    @Query("SELECT * FROM users WHERE course LIKE '%' || :course || '%' ORDER BY fullName ASC")
    List<UserEntity> getUsersByCourse(String course);
    
    @Query("DELETE FROM users")
    void deleteAllUsers();
    
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
    
    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE userId = :userId")
    void updateUserOnlineStatus(String userId, boolean isOnline, long lastSeen);
}

package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String userId = "";
    
    public String fullName;
    public String email;
    public String major;
    public String graduationYear;
    public String currentJob;
    public String company;
    public String profileImageUrl;
    public long lastSynced;
    
    // Default constructor
    public UserEntity() {}
    
    // Constructor with required fields
    public UserEntity(@NonNull String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.lastSynced = System.currentTimeMillis();
    }
}
package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

/**
 * Room entity for offline user profile storage
 */
@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    public String userId;
    
    @ColumnInfo(name = "email")
    public String email;
    
    @ColumnInfo(name = "full_name")
    public String fullName;
    
    @ColumnInfo(name = "profile_image_url")
    public String profileImageUrl;
    
    @ColumnInfo(name = "bio")
    public String bio;
    
    @ColumnInfo(name = "graduation_year")
    public int graduationYear;
    
    @ColumnInfo(name = "major")
    public String major;
    
    @ColumnInfo(name = "current_job_title")
    public String currentJobTitle;
    
    @ColumnInfo(name = "current_company")
    public String currentCompany;
    
    @ColumnInfo(name = "location")
    public String location;
    
    @ColumnInfo(name = "skills")
    public String skills;
    
    @ColumnInfo(name = "linkedin_url")
    public String linkedinUrl;
    
    @ColumnInfo(name = "github_url")
    public String githubUrl;
    
    @ColumnInfo(name = "website_url")
    public String websiteUrl;
    
    @ColumnInfo(name = "is_mentor")
    public boolean isMentor;
    
    @ColumnInfo(name = "mentor_expertise")
    public String mentorExpertise;
    
    @ColumnInfo(name = "is_online")
    public boolean isOnline;
    
    @ColumnInfo(name = "last_seen")
    public long lastSeen;
    
    @ColumnInfo(name = "privacy_profile_visibility")
    public String privacyProfileVisibility;
    
    @ColumnInfo(name = "privacy_contact_visibility")
    public String privacyContactVisibility;
    
    @ColumnInfo(name = "created_at")
    public long createdAt;
    
    @ColumnInfo(name = "updated_at")
    public long updatedAt;
    
    @ColumnInfo(name = "sync_status")
    public String syncStatus; // "synced", "pending", "failed"
    
    @ColumnInfo(name = "last_sync")
    public long lastSync;
    
    // Default constructor
    public UserEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending";
        this.lastSync = 0;
    }
}
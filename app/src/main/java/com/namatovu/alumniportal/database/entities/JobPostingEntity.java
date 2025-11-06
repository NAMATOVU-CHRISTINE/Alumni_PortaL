package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

/**
 * Room entity for offline job posting storage
 */
@Entity(tableName = "job_postings")
public class JobPostingEntity {
    @PrimaryKey
    public String jobId;
    
    @ColumnInfo(name = "company")
    public String company;
    
    @ColumnInfo(name = "position")
    public String position;
    
    @ColumnInfo(name = "description")
    public String description;
    
    @ColumnInfo(name = "requirements")
    public String requirements;
    
    @ColumnInfo(name = "location")
    public String location;
    
    @ColumnInfo(name = "job_type")
    public String jobType;
    
    @ColumnInfo(name = "experience_level")
    public String experienceLevel;
    
    @ColumnInfo(name = "salary_range")
    public String salaryRange;
    
    @ColumnInfo(name = "application_deadline")
    public long applicationDeadline;
    
    @ColumnInfo(name = "application_url")
    public String applicationUrl;
    
    @ColumnInfo(name = "posted_by_user_id")
    public String postedByUserId;
    
    @ColumnInfo(name = "posted_by_name")
    public String postedByName;
    
    @ColumnInfo(name = "posted_at")
    public long postedAt;
    
    @ColumnInfo(name = "is_active")
    public boolean isActive;
    
    @ColumnInfo(name = "tags")
    public String tags;
    
    @ColumnInfo(name = "created_at")
    public long createdAt;
    
    @ColumnInfo(name = "updated_at")
    public long updatedAt;
    
    @ColumnInfo(name = "sync_status")
    public String syncStatus;
    
    @ColumnInfo(name = "last_sync")
    public long lastSync;
    
    // Default constructor
    public JobPostingEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending";
        this.lastSync = 0;
        this.isActive = true;
    }
}
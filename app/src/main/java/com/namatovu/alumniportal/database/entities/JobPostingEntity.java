package com.namatovu.alumniportal.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "job_postings")
public class JobPostingEntity {
    @PrimaryKey
    @NonNull
    public String jobId;
    public String company;
    public String position;
    public String description;
    public String requirements;
    public String location;
    public String jobType;
    public String experienceLevel;
    public String salaryRange;
    public long applicationDeadline;
    public String applicationUrl;
    public String postedByUserId;
    public String postedByName;
    public long postedAt;
    public boolean isActive;
    public String tags;
    public long createdAt;
    public long updatedAt;
    public long lastSync;
    public String syncStatus;
}

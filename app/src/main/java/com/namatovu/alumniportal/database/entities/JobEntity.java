package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "jobs")
public class JobEntity {
    @PrimaryKey
    @NonNull
    private String jobId;
    
    private String title;
    private String company;
    private String description;
    private String location;
    private String jobType; // Full-time, Part-time, Contract
    private String experienceLevel;
    private String salary;
    private String applicationUrl;
    private String posterId;
    private String posterName;
    private long postedDate;
    private long deadline;
    private boolean isSaved;
    private long lastSyncTime;

    public JobEntity() {
        this.lastSyncTime = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getJobId() { return jobId; }
    public void setJobId(@NonNull String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public String getPosterId() { return posterId; }
    public void setPosterId(String posterId) { this.posterId = posterId; }

    public String getPosterName() { return posterName; }
    public void setPosterName(String posterName) { this.posterName = posterName; }

    public long getPostedDate() { return postedDate; }
    public void setPostedDate(long postedDate) { this.postedDate = postedDate; }

    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }

    public long getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(long lastSyncTime) { this.lastSyncTime = lastSyncTime; }
}

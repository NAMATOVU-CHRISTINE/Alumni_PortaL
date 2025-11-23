package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "mentors")
public class MentorEntity {
    @PrimaryKey
    @NonNull
    private String mentorId;
    
    private String fullName;
    private String email;
    private String profileImageUrl;
    private String currentJob;
    private String company;
    private String expertise; // e.g., "Software Engineering, Data Science"
    private String category; // "Technology", "Business", "Healthcare", etc.
    private String bio;
    private String graduationYear;
    private String course;
    private int yearsOfExperience;
    private int menteeCount;
    private double rating;
    private boolean isAvailable;
    private long lastSyncTime;

    public MentorEntity() {
        this.lastSyncTime = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getMentorId() { return mentorId; }
    public void setMentorId(@NonNull String mentorId) { this.mentorId = mentorId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getCurrentJob() { return currentJob; }
    public void setCurrentJob(String currentJob) { this.currentJob = currentJob; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGraduationYear() { return graduationYear; }
    public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public int getMenteeCount() { return menteeCount; }
    public void setMenteeCount(int menteeCount) { this.menteeCount = menteeCount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public long getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(long lastSyncTime) { this.lastSyncTime = lastSyncTime; }
}

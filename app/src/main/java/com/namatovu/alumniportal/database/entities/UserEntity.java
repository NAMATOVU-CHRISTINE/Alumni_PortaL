package com.namatovu.alumniportal.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String userId;
    public String email;
    public String fullName;
    public String profileImageUrl;
    public String bio;
    public String graduationYear;
    public String major;
    public String currentJobTitle;
    public String currentCompany;
    public String location;
    public String skills;
    public String linkedinUrl;
    public String githubUrl;
    public String websiteUrl;
    public boolean isMentor;
    public String mentorExpertise;
    public boolean isOnline;
    public long lastSeen;
    public boolean privacyProfileVisibility;
    public boolean privacyContactVisibility;
    public long createdAt;
    public long updatedAt;
    public long lastSync;
    public String syncStatus;
}

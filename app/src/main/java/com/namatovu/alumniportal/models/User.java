package com.namatovu.alumniportal.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User model class for Alumni Portal
 * Represents user data structure in Firestore
 */
public class User {
    private String userId;
    private String fullName;
    private String username;
    private String email;
    private String personalEmail;
    private String studentId;
    private String profileImageUrl;
    private String bio;
    private String major;
    private String graduationYear;
    private String currentJob;
    private String company;
    private String location;
    private String phoneNumber;
    private List<String> skills;
    private boolean isVerified;
    private boolean isAlumni;
    // userType values:
    // "student" - Current students
    // "alumni" - Graduated students
    // "staff" - Faculty/staff members
    private String userType;
    private String role; // "user", "admin"
    private long createdAt;
    private long lastActive;
    private boolean emailVerified;
    private Map<String, Object> socialLinks;
    private Map<String, Object> privacySettings;
    
    // Transient fields (not stored in Firestore)
    private transient boolean isConnected; // Whether current user is connected to this user
    private transient String connectionStatus; // "connected", "pending", "not_connected"

    // Default constructor required for Firebase
    public User() {
        this.skills = new ArrayList<>();
        this.socialLinks = new HashMap<>();
        this.privacySettings = new HashMap<>();
        this.isVerified = false;
        this.isAlumni = false;
        this.userType = "student";
        this.role = "user";
        this.createdAt = System.currentTimeMillis();
        this.lastActive = System.currentTimeMillis();
        initializeDefaultPrivacySettings();
    }

    // Constructor for user creation
    public User(String fullName, String username, String email, String personalEmail, String studentId) {
        this();
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.personalEmail = personalEmail;
        this.studentId = studentId;
    }

    private void initializeDefaultPrivacySettings() {
        privacySettings.put("showEmail", false);
        privacySettings.put("showPhone", false);
        privacySettings.put("showLocation", true);
        privacySettings.put("showCurrentJob", true);
        privacySettings.put("showProfileImage", true); // Profile images always visible
        privacySettings.put("allowMentorRequests", true);
        privacySettings.put("showInDirectory", true);
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPersonalEmail() { return personalEmail; }
    public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getGraduationYear() { return graduationYear; }
    public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }

    public String getCurrentJob() { return currentJob; }
    public void setCurrentJob(String currentJob) { this.currentJob = currentJob; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isAlumni() { return isAlumni; }
    public void setAlumni(boolean alumni) { isAlumni = alumni; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public Map<String, Object> getSocialLinks() { return socialLinks; }
    public void setSocialLinks(Map<String, Object> socialLinks) { this.socialLinks = socialLinks; }

    public Map<String, Object> getPrivacySettings() { return privacySettings; }
    public void setPrivacySettings(Map<String, Object> privacySettings) { this.privacySettings = privacySettings; }
    
    // Connection status getters and setters (transient fields)
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
    
    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    // Helper methods
    public void addSkill(String skill) {
        if (skills == null) {
            skills = new ArrayList<>();
        }
        if (!skills.contains(skill.trim()) && !skill.trim().isEmpty()) {
            skills.add(skill.trim());
        }
    }

    public void removeSkill(String skill) {
        if (skills != null) {
            skills.remove(skill);
        }
    }

    public String getSkillsAsString() {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return String.join(", ", skills);
    }

    public void setSkillsFromString(String skillsString) {
        if (skills == null) {
            skills = new ArrayList<>();
        }
        skills.clear();
        
        if (skillsString != null && !skillsString.trim().isEmpty()) {
            String[] skillArray = skillsString.split(",");
            for (String skill : skillArray) {
                String trimmedSkill = skill.trim();
                if (!trimmedSkill.isEmpty()) {
                    skills.add(trimmedSkill);
                }
            }
        }
    }

    public void updateLastActive() {
        this.lastActive = System.currentTimeMillis();
    }

    public boolean getPrivacySetting(String setting) {
        if (privacySettings != null && privacySettings.containsKey(setting)) {
            Object value = privacySettings.get(setting);
            return value instanceof Boolean ? (Boolean) value : false;
        }
        return false;
    }

    public void setPrivacySetting(String setting, boolean value) {
        if (privacySettings == null) {
            privacySettings = new HashMap<>();
        }
        privacySettings.put(setting, value);
    }

    public void setSocialLink(String platform, String url) {
        if (socialLinks == null) {
            socialLinks = new HashMap<>();
        }
        if (url != null && !url.trim().isEmpty()) {
            socialLinks.put(platform, url.trim());
        } else {
            socialLinks.remove(platform);
        }
    }

    public String getSocialLink(String platform) {
        if (socialLinks != null && socialLinks.containsKey(platform)) {
            Object value = socialLinks.get(platform);
            return value instanceof String ? (String) value : "";
        }
        return "";
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("fullName", fullName);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("personalEmail", personalEmail);
        userMap.put("studentId", studentId);
        userMap.put("profileImageUrl", profileImageUrl);
        userMap.put("bio", bio);
        userMap.put("major", major);
        userMap.put("graduationYear", graduationYear);
        userMap.put("currentJob", currentJob);
        userMap.put("company", company);
        userMap.put("location", location);
        userMap.put("phoneNumber", phoneNumber);
        userMap.put("skills", skills);
        userMap.put("isVerified", isVerified);
        userMap.put("isAlumni", isAlumni);
        userMap.put("userType", userType);
        userMap.put("role", role);
        userMap.put("createdAt", createdAt);
        userMap.put("lastActive", lastActive);
        userMap.put("socialLinks", socialLinks);
        userMap.put("privacySettings", privacySettings);
        return userMap;
    }
}
package com.namatovu.alumniportal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private String name;
    private String email;
    private String username;
    private String studentID;
    private String bio;
    private String career;
    private String profileImageUrl;
    private List<String> skills;

    // Public no-argument constructor is REQUIRED for Firestore deserialization.
    public User() {}

    // Constructor for new user creation during signup
    public User(String name, String email, String username, String studentID) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.studentID = studentID;
    }

    // --- BULLETPROOF GETTERS ---
    // These getters ensure that no part of the app will ever get a NullPointerException
    // when accessing user data, even if the fields are missing in Firestore.

    public String getName() { 
        return name != null ? name : ""; 
    }

    public String getEmail() { 
        return email != null ? email : ""; 
    }

    public String getUsername() { 
        return username != null ? username : ""; 
    }

    public String getStudentID() { 
        return studentID != null ? studentID : ""; 
    }

    public String getBio() { 
        return bio != null ? bio : ""; 
    }

    public String getCareer() { 
        return career != null ? career : ""; 
    }

    public String getProfileImageUrl() { 
        return profileImageUrl != null ? profileImageUrl : ""; 
    }

    public List<String> getSkills() {
        // CRITICAL: Never return a null list. If the list is null in the database,
        // return a safe, empty list instead. This prevents the crash.
        return skills != null ? skills : Collections.emptyList();
    }

    // --- SETTERS ---
    // Setters are also required by Firestore for deserialization.
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setStudentID(String studentID) { this.studentID = studentID; }
    public void setBio(String bio) { this.bio = bio; }
    public void setCareer(String career) { this.career = career; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}

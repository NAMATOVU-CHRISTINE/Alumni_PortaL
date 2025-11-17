package com.namatovu.alumniportal.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mentorship connection model for Alumni Portal
 */
public class MentorshipConnection {
    private String connectionId;
    private String mentorId;
    private String menteeId;
    private String mentorName;
    private String menteeName;
    private String mentorTitle;
    private String mentorCompany;
    private String mentorProfileImage;
    private String menteeProfileImage;
    private String status; // "pending", "accepted", "rejected", "active", "completed", "cancelled"
    private String message; // Initial request message
    private String mentorshipType; // "career", "academic", "industry", "entrepreneurship", "general"
    private List<String> focusAreas; // Specific skills or areas to focus on
    private long requestedAt;
    private long acceptedAt;
    private long completedAt;
    private int sessionCount;
    private String duration; // "1-month", "3-months", "6-months", "ongoing"
    private Map<String, Object> mentorFeedback;
    private Map<String, Object> menteeFeedback;
    private boolean isFeatured; // For highlighting successful mentorships
    
    // Default constructor required for Firebase
    public MentorshipConnection() {
        this.requestedAt = System.currentTimeMillis();
        this.sessionCount = 0;
        this.mentorFeedback = new HashMap<>();
        this.menteeFeedback = new HashMap<>();
        this.isFeatured = false;
    }
    
    // Constructor for creating new mentorship request
    public MentorshipConnection(String mentorId, String menteeId, String mentorName, String menteeName, String message) {
        this();
        this.mentorId = mentorId;
        this.menteeId = menteeId;
        this.mentorName = mentorName;
        this.menteeName = menteeName;
        this.message = message;
        this.status = "pending";
    }
    
    // Getters and Setters
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    
    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }
    
    public String getMenteeId() { return menteeId; }
    public void setMenteeId(String menteeId) { this.menteeId = menteeId; }
    
    public String getMentorName() { return mentorName; }
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }
    
    public String getMenteeName() { return menteeName; }
    public void setMenteeName(String menteeName) { this.menteeName = menteeName; }
    
    public String getMentorTitle() { return mentorTitle; }
    public void setMentorTitle(String mentorTitle) { this.mentorTitle = mentorTitle; }
    
    public String getMentorCompany() { return mentorCompany; }
    public void setMentorCompany(String mentorCompany) { this.mentorCompany = mentorCompany; }
    
    public String getMentorProfileImage() { return mentorProfileImage; }
    public void setMentorProfileImage(String mentorProfileImage) { this.mentorProfileImage = mentorProfileImage; }
    
    // Alias methods for compatibility
    public String getMentorImageUrl() { return mentorProfileImage; }
    public void setMentorImageUrl(String mentorImageUrl) { this.mentorProfileImage = mentorImageUrl; }
    
    public String getMenteeProfileImage() { return menteeProfileImage; }
    public void setMenteeProfileImage(String menteeProfileImage) { this.menteeProfileImage = menteeProfileImage; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getMentorshipType() { return mentorshipType; }
    public void setMentorshipType(String mentorshipType) { this.mentorshipType = mentorshipType; }
    
    public List<String> getFocusAreas() { return focusAreas; }
    public void setFocusAreas(List<String> focusAreas) { this.focusAreas = focusAreas; }
    
    public long getRequestedAt() { return requestedAt; }
    public void setRequestedAt(long requestedAt) { this.requestedAt = requestedAt; }
    
    public long getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(long acceptedAt) { this.acceptedAt = acceptedAt; }
    
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    
    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public Map<String, Object> getMentorFeedback() { return mentorFeedback; }
    public void setMentorFeedback(Map<String, Object> mentorFeedback) { this.mentorFeedback = mentorFeedback; }
    
    public Map<String, Object> getMenteeFeedback() { return menteeFeedback; }
    public void setMenteeFeedback(Map<String, Object> menteeFeedback) { this.menteeFeedback = menteeFeedback; }
    
    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    
    // Request method for sending mentorship request
    public void request() {
        this.status = "pending";
        this.requestedAt = System.currentTimeMillis();
    }
    
    // Helper methods
    public boolean isPending() { return "pending".equals(status); }
    public boolean isAccepted() { return "accepted".equals(status); }
    public boolean isActive() { return "active".equals(status); }
    public boolean isCompleted() { return "completed".equals(status); }
    public boolean isRejected() { return "rejected".equals(status); }
    public boolean isCancelled() { return "cancelled".equals(status); }
    
    public void accept() {
        this.status = "accepted";
        this.acceptedAt = System.currentTimeMillis();
    }
    
    public void activate() {
        this.status = "active";
        if (this.acceptedAt == 0) {
            this.acceptedAt = System.currentTimeMillis();
        }
    }
    
    public void complete() {
        this.status = "completed";
        this.completedAt = System.currentTimeMillis();
    }
    
    public void reject() {
        this.status = "rejected";
    }
    
    public void cancel() {
        this.status = "cancelled";
    }
    
    public void incrementSessionCount() {
        this.sessionCount++;
    }
    
    public String getTimeAgo() {
        long diff = System.currentTimeMillis() - requestedAt;
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (days < 1) {
            long hours = diff / (60 * 60 * 1000);
            return hours < 1 ? "Just now" : hours + "h ago";
        } else if (days < 7) {
            return days + "d ago";
        } else if (days < 30) {
            return (days / 7) + "w ago";
        } else {
            return (days / 30) + "m ago";
        }
    }
    
    public String getStatusDisplayText() {
        switch (status) {
            case "pending": return "Pending Response";
            case "accepted": return "Accepted";
            case "active": return "Active Mentorship";
            case "completed": return "Completed";
            case "rejected": return "Declined";
            case "cancelled": return "Cancelled";
            default: return "Unknown";
        }
    }
    
    public String getStatusColor() {
        switch (status) {
            case "pending": return "#FFA500"; // Orange
            case "accepted": 
            case "active": return "#4CAF50"; // Green
            case "completed": return "#2196F3"; // Blue
            case "rejected": 
            case "cancelled": return "#F44336"; // Red
            default: return "#9E9E9E"; // Gray
        }
    }
    
    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> connectionMap = new HashMap<>();
        connectionMap.put("mentorId", mentorId);
        connectionMap.put("menteeId", menteeId);
        connectionMap.put("mentorName", mentorName);
        connectionMap.put("menteeName", menteeName);
        connectionMap.put("mentorTitle", mentorTitle);
        connectionMap.put("mentorCompany", mentorCompany);
        connectionMap.put("mentorProfileImage", mentorProfileImage);
        connectionMap.put("menteeProfileImage", menteeProfileImage);
        connectionMap.put("status", status);
        connectionMap.put("message", message);
        connectionMap.put("mentorshipType", mentorshipType);
        connectionMap.put("focusAreas", focusAreas);
        connectionMap.put("requestedAt", requestedAt);
        connectionMap.put("acceptedAt", acceptedAt);
        connectionMap.put("completedAt", completedAt);
        connectionMap.put("sessionCount", sessionCount);
        connectionMap.put("duration", duration);
        connectionMap.put("mentorFeedback", mentorFeedback);
        connectionMap.put("menteeFeedback", menteeFeedback);
        connectionMap.put("isFeatured", isFeatured);
        return connectionMap;
    }
}
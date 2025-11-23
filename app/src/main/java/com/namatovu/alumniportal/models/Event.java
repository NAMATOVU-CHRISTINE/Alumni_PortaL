package com.namatovu.alumniportal.models;

/**
 * Event model for alumni portal events, mentorship programs, and leadership activities
 */
public class Event {
    
    public enum Category {
        MENTORSHIP("Mentorship", "🤝"),
        LEADERSHIP("Leadership", "👑"),
        NETWORKING("Networking", "🌐"),
        UNIVERSITY("University", "🏛️"),
        CAREER("Career", "💼"),
        TECHNOLOGY("Technology", "💻"),
        SOCIAL("Social", "🎉");
        
        private final String displayName;
        private final String icon;
        
        Category(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
    
    public enum Status {
        UPCOMING,
        ONGOING,
        COMPLETED,
        CANCELLED
    }
    
    private String id;
    private String title;
    private String description;
    private String summary; // Short description for cards
    private long dateTime;
    private String location;
    private Category category;
    private Status status;
    private String imageUrl;
    private String registrationUrl;
    private String organizerName;
    private int maxParticipants;
    private int currentParticipants;
    private boolean isOnline;
    private long createdAt;
    private long updatedAt;
    
    public Event() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = Status.UPCOMING;
        this.currentParticipants = 0;
        this.maxParticipants = 50;
    }
    
    public Event(String title, String description, String summary, long dateTime, String location, Category category) {
        this();
        this.title = title;
        this.description = description;
        this.summary = summary;
        this.dateTime = dateTime;
        this.location = location;
        this.category = category;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getRegistrationUrl() { return registrationUrl; }
    public void setRegistrationUrl(String registrationUrl) { this.registrationUrl = registrationUrl; }
    
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    
    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isAvailableForRegistration() {
        return status == Status.UPCOMING && currentParticipants < maxParticipants;
    }
    
    public int getAvailableSpots() {
        return Math.max(0, maxParticipants - currentParticipants);
    }
    
    public String getFormattedDateTime() {
        return android.text.format.DateFormat.format("MMM dd, yyyy 'at' h:mm a", dateTime).toString();
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > dateTime;
    }
}
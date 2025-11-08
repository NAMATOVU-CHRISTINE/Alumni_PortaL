package com.namatovu.alumniportal.models;

/**
 * RecentActivity - Model class for user's recent activities
 */
public class RecentActivity {
    
    public enum Type {
        OPPORTUNITY,
        MESSAGE,
        KNOWLEDGE,
        CONNECTION,
        PROFILE,
        ACHIEVEMENT
    }

    private String icon;
    private String title;
    private String description;
    private String timeStamp;
    private Type type;
    private boolean isRead;

    public RecentActivity(String icon, String title, String description, String timeStamp, Type type) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.timeStamp = timeStamp;
        this.type = type;
        this.isRead = false;
    }

    // Getters and Setters
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
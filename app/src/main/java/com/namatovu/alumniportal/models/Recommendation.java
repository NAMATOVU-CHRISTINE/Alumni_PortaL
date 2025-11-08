package com.namatovu.alumniportal.models;

public class Recommendation {
    public enum Type {
        PROFILE_COMPLETION,
        JOB_OPPORTUNITY,
        SKILL_DEVELOPMENT,
        NETWORKING,
        EVENT_SUGGESTION,
        MENTORSHIP
    }
    
    private String id;
    private String title;
    private String description;
    private String icon;
    private Type type;
    private String actionUrl;
    private boolean isRead;
    private long timestamp;
    private int priority; // 1-5, higher is more important
    
    public Recommendation() {}
    
    public Recommendation(String id, String title, String description, String icon, Type type, String actionUrl, int priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.actionUrl = actionUrl;
        this.priority = priority;
        this.isRead = false;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
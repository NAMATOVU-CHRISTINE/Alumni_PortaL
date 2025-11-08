package com.namatovu.alumniportal.models;

/**
 * News model for university news, articles, and insights
 */
public class News {
    
    public enum Category {
        UNIVERSITY("University News", "🏛️"),
        ALUMNI("Alumni Stories", "👥"),
        RESEARCH("Research", "🔬"),
        ACADEMICS("Academics", "📚"),
        SPORTS("Sports", "⚽"),
        TECHNOLOGY("Technology", "💻"),
        HEALTH("Health", "🏥"),
        GENERAL("General", "📰");
        
        private final String displayName;
        private final String icon;
        
        Category(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
    
    private String id;
    private String title;
    private String content;
    private String summary; // Short description for cards
    private String imageUrl;
    private String sourceUrl; // Link to full article
    private Category category;
    private String author;
    private long publishedAt;
    private long createdAt;
    private boolean isFeatured;
    private int viewCount;
    private boolean isExternal; // True if from must.ac.ug or other sources
    
    public News() {
        this.createdAt = System.currentTimeMillis();
        this.publishedAt = System.currentTimeMillis();
        this.viewCount = 0;
        this.isFeatured = false;
        this.isExternal = false;
    }
    
    public News(String title, String content, String summary, Category category, String author) {
        this();
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.category = category;
        this.author = author;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(long publishedAt) { this.publishedAt = publishedAt; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    
    public boolean isExternal() { return isExternal; }
    public void setExternal(boolean external) { isExternal = external; }
    
    // Helper methods
    public String getFormattedPublishDate() {
        return android.text.format.DateFormat.format("MMM dd, yyyy", publishedAt).toString();
    }
    
    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - publishedAt;
        
        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            int minutes = (int) (diff / 60000);
            return minutes + " min ago";
        } else if (diff < 86400000) { // Less than 1 day
            int hours = (int) (diff / 3600000);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (diff < 604800000) { // Less than 1 week
            int days = (int) (diff / 86400000);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return getFormattedPublishDate();
        }
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
}
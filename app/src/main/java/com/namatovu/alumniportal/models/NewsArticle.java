package com.namatovu.alumniportal.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * News article model for Alumni Portal
 */
public class NewsArticle {
    private String articleId;
    private String title;
    private String content;
    private String summary;
    private String authorId;
    private String authorName;
    private String authorRole;
    private String category; // "university", "alumni", "achievements", "announcements", "careers", "research"
    private List<String> tags;
    private String imageUrl;
    private String sourceUrl;
    private boolean isFeatured;
    private boolean isPublished;
    private boolean isPinned;
    private long publishedAt;
    private long createdAt;
    private long updatedAt;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private List<String> likedByUserIds;
    private String priority; // "high", "medium", "low"
    private Map<String, Object> metadata;

    // Default constructor required for Firebase
    public NewsArticle() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.isPublished = false;
        this.isFeatured = false;
        this.isPinned = false;
        this.priority = "medium";
        this.metadata = new HashMap<>();
    }

    // Constructor for creating new article
    public NewsArticle(String title, String content, String authorId, String authorName, String category) {
        this();
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.category = category;
    }

    // Getters and Setters
    public String getArticleId() { return articleId; }
    public void setArticleId(String articleId) { this.articleId = articleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(long publishedAt) { this.publishedAt = publishedAt; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public List<String> getLikedByUserIds() { return likedByUserIds; }
    public void setLikedByUserIds(List<String> likedByUserIds) { this.likedByUserIds = likedByUserIds; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Helper methods
    public void publish() {
        this.isPublished = true;
        this.publishedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public void unpublish() {
        this.isPublished = false;
        this.updatedAt = System.currentTimeMillis();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public boolean isLikedByUser(String userId) {
        return likedByUserIds != null && likedByUserIds.contains(userId);
    }

    public String getTimeAgo() {
        long timestamp = publishedAt > 0 ? publishedAt : createdAt;
        long diff = System.currentTimeMillis() - timestamp;
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

    public String getFormattedDate() {
        long timestamp = publishedAt > 0 ? publishedAt : createdAt;
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return dateFormat.format(new java.util.Date(timestamp));
    }

    public String getCategoryDisplayText() {
        if (category == null) return "News";
        
        switch (category.toLowerCase()) {
            case "university": return "University News";
            case "alumni": return "Alumni Spotlight";
            case "achievements": return "Achievements";
            case "announcements": return "Announcements";
            case "careers": return "Career News";
            case "research": return "Research & Innovation";
            default: return category;
        }
    }

    public String getAuthorDisplayText() {
        if (authorRole != null && !authorRole.isEmpty()) {
            return authorName + " - " + authorRole;
        }
        return authorName != null ? authorName : "Alumni Portal";
    }

    public String getReadTimeEstimate() {
        if (content == null) return "1 min read";
        
        int wordCount = content.split("\\s+").length;
        int readTimeMinutes = Math.max(1, wordCount / 200); // Average reading speed: 200 words per minute
        
        return readTimeMinutes + " min read";
    }

    public String getGeneratedSummary() {
        if (summary != null && !summary.isEmpty()) {
            return summary;
        }
        
        if (content != null && content.length() > 150) {
            return content.substring(0, 147) + "...";
        }
        
        return content;
    }

    public String getPriorityColor() {
        switch (priority.toLowerCase()) {
            case "high": return "#F44336"; // Red
            case "medium": return "#FF9800"; // Orange  
            case "low": return "#4CAF50"; // Green
            default: return "#9E9E9E"; // Gray
        }
    }

    public boolean shouldShowToUser(String userId, List<String> userInterests) {
        if (!isPublished) return false;
        
        // Always show pinned and featured articles
        if (isPinned || isFeatured) return true;
        
        // Show if user has interests matching article tags
        if (userInterests != null && tags != null) {
            for (String interest : userInterests) {
                for (String tag : tags) {
                    if (tag.toLowerCase().contains(interest.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        
        return true; // Default: show all published articles
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> articleMap = new HashMap<>();
        articleMap.put("title", title);
        articleMap.put("content", content);
        articleMap.put("summary", summary);
        articleMap.put("authorId", authorId);
        articleMap.put("authorName", authorName);
        articleMap.put("authorRole", authorRole);
        articleMap.put("category", category);
        articleMap.put("tags", tags);
        articleMap.put("imageUrl", imageUrl);
        articleMap.put("sourceUrl", sourceUrl);
        articleMap.put("isFeatured", isFeatured);
        articleMap.put("isPublished", isPublished);
        articleMap.put("isPinned", isPinned);
        articleMap.put("publishedAt", publishedAt);
        articleMap.put("createdAt", createdAt);
        articleMap.put("updatedAt", updatedAt);
        articleMap.put("viewCount", viewCount);
        articleMap.put("likeCount", likeCount);
        articleMap.put("commentCount", commentCount);
        articleMap.put("likedByUserIds", likedByUserIds);
        articleMap.put("priority", priority);
        articleMap.put("metadata", metadata);
        return articleMap;
    }

    // Alias methods for adapter compatibility
    public String getAuthor() { 
        return getAuthorName(); 
    }
    
    public java.util.Date getPublishedDate() { 
        return new java.util.Date(getPublishedAt()); 
    }
}
package com.namatovu.alumniportal.models;

import java.util.Date;

/**
 * Comment model for articles
 */
public class Comment {
    private String id;
    private String authorName;
    private String content;
    private Date timestamp;
    private String authorAvatar; // Could be URL or resource identifier
    private int likeCount;
    private boolean isLiked;

    // Constructors
    public Comment() {
        this.timestamp = new Date();
        this.likeCount = 0;
        this.isLiked = false;
    }

    public Comment(String authorName, String content) {
        this();
        this.authorName = authorName;
        this.content = content;
        this.id = generateId();
    }

    public Comment(String id, String authorName, String content, Date timestamp) {
        this.id = id;
        this.authorName = authorName;
        this.content = content;
        this.timestamp = timestamp;
        this.likeCount = 0;
        this.isLiked = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    // Utility methods
    public void toggleLike() {
        if (isLiked) {
            likeCount = Math.max(0, likeCount - 1);
        } else {
            likeCount++;
        }
        isLiked = !isLiked;
    }

    private String generateId() {
        return "comment_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public String getFormattedTimestamp() {
        long now = System.currentTimeMillis();
        long commentTime = timestamp.getTime();
        long diff = now - commentTime;

        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            int minutes = (int) (diff / 60000);
            return minutes + "m ago";
        } else if (diff < 86400000) { // Less than 1 day
            int hours = (int) (diff / 3600000);
            return hours + "h ago";
        } else {
            int days = (int) (diff / 86400000);
            return days + "d ago";
        }
    }
}
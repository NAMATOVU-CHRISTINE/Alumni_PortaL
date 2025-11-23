package com.namatovu.alumniportal.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Article model class for Knowledge section
 */
public class Article {
    private String id;
    private String title;
    private String description;
    private String content;
    private String category;
    private String categoryIcon;
    private String authorId;
    private String authorName;
    private Date dateCreated;
    private Date dateModified;
    private boolean isBookmarked;
    private int views;
    private String imageUrl;
    private int likeCount;
    private List<String> likedByUsers;
    private boolean isLiked;

    // Default constructor required for Firebase
    public Article() {
        this.likedByUsers = new ArrayList<>();
        this.likeCount = 0;
        this.isLiked = false;
    }

    public Article(String title, String description, String content, String category) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.category = category;
        this.categoryIcon = getCategoryIcon(category);
        this.dateCreated = new Date();
        this.dateModified = new Date();
        this.isBookmarked = false;
        this.views = 0;
        this.likedByUsers = new ArrayList<>();
        this.likeCount = 0;
        this.isLiked = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.categoryIcon = getCategoryIcon(category);
    }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }

    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public List<String> getLikedByUsers() { return likedByUsers; }
    public void setLikedByUsers(List<String> likedByUsers) { this.likedByUsers = likedByUsers; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    // Method to toggle like
    public void toggleLike(String userId) {
        if (likedByUsers == null) {
            likedByUsers = new ArrayList<>();
        }
        
        if (likedByUsers.contains(userId)) {
            likedByUsers.remove(userId);
            likeCount = Math.max(0, likeCount - 1);
            isLiked = false;
        } else {
            likedByUsers.add(userId);
            likeCount++;
            isLiked = true;
        }
    }

    // Method to check if user has liked
    public boolean hasUserLiked(String userId) {
        return likedByUsers != null && likedByUsers.contains(userId);
    }

    // Helper method to get category icon
    private String getCategoryIcon(String category) {
        switch (category) {
            case "Networking": return "🤝";
            case "Skills": return "💡";
            case "Mentorship": return "🌟";
            case "Career Growth": return "🚀";
            case "Leadership": return "🏆";
            default: return "📚";
        }
    }

    // Helper method to get formatted date
    public String getFormattedDate() {
        if (dateCreated == null) return "";
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
        return sdf.format(dateCreated);
    }
}
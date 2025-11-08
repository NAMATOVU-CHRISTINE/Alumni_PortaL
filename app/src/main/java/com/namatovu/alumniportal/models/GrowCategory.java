package com.namatovu.alumniportal.models;

public class GrowCategory {
    private String title;
    private String description;
    private String icon;
    private String backgroundColor;
    private Class<?> targetActivity;

    public GrowCategory() {
        // Default constructor required for Firebase
    }

    public GrowCategory(String title, String description, String icon, String backgroundColor, Class<?> targetActivity) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.backgroundColor = backgroundColor;
        this.targetActivity = targetActivity;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Class<?> getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(Class<?> targetActivity) {
        this.targetActivity = targetActivity;
    }
}
package com.namatovu.alumniportal.models;

/**
 * CareerTip model class for storing career tip data
 * Contains tip content, category, and save status
 */
public class CareerTip {
    private String id;
    private String text;
    private String category;
    private boolean saved;

    public CareerTip() {
        // Empty constructor required for Firestore
    }

    public CareerTip(String id, String text, String category, boolean saved) {
        this.id = id;
        this.text = text;
        this.category = category;
        this.saved = saved;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    @Override
    public String toString() {
        return "CareerTip{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", category='" + category + '\'' +
                ", saved=" + saved +
                '}';
    }
}
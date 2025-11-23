package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey
    @NonNull
    private String eventId;
    
    private String title;
    private String description;
    private String location;
    private String imageUrl;
    private long eventDate;
    private String organizerId;
    private String organizerName;
    private String category;
    private int attendeeCount;
    private boolean isAttending;
    private long createdAt;
    private long lastSyncTime;

    public EventEntity() {
        this.lastSyncTime = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getEventId() { return eventId; }
    public void setEventId(@NonNull String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getEventDate() { return eventDate; }
    public void setEventDate(long eventDate) { this.eventDate = eventDate; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getAttendeeCount() { return attendeeCount; }
    public void setAttendeeCount(int attendeeCount) { this.attendeeCount = attendeeCount; }

    public boolean isAttending() { return isAttending; }
    public void setAttending(boolean attending) { isAttending = attending; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(long lastSyncTime) { this.lastSyncTime = lastSyncTime; }
}

package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

/**
 * Room entity for offline event storage
 */
@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "event_id")
    public String eventId;
    
    @ColumnInfo(name = "title")
    public String title;
    
    @ColumnInfo(name = "description")
    public String description;
    
    @ColumnInfo(name = "category")
    public String category;
    
    @ColumnInfo(name = "date_time")
    public long dateTime;
    
    @ColumnInfo(name = "end_date_time")
    public long endDateTime;
    
    @ColumnInfo(name = "location")
    public String location;
    
    @ColumnInfo(name = "venue")
    public String venue;
    
    @ColumnInfo(name = "is_virtual")
    public boolean isVirtual;
    
    @ColumnInfo(name = "meeting_link")
    public String meetingLink;
    
    @ColumnInfo(name = "max_attendees")
    public int maxAttendees;
    
    @ColumnInfo(name = "current_attendees")
    public int currentAttendees;
    
    @ColumnInfo(name = "registration_deadline")
    public long registrationDeadline;
    
    @ColumnInfo(name = "is_paid")
    public boolean isPaid;
    
    @ColumnInfo(name = "price")
    public double price;
    
    @ColumnInfo(name = "currency")
    public String currency;
    
    @ColumnInfo(name = "organizer_id")
    public String organizerId;
    
    @ColumnInfo(name = "organizer_name")
    public String organizerName;
    
    @ColumnInfo(name = "contact_email")
    public String contactEmail;
    
    @ColumnInfo(name = "contact_phone")
    public String contactPhone;
    
    @ColumnInfo(name = "image_url")
    public String imageUrl;
    
    @ColumnInfo(name = "tags")
    public String tags;
    
    @ColumnInfo(name = "is_active")
    public boolean isActive;
    
    @ColumnInfo(name = "created_at")
    public long createdAt;
    
    @ColumnInfo(name = "updated_at")
    public long updatedAt;
    
    @ColumnInfo(name = "sync_status")
    public String syncStatus;
    
    @ColumnInfo(name = "last_sync")
    public long lastSync;
    
    // Default constructor
    public EventEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending";
        this.lastSync = 0;
        this.isActive = true;
        this.currency = "USD";
    }
}
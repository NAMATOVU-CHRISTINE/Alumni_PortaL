package com.namatovu.alumniportal.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey
    @NonNull
    public String eventId;
    public String title;
    public String description;
    public String category;
    public long dateTime;
    public long endDateTime;
    public String location;
    public String venue;
    public boolean isVirtual;
    public String meetingLink;
    public int maxAttendees;
    public int currentAttendees;
    public long registrationDeadline;
    public boolean isPaid;
    public double price;
    public String currency;
    public String organizerId;
    public String organizerName;
    public String contactEmail;
    public String contactPhone;
    public String imageUrl;
    public String tags;
    public boolean isActive;
    public long createdAt;
    public long updatedAt;
    public long lastSync;
    public String syncStatus;
}

package com.namatovu.alumniportal.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event model for Alumni Portal
 */
public class AlumniEvent {
    private String eventId;
    private String title;
    private String description;
    private String venue;
    private String address;
    private String eventType; // "reunion", "networking", "workshop", "seminar", "social", "career", "fundraising"
    private long startDateTime;
    private long endDateTime;
    private String organizer; // User ID
    private String organizerName;
    private String organizerContact;
    private String imageUrl;
    private String registrationUrl;
    private boolean isOnline;
    private String onlineLink;
    private boolean requiresRegistration;
    private boolean isFree;
    private String ticketPrice;
    private int maxAttendees;
    private int currentAttendees;
    private List<String> attendeeIds;
    private List<String> tags;
    private String targetAudience; // "all", "alumni", "students", "faculty", "specific_years"
    private List<String> targetGraduationYears;
    private boolean isPublic;
    private boolean isFeatured;
    private long createdAt;
    private long updatedAt;
    private Map<String, Object> additionalDetails;

    // Default constructor required for Firebase
    public AlumniEvent() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.currentAttendees = 0;
        this.isPublic = true;
        this.isFeatured = false;
        this.requiresRegistration = false;
        this.isFree = true;
        this.isOnline = false;
        this.additionalDetails = new HashMap<>();
    }

    // Constructor for creating new event
    public AlumniEvent(String title, String description, String venue, long startDateTime, long endDateTime, String organizer, String organizerName) {
        this();
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.organizer = organizer;
        this.organizerName = organizerName;
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public long getStartDateTime() { return startDateTime; }
    public void setStartDateTime(long startDateTime) { this.startDateTime = startDateTime; }

    public long getEndDateTime() { return endDateTime; }
    public void setEndDateTime(long endDateTime) { this.endDateTime = endDateTime; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public String getOrganizerContact() { return organizerContact; }
    public void setOrganizerContact(String organizerContact) { this.organizerContact = organizerContact; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getRegistrationUrl() { return registrationUrl; }
    public void setRegistrationUrl(String registrationUrl) { this.registrationUrl = registrationUrl; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getOnlineLink() { return onlineLink; }
    public void setOnlineLink(String onlineLink) { this.onlineLink = onlineLink; }

    public boolean isRequiresRegistration() { return requiresRegistration; }
    public void setRequiresRegistration(boolean requiresRegistration) { this.requiresRegistration = requiresRegistration; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public String getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(String ticketPrice) { this.ticketPrice = ticketPrice; }

    public int getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(int maxAttendees) { this.maxAttendees = maxAttendees; }

    public int getCurrentAttendees() { return currentAttendees; }
    public void setCurrentAttendees(int currentAttendees) { this.currentAttendees = currentAttendees; }

    public List<String> getAttendeeIds() { return attendeeIds; }
    public void setAttendeeIds(List<String> attendeeIds) { this.attendeeIds = attendeeIds; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public List<String> getTargetGraduationYears() { return targetGraduationYears; }
    public void setTargetGraduationYears(List<String> targetGraduationYears) { this.targetGraduationYears = targetGraduationYears; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getAdditionalDetails() { return additionalDetails; }
    public void setAdditionalDetails(Map<String, Object> additionalDetails) { this.additionalDetails = additionalDetails; }

    // Helper methods
    public boolean isUpcoming() {
        return startDateTime > System.currentTimeMillis();
    }

    public boolean isOngoing() {
        long now = System.currentTimeMillis();
        return startDateTime <= now && endDateTime >= now;
    }

    public boolean isPast() {
        return endDateTime < System.currentTimeMillis();
    }

    public boolean hasSpaceAvailable() {
        return maxAttendees <= 0 || currentAttendees < maxAttendees;
    }

    public boolean isUserAttending(String userId) {
        return attendeeIds != null && attendeeIds.contains(userId);
    }

    public String getFormattedDate() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return dateFormat.format(new java.util.Date(startDateTime));
    }

    public String getFormattedTime() {
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("h:mm a");
        String startTime = timeFormat.format(new java.util.Date(startDateTime));
        String endTime = timeFormat.format(new java.util.Date(endDateTime));
        return startTime + " - " + endTime;
    }

    public String getFormattedDateTime() {
        java.text.SimpleDateFormat dateTimeFormat = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a");
        return dateTimeFormat.format(new java.util.Date(startDateTime));
    }

    public String getTimeUntilEvent() {
        if (isPast()) return "Event ended";
        if (isOngoing()) return "Happening now";
        
        long diff = startDateTime - System.currentTimeMillis();
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (days < 1) {
            long hours = diff / (60 * 60 * 1000);
            return hours < 1 ? "Starting soon" : "In " + hours + "h";
        } else if (days < 7) {
            return "In " + days + " day" + (days > 1 ? "s" : "");
        } else if (days < 30) {
            return "In " + (days / 7) + " week" + (days > 7 ? "s" : "");
        } else {
            return "In " + (days / 30) + " month" + (days > 30 ? "s" : "");
        }
    }

    public String getVenueDisplayText() {
        if (isOnline) {
            return "Online Event";
        } else if (venue != null && address != null) {
            return venue + ", " + address;
        } else if (venue != null) {
            return venue;
        } else if (address != null) {
            return address;
        } else {
            return "Venue TBD";
        }
    }

    public String getPriceDisplayText() {
        if (isFree) {
            return "Free";
        } else if (ticketPrice != null) {
            return ticketPrice;
        } else {
            return "Price TBD";
        }
    }

    public String getAttendanceText() {
        if (maxAttendees > 0) {
            return currentAttendees + "/" + maxAttendees + " attending";
        } else {
            return currentAttendees + " attending";
        }
    }

    public String getEventTypeDisplayText() {
        if (eventType == null) return "Event";
        
        switch (eventType.toLowerCase()) {
            case "reunion": return "Alumni Reunion";
            case "networking": return "Networking Event";
            case "workshop": return "Workshop";
            case "seminar": return "Seminar";
            case "social": return "Social Event";
            case "career": return "Career Event";
            case "fundraising": return "Fundraising";
            default: return eventType;
        }
    }

    public void incrementAttendees() {
        this.currentAttendees++;
        this.updatedAt = System.currentTimeMillis();
    }

    public void decrementAttendees() {
        if (this.currentAttendees > 0) {
            this.currentAttendees--;
            this.updatedAt = System.currentTimeMillis();
        }
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("title", title);
        eventMap.put("description", description);
        eventMap.put("venue", venue);
        eventMap.put("address", address);
        eventMap.put("eventType", eventType);
        eventMap.put("startDateTime", startDateTime);
        eventMap.put("endDateTime", endDateTime);
        eventMap.put("organizer", organizer);
        eventMap.put("organizerName", organizerName);
        eventMap.put("organizerContact", organizerContact);
        eventMap.put("imageUrl", imageUrl);
        eventMap.put("registrationUrl", registrationUrl);
        eventMap.put("isOnline", isOnline);
        eventMap.put("onlineLink", onlineLink);
        eventMap.put("requiresRegistration", requiresRegistration);
        eventMap.put("isFree", isFree);
        eventMap.put("ticketPrice", ticketPrice);
        eventMap.put("maxAttendees", maxAttendees);
        eventMap.put("currentAttendees", currentAttendees);
        eventMap.put("attendeeIds", attendeeIds);
        eventMap.put("tags", tags);
        eventMap.put("targetAudience", targetAudience);
        eventMap.put("targetGraduationYears", targetGraduationYears);
        eventMap.put("isPublic", isPublic);
        eventMap.put("isFeatured", isFeatured);
        eventMap.put("createdAt", createdAt);
        eventMap.put("updatedAt", updatedAt);
        eventMap.put("additionalDetails", additionalDetails);
        return eventMap;
    }
}
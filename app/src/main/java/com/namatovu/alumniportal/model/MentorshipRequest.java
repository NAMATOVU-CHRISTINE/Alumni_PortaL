package com.namatovu.alumniportal.model;

import java.util.Date;

public class MentorshipRequest {
    private String id;
    private String requesterUid;
    private String mentorUid;
    private String topic;
    private String message;
    private String status; // pending, accepted, declined, completed
    private Date createdAt;

    public MentorshipRequest() { }

    public MentorshipRequest(String requesterUid, String mentorUid, String topic, String message) {
        this.requesterUid = requesterUid;
        this.mentorUid = mentorUid;
        this.topic = topic;
        this.message = message;
        this.status = "pending";
        this.createdAt = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRequesterUid() { return requesterUid; }
    public void setRequesterUid(String requesterUid) { this.requesterUid = requesterUid; }

    public String getMentorUid() { return mentorUid; }
    public void setMentorUid(String mentorUid) { this.mentorUid = mentorUid; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

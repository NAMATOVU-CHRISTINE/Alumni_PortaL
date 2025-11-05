package com.namatovu.alumniportal.model;

import java.util.Date;

public class ConnectionRequest {
    private String id;
    private String fromUid;
    private String toUid;
    private String message;
    private String status; // pending, accepted, declined
    private Date createdAt;

    public ConnectionRequest() {
        // Required empty constructor for Firestore
    }

    public ConnectionRequest(String fromUid, String toUid, String message) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.message = message;
        this.status = "pending";
        this.createdAt = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromUid() { return fromUid; }
    public void setFromUid(String fromUid) { this.fromUid = fromUid; }

    public String getToUid() { return toUid; }
    public void setToUid(String toUid) { this.toUid = toUid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

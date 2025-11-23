package com.namatovu.alumniportal.model;

import java.util.Date;

public class Message {
    private String id;
    private String fromUid;
    private String toUid;
    private String text;
    private Date sentAt;

    public Message() { }

    public Message(String fromUid, String toUid, String text) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.text = text;
        this.sentAt = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromUid() { return fromUid; }
    public void setFromUid(String fromUid) { this.fromUid = fromUid; }

    public String getToUid() { return toUid; }
    public void setToUid(String toUid) { this.toUid = toUid; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
}

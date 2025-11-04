package com.namatovu.alumniportal;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Post {
    private String title;
    private String content;
    private String authorName;
    private String authorId;
    @ServerTimestamp
    private Date timestamp;

    // No-argument constructor required for Firestore
    public Post() {}

    public Post(String title, String content, String authorName, String authorId) {
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

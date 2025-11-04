package com.namatovu.alumniportal;

import com.google.firebase.firestore.DocumentId;

public class Mentor {

    @DocumentId
    private String id; // This field will be automatically populated with the document ID

    private String name;
    private String expertise;
    private String profileImageUrl;

    // No-argument constructor required for Firestore deserialization
    public Mentor() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getExpertise() {
        return expertise;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}

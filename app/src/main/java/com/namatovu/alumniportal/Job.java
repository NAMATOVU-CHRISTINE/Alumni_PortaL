package com.namatovu.alumniportal;

import com.google.firebase.firestore.DocumentId;

public class Job {

    @DocumentId
    private String id;

    private String title;
    private String company;
    private String location;
    private String description;
    private String applyUrl;

    // No-argument constructor required for Firestore deserialization
    public Job() {}

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getApplyUrl() {
        return applyUrl;
    }
}

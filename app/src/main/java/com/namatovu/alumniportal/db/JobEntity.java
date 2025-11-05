package com.namatovu.alumniportal.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "jobs")
public class JobEntity {

    @PrimaryKey
    @androidx.annotation.NonNull
    public String id;

    public String title;
    public String company;
    public String location;
    public String description;
    public String applyUrl;

    public JobEntity() {}
}

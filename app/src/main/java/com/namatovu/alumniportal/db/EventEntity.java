package com.namatovu.alumniportal.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity {

    @PrimaryKey
    @androidx.annotation.NonNull
    public String id;

    public String title;
    public String link;
    public String pubDate;
    public String description;

    public EventEntity() {}
}

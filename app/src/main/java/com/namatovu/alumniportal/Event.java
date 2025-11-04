package com.namatovu.alumniportal;

public class Event {
    private String title;
    private String link;

    public Event(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }
}

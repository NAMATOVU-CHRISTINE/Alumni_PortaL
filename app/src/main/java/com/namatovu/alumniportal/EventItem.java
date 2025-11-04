package com.namatovu.alumniportal;

public class EventItem {
    private final String title;
    private final String description;
    private final String articleUrl;

    public EventItem(String title, String description, String articleUrl) {
        this.title = title;
        this.description = description;
        this.articleUrl = articleUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getArticleUrl() {
        return articleUrl;
    }
}

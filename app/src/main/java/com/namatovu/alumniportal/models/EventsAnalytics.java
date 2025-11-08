package com.namatovu.alumniportal.models;

import java.util.Map;
import java.util.HashMap;

/**
 * Analytics model for Events, News & Insights statistics
 */
public class EventsAnalytics {
    
    private int totalEvents;
    private int totalArticles;
    private int totalUpcomingEvents;
    private int totalCompletedEvents;
    private String topEventCategory;
    private String topNewsCategory;
    private Map<String, Integer> eventCategoryCounts;
    private Map<String, Integer> newsCategoryCounts;
    private long lastUpdated;
    
    public EventsAnalytics() {
        this.eventCategoryCounts = new HashMap<>();
        this.newsCategoryCounts = new HashMap<>();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    // Getters and setters
    public int getTotalEvents() { return totalEvents; }
    public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
    
    public int getTotalArticles() { return totalArticles; }
    public void setTotalArticles(int totalArticles) { this.totalArticles = totalArticles; }
    
    public int getTotalUpcomingEvents() { return totalUpcomingEvents; }
    public void setTotalUpcomingEvents(int totalUpcomingEvents) { this.totalUpcomingEvents = totalUpcomingEvents; }
    
    public int getTotalCompletedEvents() { return totalCompletedEvents; }
    public void setTotalCompletedEvents(int totalCompletedEvents) { this.totalCompletedEvents = totalCompletedEvents; }
    
    public String getTopEventCategory() { return topEventCategory; }
    public void setTopEventCategory(String topEventCategory) { this.topEventCategory = topEventCategory; }
    
    public String getTopNewsCategory() { return topNewsCategory; }
    public void setTopNewsCategory(String topNewsCategory) { this.topNewsCategory = topNewsCategory; }
    
    public Map<String, Integer> getEventCategoryCounts() { return eventCategoryCounts; }
    public void setEventCategoryCounts(Map<String, Integer> eventCategoryCounts) { this.eventCategoryCounts = eventCategoryCounts; }
    
    public Map<String, Integer> getNewsCategoryCounts() { return newsCategoryCounts; }
    public void setNewsCategoryCounts(Map<String, Integer> newsCategoryCounts) { this.newsCategoryCounts = newsCategoryCounts; }
    
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    
    // Helper methods
    public void incrementEventCategory(String category) {
        eventCategoryCounts.put(category, eventCategoryCounts.getOrDefault(category, 0) + 1);
        updateTopEventCategory();
    }
    
    public void incrementNewsCategory(String category) {
        newsCategoryCounts.put(category, newsCategoryCounts.getOrDefault(category, 0) + 1);
        updateTopNewsCategory();
    }
    
    private void updateTopEventCategory() {
        topEventCategory = eventCategoryCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
    }
    
    private void updateTopNewsCategory() {
        topNewsCategory = newsCategoryCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
    }
    
    public String getOverallTopCategory() {
        String eventTop = topEventCategory != null ? topEventCategory : "None";
        String newsTop = topNewsCategory != null ? topNewsCategory : "None";
        
        int eventTopCount = eventCategoryCounts.getOrDefault(eventTop, 0);
        int newsTopCount = newsCategoryCounts.getOrDefault(newsTop, 0);
        
        return eventTopCount >= newsTopCount ? eventTop : newsTop;
    }
    
    public void updateAnalytics() {
        this.lastUpdated = System.currentTimeMillis();
        updateTopEventCategory();
        updateTopNewsCategory();
    }
}
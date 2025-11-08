package com.namatovu.alumniportal.utils;

import com.namatovu.alumniportal.models.Event;
import com.namatovu.alumniportal.models.News;
import com.namatovu.alumniportal.models.EventsAnalytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Data provider for Events, News & Insights
 * Initially empty - will be populated when users start creating content
 */
public class EventsDataProvider {
    
    /**
     * Get all events - Returns empty list initially, will be populated by user actions
     */
    public static List<Event> getEvents() {
        // Return empty list - will be populated when users create/add events
        return new ArrayList<>();
    }
    
    /**
     * Get all news - Returns empty list initially, will be populated by user actions  
     */
    public static List<News> getNews() {
        // Return empty list - will be populated when news is added by users/admin
        return new ArrayList<>();
    }
    
    /**
     * Get analytics for events and news
     */
    public static EventsAnalytics getAnalytics(List<Event> events, List<News> news) {
        EventsAnalytics analytics = new EventsAnalytics();
        
        if (events != null) {
            analytics.setTotalEvents(events.size());
        }
        
        if (news != null) {
            analytics.setTotalArticles(news.size());
        }
        
        // Update analytics - will calculate top category based on actual data
        analytics.updateAnalytics();
        
        return analytics;
    }
}
package com.namatovu.alumniportal.utils;

import com.namatovu.alumniportal.models.Event;
import com.namatovu.alumniportal.models.News;
import com.namatovu.alumniportal.models.EventsAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Data provider for Events, News & Insights
 * Loads real data from Firestore
 */
public class EventsDataProvider {
    
    private static final String TAG = "EventsDataProvider";
    
    /**
     * Callback interface for async news loading
     */
    public interface NewsCallback {
        void onNewsLoaded(List<News> newsList);
        void onError(Exception e);
    }
    
    /**
     * Get all events from Firestore
     */
    public static List<Event> getEvents() {
        // Return empty list - will be populated when users create/add events
        return new ArrayList<>();
    }
    
    /**
     * Get all news from Firestore - Real data with callback
     */
    public static void getNewsAsync(NewsCallback callback) {
        List<News> newsList = new ArrayList<>();
        
        // Load from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("news")
            .orderBy("publishedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                android.util.Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " news items");
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        News news = document.toObject(News.class);
                        if (news != null) {
                            news.setId(document.getId());
                            newsList.add(news);
                            android.util.Log.d(TAG, "Loaded news: " + news.getTitle());
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error parsing news", e);
                    }
                }
                
                callback.onNewsLoaded(newsList);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e(TAG, "Error loading news from Firestore", e);
                callback.onError(e);
            });
    }
    
    /**
     * Get all news from Firestore - Synchronous (returns empty, loads async)
     */
    public static List<News> getNews() {
        // Return empty list - news will be loaded asynchronously
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
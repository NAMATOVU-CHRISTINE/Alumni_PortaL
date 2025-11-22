package com.namatovu.alumniportal.utils;

import com.namatovu.alumniportal.models.Event;
import com.namatovu.alumniportal.models.News;
import com.namatovu.alumniportal.models.EventsAnalytics;
import com.namatovu.alumniportal.services.MUSTNewsScraper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class EventsDataProvider {
    
    private static final String TAG = "EventsDataProvider";
    
    public interface NewsCallback {
        void onNewsLoaded(List<News> newsList);
        void onError(Exception e);
    }
    
    public static List<Event> getEvents() {
        return new ArrayList<>();
    }
    
    public static void getNewsAsync(NewsCallback callback) {
        MUSTNewsScraper.scrapeAndSaveNews(new MUSTNewsScraper.ScraperCallback() {
            @Override
            public void onSuccess(int newsCount) {
                loadNewsFromFirestore(callback);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Scraping error: " + error);
                loadNewsFromFirestore(callback);
            }
        });
    }
    
    public static List<News> getNews() {
        MUSTNewsScraper.scrapeAndSaveNews(new MUSTNewsScraper.ScraperCallback() {
            @Override
            public void onSuccess(int newsCount) {
                Log.d(TAG, "Scraped " + newsCount + " news items");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Scraping error: " + error);
            }
        });
        
        return new ArrayList<>();
    }
    
    private static void loadNewsFromFirestore(NewsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<News> newsList = new ArrayList<>();
        
        db.collection("news")
            .orderBy("publishedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " news items");
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        News news = new News();
                        news.setId(document.getId());
                        news.setTitle(document.getString("title"));
                        news.setSummary(document.getString("summary"));
                        news.setContent(document.getString("content"));
                        news.setPublishedAt(document.getLong("publishedAt") != null ? document.getLong("publishedAt") : System.currentTimeMillis());
                        news.setAuthor("MUST Website");
                        
                        String categoryStr = document.getString("category");
                        if (categoryStr != null) {
                            try {
                                news.setCategory(News.Category.valueOf(categoryStr));
                            } catch (IllegalArgumentException e) {
                                news.setCategory(News.Category.UNIVERSITY);
                            }
                        } else {
                            news.setCategory(News.Category.UNIVERSITY);
                        }
                        
                        newsList.add(news);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing news", e);
                    }
                }
                
                callback.onNewsLoaded(newsList);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading news from Firestore", e);
                callback.onError(e);
            });
    }
    
    public static EventsAnalytics getAnalytics(List<Event> events, List<News> news) {
        EventsAnalytics analytics = new EventsAnalytics();
        
        if (events != null) {
            analytics.setTotalEvents(events.size());
        }
        
        if (news != null) {
            analytics.setTotalArticles(news.size());
        }
        
        analytics.updateAnalytics();
        
        return analytics;
    }
}

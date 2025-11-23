package com.namatovu.alumniportal.services;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Initialize sample news and events data to Firestore
 */
public class InitialDataLoader {
    
    private static final String TAG = "InitialDataLoader";
    
    public interface LoadCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * Load initial news and events data from MUST website
     */
    public static void loadInitialData(LoadCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Check if data already exists
        db.collection("news").limit(1).get().addOnSuccessListener(newsSnapshot -> {
            if (!newsSnapshot.isEmpty()) {
                Log.d(TAG, "News already exists, skipping initialization");
                callback.onSuccess();
                return;
            }
            
            // First add sample news structure
            addSampleNews(db);
            
            // Then scrape real news from MUST website
            MUSTNewsScraper.scrapeAndSaveNews(new MUSTNewsScraper.ScraperCallback() {
                @Override
                public void onSuccess(int newsCount) {
                    Log.d(TAG, "Successfully loaded " + newsCount + " news items from MUST website");
                    callback.onSuccess();
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading news from MUST website: " + error);
                    callback.onSuccess(); // Still succeed with sample data
                }
            });
        });
    }
    
    private static void addSampleNews(FirebaseFirestore db) {
        List<Map<String, Object>> newsList = new ArrayList<>();
        
        // News 1
        Map<String, Object> news1 = new HashMap<>();
        news1.put("title", "Welcome to MUST Alumni Portal");
        news1.put("summary", "Connect with fellow alumni, access exclusive opportunities, and stay updated with university news.");
        news1.put("content", "The MUST Alumni Portal is your gateway to lifelong connections with the university community.");
        news1.put("category", "UNIVERSITY");
        news1.put("author", "Alumni Office");
        news1.put("publishedAt", System.currentTimeMillis());
        news1.put("isPublished", true);
        newsList.add(news1);
        
        // News 2
        Map<String, Object> news2 = new HashMap<>();
        news2.put("title", "Mentorship Program Now Open");
        news2.put("summary", "Join our mentorship program and connect with experienced professionals in your field.");
        news2.put("content", "The alumni mentorship program connects students and recent graduates with experienced mentors.");
        news2.put("category", "UNIVERSITY");
        news2.put("author", "Mentorship Team");
        news2.put("publishedAt", System.currentTimeMillis() - 86400000);
        news2.put("isPublished", true);
        newsList.add(news2);
        
        // News 3
        Map<String, Object> news3 = new HashMap<>();
        news3.put("title", "Career Fair 2024 - Register Now");
        news3.put("summary", "Join us for the annual Career Fair featuring 50+ companies. Network with recruiters and explore opportunities.");
        news3.put("content", "The Career Fair brings together top employers and talented alumni for networking and recruitment.");
        news3.put("category", "UNIVERSITY");
        news3.put("author", "Career Services");
        news3.put("publishedAt", System.currentTimeMillis() - 172800000);
        news3.put("isPublished", true);
        newsList.add(news3);
        
        // Save all sample news
        for (Map<String, Object> news : newsList) {
            db.collection("news").add(news)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Added sample news: " + news.get("title"));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding sample news", e);
                });
        }
    }
}

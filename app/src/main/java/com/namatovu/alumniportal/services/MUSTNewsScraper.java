package com.namatovu.alumniportal.services;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.models.News;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to scrape news from MUST website and save to Firestore
 */
public class MUSTNewsScraper {
    
    private static final String TAG = "MUSTNewsScraper";
    private static final String MUST_NEWS_URL = "https://www.must.ac.ug/news";
    
    public interface ScraperCallback {
        void onSuccess(int newsCount);
        void onError(String error);
    }
    
    /**
     * Scrape news from MUST website and save to Firestore
     */
    public static void scrapeAndSaveNews(ScraperCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting to scrape news from: " + MUST_NEWS_URL);
                
                // Fetch the webpage
                Document doc = Jsoup.connect(MUST_NEWS_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
                
                List<Map<String, Object>> newsList = new ArrayList<>();
                
                // Try multiple selectors to find news items
                Elements newsItems = doc.select("article, .news-item, .post, .entry, .news-post, .blog-post, .item-list li, .news-list li");
                
                Log.d(TAG, "Found " + newsItems.size() + " potential news items");
                
                // If no items found, try broader search
                if (newsItems.isEmpty()) {
                    newsItems = doc.select("div[class*='news'], div[class*='post'], div[class*='article']");
                    Log.d(TAG, "Retrying with broader selectors, found: " + newsItems.size());
                }
                
                for (Element item : newsItems) {
                    try {
                        // Extract title - try multiple selectors
                        String title = null;
                        Element titleElement = item.selectFirst("h1, h2, h3, h4, .title, .headline, .post-title, .news-title");
                        if (titleElement != null) {
                            title = titleElement.text().trim();
                        }
                        
                        if (title == null || title.isEmpty()) continue;
                        
                        // Extract summary/content
                        String summary = null;
                        Element summaryElement = item.selectFirst("p, .summary, .excerpt, .content, .description, .post-excerpt");
                        if (summaryElement != null) {
                            summary = summaryElement.text().trim();
                            if (summary.length() > 250) {
                                summary = summary.substring(0, 250) + "...";
                            }
                        }
                        
                        if (summary == null || summary.isEmpty()) {
                            summary = title; // Use title as summary if no summary found
                        }
                        
                        // Extract date
                        long publishedAt = System.currentTimeMillis();
                        Element dateElement = item.selectFirst(".date, .published, time, .meta, .post-date, .news-date");
                        if (dateElement != null) {
                            String dateStr = dateElement.text();
                            Log.d(TAG, "Found date: " + dateStr);
                        }
                        
                        // Extract image URL
                        String imageUrl = null;
                        Element imgElement = item.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                if (!imageUrl.startsWith("http")) {
                                    imageUrl = "https://www.must.ac.ug" + imageUrl;
                                }
                            }
                        }
                        
                        // Extract link
                        String sourceUrl = null;
                        Element linkElement = item.selectFirst("a");
                        if (linkElement != null) {
                            sourceUrl = linkElement.attr("href");
                            if (sourceUrl != null && !sourceUrl.isEmpty()) {
                                if (!sourceUrl.startsWith("http")) {
                                    sourceUrl = "https://www.must.ac.ug" + sourceUrl;
                                }
                            }
                        }
                        
                        // Create news object
                        Map<String, Object> newsData = new HashMap<>();
                        newsData.put("title", title);
                        newsData.put("summary", summary);
                        newsData.put("content", summary);
                        newsData.put("category", "UNIVERSITY");
                        newsData.put("author", "MUST News");
                        newsData.put("publishedAt", publishedAt);
                        newsData.put("imageUrl", imageUrl != null ? imageUrl : "");
                        newsData.put("sourceUrl", sourceUrl != null ? sourceUrl : MUST_NEWS_URL);
                        newsData.put("isExternal", true);
                        newsData.put("isPublished", true);
                        
                        newsList.add(newsData);
                        Log.d(TAG, "Scraped: " + title);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing news item", e);
                    }
                }
                
                // Save to Firestore
                if (!newsList.isEmpty()) {
                    Log.d(TAG, "Total news to save: " + newsList.size());
                    saveToFirestore(newsList, callback);
                } else {
                    callback.onError("No news items found on MUST website");
                }
                
            } catch (IOException e) {
                Log.e(TAG, "Error scraping MUST website", e);
                callback.onError("Failed to connect to MUST website: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Save scraped news to Firestore
     */
    private static void saveToFirestore(List<Map<String, Object>> newsList, ScraperCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // First, get existing news titles to avoid duplicates
        db.collection("news")
            .get()
            .addOnSuccessListener(existingSnapshot -> {
                java.util.Set<String> existingTitles = new java.util.HashSet<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : existingSnapshot) {
                    String title = doc.getString("title");
                    if (title != null) {
                        existingTitles.add(title);
                    }
                }
                
                Log.d(TAG, "Existing news count: " + existingTitles.size());
                
                // Save only new news items
                int savedCount = 0;
                for (Map<String, Object> newsData : newsList) {
                    String title = (String) newsData.get("title");
                    
                    // Skip if already exists
                    if (existingTitles.contains(title)) {
                        Log.d(TAG, "Skipping duplicate: " + title);
                        continue;
                    }
                    
                    db.collection("news")
                        .add(newsData)
                        .addOnSuccessListener(docRef -> {
                            Log.d(TAG, "Saved news to Firestore: " + docRef.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving to Firestore", e);
                        });
                    savedCount++;
                }
                
                Log.d(TAG, "Saved " + savedCount + " new news items");
                callback.onSuccess(savedCount);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking existing news", e);
                // Still try to save even if check fails
                int savedCount = 0;
                for (Map<String, Object> newsData : newsList) {
                    db.collection("news")
                        .add(newsData)
                        .addOnSuccessListener(docRef -> {
                            Log.d(TAG, "Saved news to Firestore: " + docRef.getId());
                        })
                        .addOnFailureListener(e2 -> {
                            Log.e(TAG, "Error saving to Firestore", e2);
                        });
                    savedCount++;
                }
                callback.onSuccess(savedCount);
            });
    }
}

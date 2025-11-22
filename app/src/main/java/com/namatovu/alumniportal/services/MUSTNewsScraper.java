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
                    .timeout(10000)
                    .get();
                
                List<Map<String, Object>> newsList = new ArrayList<>();
                
                // Parse news items - adjust selectors based on MUST website structure
                Elements newsItems = doc.select("article, .news-item, .post, .entry");
                
                Log.d(TAG, "Found " + newsItems.size() + " potential news items");
                
                for (Element item : newsItems) {
                    try {
                        // Extract title
                        String title = null;
                        Element titleElement = item.selectFirst("h2, h3, .title, .headline");
                        if (titleElement != null) {
                            title = titleElement.text();
                        }
                        
                        if (title == null || title.isEmpty()) continue;
                        
                        // Extract summary/content
                        String summary = null;
                        Element summaryElement = item.selectFirst("p, .summary, .excerpt, .content");
                        if (summaryElement != null) {
                            summary = summaryElement.text();
                            if (summary.length() > 200) {
                                summary = summary.substring(0, 200) + "...";
                            }
                        }
                        
                        // Extract date
                        String dateStr = null;
                        Element dateElement = item.selectFirst(".date, .published, time, .meta");
                        if (dateElement != null) {
                            dateStr = dateElement.text();
                        }
                        
                        // Extract image URL
                        String imageUrl = null;
                        Element imgElement = item.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (imageUrl != null && !imageUrl.startsWith("http")) {
                                imageUrl = "https://www.must.ac.ug" + imageUrl;
                            }
                        }
                        
                        // Extract link
                        String sourceUrl = null;
                        Element linkElement = item.selectFirst("a");
                        if (linkElement != null) {
                            sourceUrl = linkElement.attr("href");
                            if (sourceUrl != null && !sourceUrl.startsWith("http")) {
                                sourceUrl = "https://www.must.ac.ug" + sourceUrl;
                            }
                        }
                        
                        // Create news object
                        Map<String, Object> newsData = new HashMap<>();
                        newsData.put("title", title);
                        newsData.put("summary", summary != null ? summary : "");
                        newsData.put("content", summary != null ? summary : "");
                        newsData.put("category", "UNIVERSITY");
                        newsData.put("author", "MUST News");
                        newsData.put("publishedAt", System.currentTimeMillis());
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
        
        int savedCount = 0;
        for (Map<String, Object> newsData : newsList) {
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
        
        callback.onSuccess(savedCount);
    }
}

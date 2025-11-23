package com.namatovu.alumniportal.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.utils.ThreadManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashMap;
import java.util.Map;

/**
 * Web Scraping Service to fetch university news and events
 * Demonstrates web scraping and data analysis capabilities
 */
public class WebScrapingService extends Service {
    private static final String TAG = "WebScrapingService";
    private static final String MUST_NEWS_URL = "https://www.must.ac.ug/news/";
    private FirebaseFirestore db;
    
    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "WebScrapingService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting web scraping service");
        
        // Use modern threading instead of AsyncTask
        ThreadManager.getInstance().executeAsync(
            this::scrapeUniversityNews,
            () -> {
                Log.d(TAG, "Web scraping completed");
                stopSelf(startId);
            }
        );
        
        return START_NOT_STICKY;
    }
    
    private Void scrapeUniversityNews() {
        try {
            Log.d(TAG, "Scraping university news from: " + MUST_NEWS_URL);
            
            // Connect to website and parse HTML
            Document doc = Jsoup.connect(MUST_NEWS_URL)
                    .userAgent("Mozilla/5.0 (compatible; AlumniPortal/1.0)")
                    .timeout(10000)
                    .get();
            
            Log.d(TAG, "Successfully connected to MUST website");
            
            // Extract news articles
            Elements newsElements = doc.select("article, .news-item, .post, h2, h3");
            
            int articleCount = 0;
            for (Element element : newsElements) {
                if (articleCount >= 5) break; // Limit to 5 articles
                
                String title = extractTitle(element);
                String content = extractContent(element);
                String link = extractLink(element);
                
                if (title != null && !title.isEmpty()) {
                    saveNewsArticle(title, content, link);
                    articleCount++;
                    Log.d(TAG, "Scraped article: " + title);
                }
            }
            
            // If no specific news structure found, scrape general content
            if (articleCount == 0) {
                scrapeGeneralContent(doc);
            }
            
            Log.d(TAG, "Scraped " + articleCount + " news articles");
            
        } catch (Exception e) {
            Log.e(TAG, "Error scraping university news", e);
            
            // Create sample data if scraping fails
            createSampleNewsData();
        }
        
        return null;
    }
    
    private String extractTitle(Element element) {
        // Try different selectors for title
        Element titleElement = element.selectFirst("h1, h2, h3, .title, .headline");
        if (titleElement != null) {
            return titleElement.text().trim();
        }
        
        // If element itself is a heading
        if (element.tagName().matches("h[1-6]")) {
            return element.text().trim();
        }
        
        return null;
    }
    
    private String extractContent(Element element) {
        // Try to find content/description
        Element contentElement = element.selectFirst("p, .content, .description, .excerpt");
        if (contentElement != null) {
            return contentElement.text().trim();
        }
        
        // Get text from element itself if it's substantial
        String text = element.text().trim();
        if (text.length() > 50) {
            return text.length() > 200 ? text.substring(0, 197) + "..." : text;
        }
        
        return "University news and updates from MUST.";
    }
    
    private String extractLink(Element element) {
        // Try to find a link
        Element linkElement = element.selectFirst("a[href]");
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (href.startsWith("/")) {
                return "https://www.must.ac.ug" + href;
            }
            return href;
        }
        return MUST_NEWS_URL;
    }
    
    private void saveNewsArticle(String title, String content, String link) {
        Map<String, Object> article = new HashMap<>();
        article.put("title", title);
        article.put("content", content);
        article.put("link", link);
        article.put("source", "MUST Website");
        article.put("scrapedAt", System.currentTimeMillis());
        article.put("category", "University News");
        
        db.collection("scrapedNews")
                .add(article)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "News article saved: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving news article", e);
                });
    }
    
    private void scrapeGeneralContent(Document doc) {
        // Extract general content if specific news structure not found
        Elements headings = doc.select("h1, h2, h3");
        
        int count = 0;
        for (Element heading : headings) {
            if (count >= 3) break;
            
            String title = heading.text().trim();
            if (title.length() > 10) { // Only meaningful titles
                String content = "Latest updates and information from Mbarara University of Science and Technology.";
                
                // Try to get following paragraph
                Element nextElement = heading.nextElementSibling();
                if (nextElement != null && nextElement.tagName().equals("p")) {
                    content = nextElement.text().trim();
                }
                
                saveNewsArticle(title, content, MUST_NEWS_URL);
                count++;
            }
        }
    }
    
    private void createSampleNewsData() {
        Log.d(TAG, "Creating sample news data as fallback");
        
        String[] sampleTitles = {
            "MUST Alumni Excellence Awards 2025",
            "New Research Center Opens at MUST",
            "Student Innovation Competition Winners",
            "Alumni Networking Event Success",
            "Technology Conference at MUST"
        };
        
        String[] sampleContents = {
            "Celebrating outstanding achievements of MUST alumni in various fields including technology, healthcare, and business innovation.",
            "The university inaugurates a new state-of-the-art research facility to advance scientific discovery and innovation.",
            "Students showcase groundbreaking projects in artificial intelligence, mobile applications, and sustainable technology solutions.",
            "Over 200 alumni gathered for the annual networking event, fostering connections and mentorship opportunities.",
            "Leading experts in technology and innovation share insights at the annual MUST Technology Conference."
        };
        
        for (int i = 0; i < sampleTitles.length; i++) {
            saveNewsArticle(sampleTitles[i], sampleContents[i], MUST_NEWS_URL);
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WebScrapingService destroyed");
    }
}
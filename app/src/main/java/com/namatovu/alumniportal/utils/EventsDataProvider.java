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
        // Load static news directly
        callback.onNewsLoaded(getStaticFallbackNews());
    }
    
    public static List<News> getNews() {
        // Return static news directly
        return getStaticFallbackNews();
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
                
                // If no news found, use fallback static news
                if (newsList.isEmpty()) {
                    Log.d(TAG, "No news in Firestore, using fallback static news");
                    newsList.addAll(getStaticFallbackNews());
                }
                
                callback.onNewsLoaded(newsList);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading news from Firestore", e);
                // Use fallback static news on error
                callback.onNewsLoaded(getStaticFallbackNews());
            });
    }
    
    private static List<News> getStaticFallbackNews() {
        List<News> newsList = new ArrayList<>();
        
        // Latest News - Updated November 2025
        News news1 = new News();
        news1.setTitle("MUST Hosts Annual Technology Summit 2025");
        news1.setSummary("Leading tech innovators and industry experts gather at MUST for the annual Technology Summit featuring keynote speeches and networking sessions.");
        news1.setContent("Mbarara University of Science and Technology hosted its annual Technology Summit 2025, bringing together leading tech innovators, industry experts, and students. The summit featured keynote presentations on AI, cloud computing, and digital transformation, along with networking sessions connecting students with potential employers and mentors.");
        news1.setCategory(News.Category.UNIVERSITY);
        news1.setAuthor("MUST Communications");
        news1.setPublishedAt(System.currentTimeMillis() - 86400000);
        newsList.add(news1);
        
        News news2 = new News();
        news2.setTitle("MUST Launches Innovation Hub");
        news2.setSummary("Mbarara University launches new Innovation Hub to foster entrepreneurship and technological advancement among students and staff.");
        news2.setContent("Mbarara University of Science and Technology has officially launched its state-of-the-art Innovation Hub to foster entrepreneurship and technological advancement among students and staff. The hub provides resources, mentorship, and funding opportunities for student-led startups and research projects. The facility includes co-working spaces, prototyping labs, and access to industry mentors.");
        news2.setCategory(News.Category.UNIVERSITY);
        news2.setAuthor("MUST News");
        news2.setPublishedAt(System.currentTimeMillis() - 172800000);
        newsList.add(news2);
        
        News news3 = new News();
        news3.setTitle("MUST Receives Accreditation for New Engineering Programs");
        news3.setSummary("The university receives international accreditation for its newly developed engineering and technology programs.");
        news3.setContent("Mbarara University has received international accreditation for its newly developed engineering and technology programs. This recognition validates the quality of education and prepares graduates for global opportunities in the technology sector. The accreditation covers Software Engineering, Civil Engineering, and Electrical Engineering programs.");
        news3.setCategory(News.Category.ACADEMICS);
        news3.setAuthor("MUST News");
        news3.setPublishedAt(System.currentTimeMillis() - 259200000);
        newsList.add(news3);
        
        News news4 = new News();
        news4.setTitle("MUST Research Team Develops Agricultural Innovation");
        news4.setSummary("MUST researchers develop breakthrough technology to improve crop yields and water efficiency in East African farming.");
        news4.setContent("A team of MUST researchers has developed an innovative agricultural technology that increases crop yield by 40% while reducing water usage. This breakthrough has potential to transform sustainable farming practices across East Africa. The technology uses IoT sensors and AI-powered analytics to optimize irrigation and fertilizer application.");
        news4.setCategory(News.Category.RESEARCH);
        news4.setAuthor("MUST News");
        news4.setPublishedAt(System.currentTimeMillis() - 345600000);
        newsList.add(news4);
        
        News news5 = new News();
        news5.setTitle("Alumni Network Reaches 10,000 Members Milestone");
        news5.setSummary("MUST Alumni Network celebrates reaching 10,000 active members worldwide, strengthening global connections.");
        news5.setContent("The MUST Alumni Network has grown to over 10,000 active members worldwide, strengthening connections and opportunities for graduates across the globe. The network continues to facilitate mentorship, career development, and collaborative initiatives. Members are now active in 45 countries across Africa, Europe, North America, and Asia.");
        news5.setCategory(News.Category.ALUMNI);
        news5.setAuthor("MUST News");
        news5.setPublishedAt(System.currentTimeMillis() - 432000000);
        newsList.add(news5);
        
        News news6 = new News();
        news6.setTitle("MUST Career Fair 2025 Attracts Top Employers");
        news6.setSummary("Annual Career Fair brings together 60+ leading companies offering internships and employment opportunities to students.");
        news6.setContent("MUST's annual Career Fair brought together over 60 leading companies and organizations, offering internship and employment opportunities to hundreds of students. The event featured workshops on resume writing, interview skills, and direct recruitment interviews. Companies included tech giants, financial institutions, and NGOs.");
        news6.setCategory(News.Category.GENERAL);
        news6.setAuthor("MUST News");
        news6.setPublishedAt(System.currentTimeMillis() - 518400000);
        newsList.add(news6);
        
        News news7 = new News();
        news7.setTitle("MUST Partners with International Universities");
        news7.setSummary("New partnerships established with leading international universities for student exchange and research collaboration.");
        news7.setContent("Mbarara University has established partnerships with leading international universities to facilitate student exchange programs and collaborative research initiatives. These partnerships enhance academic excellence and provide students with global learning opportunities. Partner institutions include universities in Germany, Canada, and South Africa.");
        news7.setCategory(News.Category.UNIVERSITY);
        news7.setAuthor("MUST News");
        news7.setPublishedAt(System.currentTimeMillis() - 604800000);
        newsList.add(news7);
        
        News news8 = new News();
        news8.setTitle("MUST Wins National Science Competition");
        news8.setSummary("MUST students win first place in the National Science and Innovation Competition with their renewable energy project.");
        news8.setContent("A team of MUST students won first place in the National Science and Innovation Competition with their innovative renewable energy project. The project focuses on developing affordable solar-powered water purification systems for rural communities. The team received a cash prize and opportunity to present at international conferences.");
        news8.setCategory(News.Category.ACADEMICS);
        news8.setAuthor("MUST News");
        news8.setPublishedAt(System.currentTimeMillis() - 691200000);
        newsList.add(news8);
        
        News news9 = new News();
        news9.setTitle("New Scholarship Program for Underprivileged Students");
        news9.setSummary("MUST launches comprehensive scholarship program to support talented students from disadvantaged backgrounds.");
        news9.setContent("Mbarara University has launched a comprehensive scholarship program aimed at supporting talented students from disadvantaged backgrounds. The program covers tuition, accommodation, and living expenses for 100 students annually. Applications are now open for the 2025/2026 academic year.");
        news9.setCategory(News.Category.UNIVERSITY);
        news9.setAuthor("MUST News");
        news9.setPublishedAt(System.currentTimeMillis() - 777600000);
        newsList.add(news9);
        
        News news10 = new News();
        news10.setTitle("MUST Hosts International Conference on Sustainable Development");
        news10.setSummary("University hosts three-day international conference bringing together researchers and policymakers to discuss sustainable development goals.");
        news10.setContent("Mbarara University hosted a three-day international conference on Sustainable Development, bringing together researchers, policymakers, and industry leaders from across Africa. The conference featured presentations on climate change, renewable energy, and sustainable agriculture. Over 500 participants attended from 20 countries.");
        news10.setCategory(News.Category.RESEARCH);
        news10.setAuthor("MUST News");
        news10.setPublishedAt(System.currentTimeMillis() - 864000000);
        newsList.add(news10);
        
        return newsList;
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

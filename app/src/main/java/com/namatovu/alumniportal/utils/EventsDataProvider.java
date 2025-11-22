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
        return getStaticFallbackEvents();
    }
    
    private static List<Event> getStaticFallbackEvents() {
        List<Event> eventsList = new ArrayList<>();
        
        Event event1 = new Event();
        event1.setTitle("Mentorship Program Launch");
        event1.setSummary("Join our new mentorship program connecting alumni with current students for career guidance and professional development.");
        event1.setDescription("The MUST Alumni Mentorship Program is launching this month. Connect with experienced alumni mentors who can guide you through career development, industry insights, and professional networking. Registration is open for both mentors and mentees.");
        event1.setDateTime(System.currentTimeMillis() + 604800000); // 1 week from now
        event1.setLocation("MUST Main Campus, Auditorium");
        event1.setCategory(Event.Category.MENTORSHIP);
        event1.setOrganizerName("MUST Alumni Office");
        event1.setRegistrationUrl("https://must.ac.ug/events/mentorship-program-2025");
        event1.setMaxParticipants(100);
        event1.setCurrentParticipants(45);
        eventsList.add(event1);
        
        Event event2 = new Event();
        event2.setTitle("Leadership Summit 2025");
        event2.setSummary("Annual leadership summit featuring keynote speakers, workshops, and networking sessions for emerging leaders.");
        event2.setDescription("The MUST Leadership Summit brings together student leaders, alumni, and industry professionals to discuss leadership challenges and opportunities. Featuring keynote speakers from Fortune 500 companies and interactive workshops on strategic leadership.");
        event2.setDateTime(System.currentTimeMillis() + 1209600000); // 2 weeks from now
        event2.setLocation("MUST Convention Center");
        event2.setCategory(Event.Category.LEADERSHIP);
        event2.setOrganizerName("MUST Student Affairs");
        event2.setRegistrationUrl("https://must.ac.ug/events/leadership-summit-2025");
        event2.setMaxParticipants(200);
        event2.setCurrentParticipants(120);
        eventsList.add(event2);
        
        Event event3 = new Event();
        event3.setTitle("Alumni Networking Breakfast");
        event3.setSummary("Casual networking breakfast for alumni to reconnect, share experiences, and explore collaboration opportunities.");
        event3.setDescription("Join fellow MUST alumni for a morning networking breakfast. This is a great opportunity to reconnect with classmates, share career experiences, and explore potential business collaborations. Light refreshments will be served.");
        event3.setDateTime(System.currentTimeMillis() + 345600000); // 4 days from now
        event3.setLocation("MUST Alumni Center, Dining Hall");
        event3.setCategory(Event.Category.NETWORKING);
        event3.setOrganizerName("MUST Alumni Association");
        event3.setRegistrationUrl("https://must.ac.ug/events/alumni-breakfast-2025");
        event3.setMaxParticipants(80);
        event3.setCurrentParticipants(62);
        eventsList.add(event3);
        
        Event event4 = new Event();
        event4.setTitle("Career Development Workshop");
        event4.setSummary("Learn essential skills for career advancement including resume writing, interview techniques, and salary negotiation.");
        event4.setDescription("This comprehensive workshop covers key career development topics: crafting compelling resumes, mastering interview techniques, negotiating salaries, and building professional networks. Expert facilitators from HR and recruitment industries will lead interactive sessions.");
        event4.setDateTime(System.currentTimeMillis() + 864000000); // 10 days from now
        event4.setLocation("MUST Business School, Room 201");
        event4.setCategory(Event.Category.CAREER);
        event4.setOrganizerName("MUST Career Services");
        event4.setRegistrationUrl("https://must.ac.ug/events/career-workshop-2025");
        event4.setMaxParticipants(150);
        event4.setCurrentParticipants(98);
        eventsList.add(event4);
        
        Event event5 = new Event();
        event5.setTitle("Innovation Showcase");
        event5.setSummary("Showcase of student and alumni innovation projects, startups, and research initiatives with investor pitching opportunities.");
        event5.setDescription("The MUST Innovation Showcase highlights groundbreaking projects from students and alumni. Startups will pitch to investors, researchers will present findings, and attendees can explore cutting-edge innovations in technology, agriculture, and social impact.");
        event5.setDateTime(System.currentTimeMillis() + 1814400000); // 3 weeks from now
        event5.setLocation("MUST Innovation Hub");
        event5.setCategory(Event.Category.TECHNOLOGY);
        event5.setOrganizerName("MUST Innovation Office");
        event5.setRegistrationUrl("https://must.ac.ug/events/innovation-showcase-2025");
        event5.setMaxParticipants(250);
        event5.setCurrentParticipants(180);
        eventsList.add(event5);
        
        return eventsList;
    }
    
    public static void getNewsAsync(NewsCallback callback) {
        // Load static news directly
        callback.onNewsLoaded(getStaticFallbackNews());
    }
    
    public static List<News> getNews() {
        // Try to load from Firestore (which has scraped news)
        // This is a synchronous wrapper - in production use getNewsAsync
        return loadNewsFromFirestoreSync();
    }
    
    private static List<News> loadNewsFromFirestoreSync() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<News> newsList = new ArrayList<>();
        
        try {
            // This is a blocking call - use with caution
            com.google.firebase.firestore.QuerySnapshot snapshot = 
                com.google.android.gms.tasks.Tasks.await(
                    db.collection("news")
                        .orderBy("publishedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(50)
                        .get()
                );
            
            Log.d(TAG, "Found " + snapshot.size() + " news items from Firestore");
            
            for (QueryDocumentSnapshot document : snapshot) {
                try {
                    News news = new News();
                    news.setId(document.getId());
                    news.setTitle(document.getString("title"));
                    news.setSummary(document.getString("summary"));
                    news.setContent(document.getString("content"));
                    news.setImageUrl(document.getString("imageUrl"));
                    news.setSourceUrl(document.getString("sourceUrl"));
                    news.setPublishedAt(document.getLong("publishedAt") != null ? document.getLong("publishedAt") : System.currentTimeMillis());
                    news.setAuthor(document.getString("author") != null ? document.getString("author") : "MUST News");
                    
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
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading news from Firestore", e);
            // Use fallback static news on error
            newsList.addAll(getStaticFallbackNews());
        }
        
        return newsList;
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
                        news.setImageUrl(document.getString("imageUrl"));
                        news.setSourceUrl(document.getString("sourceUrl"));
                        news.setPublishedAt(document.getLong("publishedAt") != null ? document.getLong("publishedAt") : System.currentTimeMillis());
                        news.setAuthor(document.getString("author") != null ? document.getString("author") : "MUST News");
                        
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
        news1.setSourceUrl("https://must.ac.ug/news/technology-summit-2025");
        news1.setImageUrl("https://must.ac.ug/images/tech-summit-2025.jpg");
        news1.setPublishedAt(System.currentTimeMillis() - 86400000);
        newsList.add(news1);
        
        News news2 = new News();
        news2.setTitle("MUST Launches Innovation Hub");
        news2.setSummary("Mbarara University launches new Innovation Hub to foster entrepreneurship and technological advancement among students and staff.");
        news2.setContent("Mbarara University of Science and Technology has officially launched its state-of-the-art Innovation Hub to foster entrepreneurship and technological advancement among students and staff. The hub provides resources, mentorship, and funding opportunities for student-led startups and research projects. The facility includes co-working spaces, prototyping labs, and access to industry mentors.");
        news2.setCategory(News.Category.UNIVERSITY);
        news2.setAuthor("MUST News");
        news2.setSourceUrl("https://must.ac.ug/news/innovation-hub-launch");
        news2.setImageUrl("https://must.ac.ug/images/innovation-hub.jpg");
        news2.setPublishedAt(System.currentTimeMillis() - 172800000);
        newsList.add(news2);
        
        News news3 = new News();
        news3.setTitle("MUST Receives Accreditation for New Engineering Programs");
        news3.setSummary("The university receives international accreditation for its newly developed engineering and technology programs.");
        news3.setContent("Mbarara University has received international accreditation for its newly developed engineering and technology programs. This recognition validates the quality of education and prepares graduates for global opportunities in the technology sector. The accreditation covers Software Engineering, Civil Engineering, and Electrical Engineering programs.");
        news3.setCategory(News.Category.ACADEMICS);
        news3.setAuthor("MUST News");
        news3.setSourceUrl("https://must.ac.ug/news/engineering-accreditation");
        news3.setImageUrl("https://must.ac.ug/images/engineering-accreditation.jpg");
        news3.setPublishedAt(System.currentTimeMillis() - 259200000);
        newsList.add(news3);
        
        News news4 = new News();
        news4.setTitle("MUST Research Team Develops Agricultural Innovation");
        news4.setSummary("MUST researchers develop breakthrough technology to improve crop yields and water efficiency in East African farming.");
        news4.setContent("A team of MUST researchers has developed an innovative agricultural technology that increases crop yield by 40% while reducing water usage. This breakthrough has potential to transform sustainable farming practices across East Africa. The technology uses IoT sensors and AI-powered analytics to optimize irrigation and fertilizer application.");
        news4.setCategory(News.Category.RESEARCH);
        news4.setAuthor("MUST News");
        news4.setSourceUrl("https://must.ac.ug/news/agricultural-innovation");
        news4.setImageUrl("https://must.ac.ug/images/agricultural-innovation.jpg");
        news4.setPublishedAt(System.currentTimeMillis() - 345600000);
        newsList.add(news4);
        
        News news5 = new News();
        news5.setTitle("Alumni Network Reaches 10,000 Members Milestone");
        news5.setSummary("MUST Alumni Network celebrates reaching 10,000 active members worldwide, strengthening global connections.");
        news5.setContent("The MUST Alumni Network has grown to over 10,000 active members worldwide, strengthening connections and opportunities for graduates across the globe. The network continues to facilitate mentorship, career development, and collaborative initiatives. Members are now active in 45 countries across Africa, Europe, North America, and Asia.");
        news5.setCategory(News.Category.ALUMNI);
        news5.setAuthor("MUST News");
        news5.setSourceUrl("https://must.ac.ug/news/alumni-milestone");
        news5.setImageUrl("https://must.ac.ug/images/alumni-milestone.jpg");
        news5.setPublishedAt(System.currentTimeMillis() - 432000000);
        newsList.add(news5);
        
        News news6 = new News();
        news6.setTitle("MUST Career Fair 2025 Attracts Top Employers");
        news6.setSummary("Annual Career Fair brings together 60+ leading companies offering internships and employment opportunities to students.");
        news6.setContent("MUST's annual Career Fair brought together over 60 leading companies and organizations, offering internship and employment opportunities to hundreds of students. The event featured workshops on resume writing, interview skills, and direct recruitment interviews. Companies included tech giants, financial institutions, and NGOs.");
        news6.setCategory(News.Category.GENERAL);
        news6.setAuthor("MUST News");
        news6.setSourceUrl("https://must.ac.ug/news/career-fair-2025");
        news6.setImageUrl("https://must.ac.ug/images/career-fair-2025.jpg");
        news6.setPublishedAt(System.currentTimeMillis() - 518400000);
        newsList.add(news6);
        
        News news7 = new News();
        news7.setTitle("MUST Partners with International Universities");
        news7.setSummary("New partnerships established with leading international universities for student exchange and research collaboration.");
        news7.setContent("Mbarara University has established partnerships with leading international universities to facilitate student exchange programs and collaborative research initiatives. These partnerships enhance academic excellence and provide students with global learning opportunities. Partner institutions include universities in Germany, Canada, and South Africa.");
        news7.setCategory(News.Category.UNIVERSITY);
        news7.setAuthor("MUST News");
        news7.setSourceUrl("https://must.ac.ug/news/international-partnerships");
        news7.setImageUrl("https://must.ac.ug/images/international-partnerships.jpg");
        news7.setPublishedAt(System.currentTimeMillis() - 604800000);
        newsList.add(news7);
        
        News news8 = new News();
        news8.setTitle("MUST Wins National Science Competition");
        news8.setSummary("MUST students win first place in the National Science and Innovation Competition with their renewable energy project.");
        news8.setContent("A team of MUST students won first place in the National Science and Innovation Competition with their innovative renewable energy project. The project focuses on developing affordable solar-powered water purification systems for rural communities. The team received a cash prize and opportunity to present at international conferences.");
        news8.setCategory(News.Category.ACADEMICS);
        news8.setAuthor("MUST News");
        news8.setSourceUrl("https://must.ac.ug/news/science-competition-win");
        news8.setImageUrl("https://must.ac.ug/images/science-competition.jpg");
        news8.setPublishedAt(System.currentTimeMillis() - 691200000);
        newsList.add(news8);
        
        News news9 = new News();
        news9.setTitle("New Scholarship Program for Underprivileged Students");
        news9.setSummary("MUST launches comprehensive scholarship program to support talented students from disadvantaged backgrounds.");
        news9.setContent("Mbarara University has launched a comprehensive scholarship program aimed at supporting talented students from disadvantaged backgrounds. The program covers tuition, accommodation, and living expenses for 100 students annually. Applications are now open for the 2025/2026 academic year.");
        news9.setCategory(News.Category.UNIVERSITY);
        news9.setAuthor("MUST News");
        news9.setSourceUrl("https://must.ac.ug/news/scholarship-program");
        news9.setImageUrl("https://must.ac.ug/images/scholarship-program.jpg");
        news9.setPublishedAt(System.currentTimeMillis() - 777600000);
        newsList.add(news9);
        
        News news10 = new News();
        news10.setTitle("MUST Hosts International Conference on Sustainable Development");
        news10.setSummary("University hosts three-day international conference bringing together researchers and policymakers to discuss sustainable development goals.");
        news10.setContent("Mbarara University hosted a three-day international conference on Sustainable Development, bringing together researchers, policymakers, and industry leaders from across Africa. The conference featured presentations on climate change, renewable energy, and sustainable agriculture. Over 500 participants attended from 20 countries.");
        news10.setCategory(News.Category.RESEARCH);
        news10.setAuthor("MUST News");
        news10.setSourceUrl("https://must.ac.ug/news/sustainable-development-conference");
        news10.setImageUrl("https://must.ac.ug/images/sustainable-conference.jpg");
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
    
    /**
     * Trigger scraping of MUST website news
     * Call this periodically to keep news updated
     */
    public static void refreshNewsFromMUSTWebsite() {
        Log.d(TAG, "Triggering MUST website news scrape");
        MUSTNewsScraper.scrapeAndSaveNews(new MUSTNewsScraper.ScraperCallback() {
            @Override
            public void onSuccess(int newsCount) {
                Log.d(TAG, "Successfully scraped " + newsCount + " news items from MUST website");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error scraping MUST website: " + error);
            }
        });
    }
}

package com.namatovu.alumniportal.utils;

import com.namatovu.alumniportal.models.Event;
import com.namatovu.alumniportal.models.News;
import com.namatovu.alumniportal.models.EventsAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Data provider for Events, News & Insights
 * Generates realistic sample data for the Events News page
 */
public class EventsDataProvider {
    
    private static final Random random = new Random();
    
    /**
     * Generate sample events with realistic data
     */
    public static List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        
        // Get current time for realistic dates
        Calendar calendar = Calendar.getInstance();
        
        // Sample events data
        String[][] eventsData = {
            {"Leadership Excellence Workshop", "Join us for an intensive leadership development workshop designed to enhance your leadership skills and network with fellow alumni professionals.", "MUST Conference Hall", "Alumni Relations Office"},
            {"Alumni Mentorship Program Launch", "Connect with experienced alumni mentors who can guide your career journey and professional development in various fields.", "Online Event", "Career Development Center"},
            {"Tech Innovation Summit 2024", "Explore the latest trends in technology and innovation with keynote speakers from top tech companies and startups.", "MUST Innovation Hub", "Computer Science Department"},
            {"Healthcare Leadership Conference", "Leading medical professionals share insights on healthcare management, patient care, and medical research breakthroughs.", "Medical School Auditorium", "Faculty of Medicine"},
            {"Alumni Networking Mixer", "Meet fellow MUST alumni in your area, share experiences, and build meaningful professional connections over cocktails.", "Mbarara Country Club", "Alumni Association"},
            {"Research Showcase Symposium", "Faculty and student researchers present their latest findings and innovations across various disciplines.", "University Main Hall", "Research Department"},
            {"Career Fair 2024", "Connect with top employers, explore job opportunities, and advance your career with companies actively hiring MUST graduates.", "Sports Complex", "Career Services"},
            {"Women in Leadership Panel", "Inspiring female leaders share their journey, challenges, and advice for aspiring women in leadership positions.", "Student Center", "Women's Network"}
        };
        
        Event.Category[] categories = {
            Event.Category.LEADERSHIP,
            Event.Category.MENTORSHIP,
            Event.Category.TECHNOLOGY,
            Event.Category.UNIVERSITY,
            Event.Category.NETWORKING,
            Event.Category.CAREER,
            Event.Category.UNIVERSITY,
            Event.Category.LEADERSHIP
        };
        
        // Generate events with varying dates
        for (int i = 0; i < eventsData.length; i++) {
            // Create dates ranging from past week to next month
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_MONTH, random.nextInt(45) - 15); // -15 to +30 days
            calendar.add(Calendar.HOUR_OF_DAY, random.nextInt(12) + 8); // 8 AM to 8 PM
            
            Event event = new Event(
                eventsData[i][0],
                eventsData[i][1] + " This event offers valuable networking opportunities and professional development for MUST alumni and current students.",
                eventsData[i][1],
                calendar.getTimeInMillis(),
                eventsData[i][2],
                categories[i]
            );
            
            event.setId("event_" + i);
            event.setOrganizerName(eventsData[i][3]);
            event.setMaxParticipants(30 + random.nextInt(70)); // 30-100 participants
            event.setCurrentParticipants(random.nextInt(event.getMaxParticipants()));
            event.setOnline(eventsData[i][2].contains("Online"));
            
            // Set registration URL for some events
            if (random.nextBoolean()) {
                event.setRegistrationUrl("https://must.ac.ug/events/register/" + event.getId());
            }
            
            // Set status based on date
            if (calendar.getTimeInMillis() < System.currentTimeMillis() - 86400000) { // Past events
                event.setStatus(Event.Status.COMPLETED);
            } else if (calendar.getTimeInMillis() < System.currentTimeMillis() + 86400000) { // Events within 24 hours
                event.setStatus(Event.Status.ONGOING);
            } else {
                event.setStatus(Event.Status.UPCOMING);
            }
            
            events.add(event);
        }
        
        return events;
    }
    
    /**
     * Generate sample news articles
     */
    public static List<News> getNews() {
        List<News> newsList = new ArrayList<>();
        
        // Sample news data
        String[][] newsData = {
            {"MUST Announces New Research Center for Artificial Intelligence", "Mbarara University of Science and Technology has announced the establishment of a new research center focusing on artificial intelligence and machine learning applications in healthcare and agriculture.", "MUST Communications", "University"},
            {"Alumni Scholarship Fund Reaches UGX 500 Million Milestone", "The MUST Alumni Association has successfully raised over 500 million Uganda Shillings to support underprivileged students in pursuing their dreams at the university.", "Alumni Relations", "Alumni"},
            {"Medical School Receives International Accreditation", "The Faculty of Medicine at MUST has received prestigious international accreditation, placing it among the top medical schools in East Africa.", "Dr. Sarah Nakimuli", "Academics"},
            {"Students Win National Innovation Challenge", "A team of MUST computer science students has won the national innovation challenge with their groundbreaking mobile health application.", "Tech Reporter", "Technology"},
            {"New Partnership with Leading Healthcare Organizations", "MUST has signed memorandums of understanding with several leading healthcare organizations to enhance practical training and research opportunities.", "Partnership Office", "University"},
            {"Research Breakthrough in Tropical Disease Treatment", "MUST researchers have made a significant breakthrough in developing cost-effective treatments for neglected tropical diseases affecting rural communities.", "Research Department", "Research"},
            {"Alumni Success Story: From Student to CEO", "Meet Jane Tumuhairwe, a MUST alumna who has risen to become the CEO of a major technology company in Kampala, inspiring current students.", "Alumni Magazine", "Alumni"},
            {"New Campus Infrastructure Development Update", "Construction of the new state-of-the-art library and student center is progressing well, with completion expected by the end of 2024.", "Infrastructure Team", "University"}
        };
        
        News.Category[] categories = {
            News.Category.UNIVERSITY,
            News.Category.ALUMNI,
            News.Category.ACADEMICS,
            News.Category.TECHNOLOGY,
            News.Category.UNIVERSITY,
            News.Category.RESEARCH,
            News.Category.ALUMNI,
            News.Category.UNIVERSITY
        };
        
        // Generate news with varying publication dates
        Calendar calendar = Calendar.getInstance();
        
        for (int i = 0; i < newsData.length; i++) {
            // Create dates ranging from past 2 weeks
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_MONTH, -(random.nextInt(14) + 1)); // 1-14 days ago
            calendar.add(Calendar.HOUR_OF_DAY, -(random.nextInt(12))); // Random hours
            
            News news = new News(
                newsData[i][0],
                newsData[i][1] + " This development represents MUST's commitment to excellence and innovation in higher education, research, and community service. The university continues to play a leading role in advancing knowledge and creating positive impact in Uganda and the East African region.",
                newsData[i][1],
                categories[i],
                newsData[i][2]
            );
            
            news.setId("news_" + i);
            news.setPublishedAt(calendar.getTimeInMillis());
            news.setViewCount(random.nextInt(500) + 50); // 50-550 views
            news.setFeatured(i < 3); // First 3 are featured
            news.setExternal(random.nextBoolean()); // Some from external sources
            
            // Set source URL for external articles
            if (news.isExternal()) {
                news.setSourceUrl("https://must.ac.ug/news/article/" + news.getId());
            }
            
            newsList.add(news);
        }
        
        return newsList;
    }
    
    /**
     * Calculate analytics based on events and news data
     */
    public static EventsAnalytics getAnalytics(List<Event> events, List<News> news) {
        EventsAnalytics analytics = new EventsAnalytics();
        
        // Count totals
        analytics.setTotalEvents(events.size());
        analytics.setTotalArticles(news.size());
        
        // Count upcoming and completed events
        int upcomingEvents = 0;
        int completedEvents = 0;
        
        for (Event event : events) {
            if (event.getStatus() == Event.Status.UPCOMING || event.getStatus() == Event.Status.ONGOING) {
                upcomingEvents++;
            } else if (event.getStatus() == Event.Status.COMPLETED) {
                completedEvents++;
            }
            
            // Count event categories
            if (event.getCategory() != null) {
                analytics.incrementEventCategory(event.getCategory().getDisplayName());
            }
        }
        
        analytics.setTotalUpcomingEvents(upcomingEvents);
        analytics.setTotalCompletedEvents(completedEvents);
        
        // Count news categories
        for (News newsItem : news) {
            if (newsItem.getCategory() != null) {
                analytics.incrementNewsCategory(newsItem.getCategory().getDisplayName());
            }
        }
        
        analytics.updateAnalytics();
        
        return analytics;
    }
    
    /**
     * Get featured/trending content
     */
    public static List<News> getFeaturedNews(List<News> allNews) {
        List<News> featured = new ArrayList<>();
        for (News news : allNews) {
            if (news.isFeatured()) {
                featured.add(news);
            }
        }
        return featured;
    }
    
    /**
     * Get upcoming events only
     */
    public static List<Event> getUpcomingEvents(List<Event> allEvents) {
        List<Event> upcoming = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getStatus() == Event.Status.UPCOMING || event.getStatus() == Event.Status.ONGOING) {
                upcoming.add(event);
            }
        }
        return upcoming;
    }
}
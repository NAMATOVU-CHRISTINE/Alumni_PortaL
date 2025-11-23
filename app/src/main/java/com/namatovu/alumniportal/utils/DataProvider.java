package com.namatovu.alumniportal.utils;

import com.namatovu.alumniportal.models.Recommendation;
import com.namatovu.alumniportal.models.RecentActivity;
import com.namatovu.alumniportal.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DataProvider {
    
    private static final Random random = new Random();
    
    /**
     * Generate personalized recommendations based on user profile
     */
    public static List<Recommendation> getPersonalizedRecommendations(User user) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Profile completion recommendation
        if (user != null && getProfileCompletionPercentage(user) < 80) {
            recommendations.add(new Recommendation(
                "profile_completion",
                "Complete your profile",
                "Add skills & experience to get better job matches",
                "💼",
                Recommendation.Type.PROFILE_COMPLETION,
                "profile",
                5
            ));
        }
        
        // Job opportunities based on user's field/interests
        String userField = getUserField(user);
        recommendations.addAll(getJobRecommendations(userField));
        
        // Skill development recommendations
        recommendations.addAll(getSkillRecommendations(user));
        
        // Networking recommendations
        recommendations.addAll(getNetworkingRecommendations());
        
        // Sort by priority and return top 3
        Collections.sort(recommendations, (a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        return recommendations.subList(0, Math.min(3, recommendations.size()));
    }
    
    /**
     * Generate dynamic recent activities
     */
    public static List<RecentActivity> getRecentActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        
        // Generate recent job postings
        activities.addAll(generateJobActivities());
        
        // Generate message activities
        activities.addAll(generateMessageActivities());
        
        // Generate connection activities
        activities.addAll(generateConnectionActivities());
        
        // Generate achievement activities
        activities.addAll(generateAchievementActivities());
        
        // Sort by timestamp and return recent ones
        Collections.sort(activities, (a, b) -> {
            try {
                // Parse timestamp strings and compare
                long timeA = Long.parseLong(a.getTimeStamp().replaceAll("[^0-9]", ""));
                long timeB = Long.parseLong(b.getTimeStamp().replaceAll("[^0-9]", ""));
                return Long.compare(timeB, timeA);
            } catch (NumberFormatException e) {
                return 0;
            }
        });
        return activities.subList(0, Math.min(5, activities.size()));
    }
    
    private static int getProfileCompletionPercentage(User user) {
        if (user == null) return 0;
        
        int completedFields = 0;
        int totalFields = 8;
        
        if (user.getFullName() != null && !user.getFullName().isEmpty()) completedFields++;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) completedFields++;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) completedFields++;
        if (user.getCurrentJob() != null && !user.getCurrentJob().isEmpty()) completedFields++;
        if (user.getCompany() != null && !user.getCompany().isEmpty()) completedFields++;
        if (user.getLocation() != null && !user.getLocation().isEmpty()) completedFields++;
        if (user.getBio() != null && !user.getBio().isEmpty()) completedFields++;
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) completedFields++;
        
        return (completedFields * 100) / totalFields;
    }
    
    private static String getUserField(User user) {
        if (user != null && user.getCurrentJob() != null) {
            String jobTitle = user.getCurrentJob().toLowerCase();
            if (jobTitle.contains("engineer") || jobTitle.contains("developer") || jobTitle.contains("tech")) {
                return "Technology";
            } else if (jobTitle.contains("marketing") || jobTitle.contains("sales")) {
                return "Marketing";
            } else if (jobTitle.contains("finance") || jobTitle.contains("accounting")) {
                return "Finance";
            } else if (jobTitle.contains("health") || jobTitle.contains("medical")) {
                return "Healthcare";
            }
        }
        return "General";
    }
    
    private static List<Recommendation> getJobRecommendations(String field) {
        List<Recommendation> jobs = new ArrayList<>();
        
        String[][] jobData = {
            {"Software Developer", "Remote position with competitive salary", "💻"},
            {"Product Manager", "Lead innovative projects at tech startup", "🚀"},
            {"Data Analyst", "Analyze trends and drive business decisions", "📊"},
            {"Marketing Specialist", "Create campaigns that make impact", "📈"},
            {"UX Designer", "Design experiences users love", "🎨"},
            {"Business Analyst", "Bridge technology and business needs", "💼"}
        };
        
        int jobIndex = random.nextInt(jobData.length);
        jobs.add(new Recommendation(
            "job_" + System.currentTimeMillis(),
            jobData[jobIndex][0] + " - " + field,
            jobData[jobIndex][1],
            jobData[jobIndex][2],
            Recommendation.Type.JOB_OPPORTUNITY,
            "jobs",
            4
        ));
        
        return jobs;
    }
    
    private static List<Recommendation> getSkillRecommendations(User user) {
        List<Recommendation> skills = new ArrayList<>();
        
        String[][] skillData = {
            {"Machine Learning Course", "Advance your career with AI skills", "🤖"},
            {"Leadership Workshop", "Develop your management abilities", "👑"},
            {"Digital Marketing Certification", "Master modern marketing strategies", "📱"},
            {"Data Science Bootcamp", "Learn to work with big data", "📊"},
            {"Public Speaking Training", "Improve your communication skills", "🎤"}
        };
        
        int skillIndex = random.nextInt(skillData.length);
        skills.add(new Recommendation(
            "skill_" + System.currentTimeMillis(),
            skillData[skillIndex][0],
            skillData[skillIndex][1],
            skillData[skillIndex][2],
            Recommendation.Type.SKILL_DEVELOPMENT,
            "knowledge",
            3
        ));
        
        return skills;
    }
    
    private static List<Recommendation> getNetworkingRecommendations() {
        List<Recommendation> networking = new ArrayList<>();
        
        String[][] networkData = {
            {"Connect with Alumni", "5 alumni in your field want to connect", "🤝"},
            {"Industry Meetup", "Tech professionals gathering this weekend", "🌐"},
            {"Mentorship Opportunity", "Senior professional offering guidance", "🎯"}
        };
        
        int networkIndex = random.nextInt(networkData.length);
        networking.add(new Recommendation(
            "network_" + System.currentTimeMillis(),
            networkData[networkIndex][0],
            networkData[networkIndex][1],
            networkData[networkIndex][2],
            Recommendation.Type.NETWORKING,
            "mentorship",
            2
        ));
        
        return networking;
    }
    
    private static List<RecentActivity> generateJobActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        
        String[] jobTitles = {
            "Software Engineer position at TechCorp",
            "Marketing Manager role at StartupX",
            "Data Scientist opportunity at DataFlow",
            "Product Designer position at CreativeHub"
        };
        
        for (int i = 0; i < 2; i++) {
            long timestamp = System.currentTimeMillis() - (random.nextInt(24) * 60 * 60 * 1000);
            activities.add(new RecentActivity(
                "🎯",
                "New job opportunity available",
                jobTitles[random.nextInt(jobTitles.length)],
                getTimeAgoString(timestamp),
                RecentActivity.Type.OPPORTUNITY
            ));
        }
        
        return activities;
    }
    
    private static List<RecentActivity> generateMessageActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        
        String[] senders = {
            "mentor Sarah Johnson",
            "alumni network admin",
            "career counselor Mike Chen",
            "peer Emma Watson"
        };
        
        long timestamp = System.currentTimeMillis() - (random.nextInt(12) * 60 * 60 * 1000);
        activities.add(new RecentActivity(
            "💬",
            "New message from " + senders[random.nextInt(senders.length)],
            "You have a new message in your inbox",
            getTimeAgoString(timestamp),
            RecentActivity.Type.MESSAGE
        ));
        
        return activities;
    }
    
    private static List<RecentActivity> generateConnectionActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        
        long timestamp = System.currentTimeMillis() - (random.nextInt(6) * 60 * 60 * 1000);
        activities.add(new RecentActivity(
            "🤝",
            "New connection request",
            "3 alumni want to connect with you",
            getTimeAgoString(timestamp),
            RecentActivity.Type.CONNECTION
        ));
        
        return activities;
    }
    
    private static List<RecentActivity> generateAchievementActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        
        String[] achievements = {
            "Profile completion milestone reached",
            "First job application submitted",
            "Networking goal achieved",
            "Skill assessment completed"
        };
        
        long timestamp = System.currentTimeMillis() - (random.nextInt(3) * 60 * 60 * 1000);
        activities.add(new RecentActivity(
            "🏆",
            achievements[random.nextInt(achievements.length)],
            "Congratulations on your progress!",
            getTimeAgoString(timestamp),
            RecentActivity.Type.ACHIEVEMENT
        ));
        
        return activities;
    }
    
    private static String getTimeAgoString(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            int minutes = (int) (diff / 60000);
            return minutes + " min ago";
        } else if (diff < 86400000) { // Less than 1 day
            int hours = (int) (diff / 3600000);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else {
            int days = (int) (diff / 86400000);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
    }
}
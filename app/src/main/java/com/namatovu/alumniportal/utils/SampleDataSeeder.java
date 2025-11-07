package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.models.JobPosting;
import com.namatovu.alumniportal.models.MentorshipConnection;
import com.namatovu.alumniportal.models.User;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to seed sample data for testing the app functionality
 */
public class SampleDataSeeder {
    private static final String TAG = "SampleDataSeeder";
    private FirebaseFirestore db;
    private Context context;

    public SampleDataSeeder(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Seed sample job postings
     */
    public void seedJobPostings() {
        // Sample job posting 1
        Map<String, Object> job1 = new HashMap<>();
        job1.put("title", "Software Engineer");
        job1.put("company", "Tech Solutions Ltd");
        job1.put("location", "Kampala, Uganda");
        job1.put("jobType", "Full-time");
        job1.put("experienceLevel", "Mid-level");
        job1.put("description", "We are looking for a skilled software engineer to join our team. Experience with Java, Android development, and Firebase required.");
        job1.put("requirements", "Bachelor's degree in Computer Science, 3+ years experience");
        job1.put("salary", "UGX 2,500,000 - 4,000,000");
        job1.put("contactEmail", "hr@techsolutions.ug");
        job1.put("postedAt", new Date());
        job1.put("isActive", true);

        // Sample job posting 2
        Map<String, Object> job2 = new HashMap<>();
        job2.put("title", "Marketing Manager");
        job2.put("company", "Digital Marketing Agency");
        job2.put("location", "Entebbe, Uganda");
        job2.put("jobType", "Full-time");
        job2.put("experienceLevel", "Senior");
        job2.put("description", "Lead our marketing team and develop strategic marketing campaigns for our clients.");
        job2.put("requirements", "MBA or equivalent, 5+ years marketing experience");
        job2.put("salary", "UGX 3,000,000 - 5,000,000");
        job2.put("contactEmail", "jobs@digitalmarketing.ug");
        job2.put("postedAt", new Date());
        job2.put("isActive", true);

        // Sample job posting 3
        Map<String, Object> job3 = new HashMap<>();
        job3.put("title", "Data Analyst");
        job3.put("company", "Financial Services Co");
        job3.put("location", "Kampala, Uganda");
        job3.put("jobType", "Part-time");
        job3.put("experienceLevel", "Entry-level");
        job3.put("description", "Analyze financial data and create reports to support business decisions.");
        job3.put("requirements", "Bachelor's in Mathematics, Statistics, or related field");
        job3.put("salary", "UGX 1,500,000 - 2,500,000");
        job3.put("contactEmail", "careers@financialservices.ug");
        job3.put("postedAt", new Date());
        job3.put("isActive", true);

        // Add to Firestore
        db.collection("job_postings").add(job1);
        db.collection("job_postings").add(job2);
        db.collection("job_postings").add(job3);

        Log.d(TAG, "Sample job postings seeded");
    }

    /**
     * Seed sample mentorship connections
     */
    public void seedMentorshipConnections() {
        // Sample mentorship connection 1
        Map<String, Object> connection1 = new HashMap<>();
        connection1.put("mentorId", "sample_mentor_1");
        connection1.put("menteeId", "sample_mentee_1");
        connection1.put("mentorName", "Dr. Sarah Nakamya");
        connection1.put("menteeName", "John Okello");
        connection1.put("status", "active");
        connection1.put("mentorshipType", "career");
        connection1.put("message", "Looking for guidance in software engineering career");
        connection1.put("requestedAt", new Date().getTime());
        connection1.put("acceptedAt", new Date().getTime());

        // Sample mentorship connection 2
        Map<String, Object> connection2 = new HashMap<>();
        connection2.put("mentorId", "sample_mentor_2");
        connection2.put("menteeId", "sample_mentee_2");
        connection2.put("mentorName", "Prof. Moses Kiremire");
        connection2.put("menteeName", "Grace Ampaire");
        connection2.put("status", "pending");
        connection2.put("mentorshipType", "academic");
        connection2.put("message", "Seeking guidance for postgraduate studies");
        connection2.put("requestedAt", new Date().getTime());

        // Add to Firestore
        db.collection("mentor_connections").add(connection1);
        db.collection("mentor_connections").add(connection2);

        Log.d(TAG, "Sample mentorship connections seeded");
    }

    /**
     * Seed sample users
     */
    public void seedSampleUsers() {
        // Sample user 1
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "Dr. Sarah Nakamya");
        user1.put("email", "sarah.nakamya@example.com");
        user1.put("bio", "Software Engineering Professor with 10+ years industry experience");
        user1.put("career", "Professor of Computer Science");
        user1.put("company", "Makerere University");
        user1.put("graduationYear", "2008");
        user1.put("skills", Arrays.asList("Software Engineering", "Java", "Python", "Research"));
        user1.put("profileImageUrl", "https://via.placeholder.com/150x150.png?text=SN");

        // Sample user 2
        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "John Okello");
        user2.put("email", "john.okello@example.com");
        user2.put("bio", "Recent graduate looking to start career in tech");
        user2.put("career", "Junior Software Developer");
        user2.put("company", "Tech Startup");
        user2.put("graduationYear", "2023");
        user2.put("skills", Arrays.asList("Java", "Android", "Firebase"));
        user2.put("profileImageUrl", "https://via.placeholder.com/150x150.png?text=JO");

        // Add to Firestore (use specific IDs for testing)
        db.collection("users").document("sample_mentor_1").set(user1);
        db.collection("users").document("sample_mentee_1").set(user2);

        Log.d(TAG, "Sample users seeded");
    }

    /**
     * Seed all sample data
     */
    public void seedAllSampleData() {
        Log.d(TAG, "Starting to seed sample data...");
        seedJobPostings();
        seedMentorshipConnections();
        seedSampleUsers();
        Log.d(TAG, "Sample data seeding completed");
    }

    /**
     * Check if sample data should be seeded (only once)
     */
    public void seedIfNeeded() {
        // Check if we've already seeded data
        android.content.SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean hasSeeded = prefs.getBoolean("sample_data_seeded", false);

        if (!hasSeeded) {
            seedAllSampleData();
            prefs.edit().putBoolean("sample_data_seeded", true).apply();
        }
    }
}
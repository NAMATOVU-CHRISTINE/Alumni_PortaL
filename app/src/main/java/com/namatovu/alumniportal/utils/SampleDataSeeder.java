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
        Date now = new Date();
        
        // Sample job posting 1
        Map<String, Object> job1 = new HashMap<>();
        job1.put("title", "Software Engineer");
        job1.put("company", "Tech Solutions Ltd");
        job1.put("location", "Kampala, Uganda");
        job1.put("jobType", "Full-time");
        job1.put("experienceLevel", "Mid-level");
        job1.put("salary", "UGX 2,500,000 - 4,000,000");
        job1.put("contactEmail", "hr@techsolutions.ug");
        job1.put("postedAt", now);
        job1.put("isActive", true);

        // Sample job posting 2
        Map<String, Object> job2 = new HashMap<>();
        job2.put("title", "Marketing Manager");
        job2.put("company", "Digital Marketing Agency");
        job2.put("location", "Entebbe, Uganda");
        job2.put("jobType", "Full-time");
        job2.put("experienceLevel", "Senior");
        job2.put("salary", "UGX 3,000,000 - 5,000,000");
        job2.put("contactEmail", "jobs@digitalmarketing.ug");
        job2.put("postedAt", now);
        job2.put("isActive", true);

        // Sample job posting 3
        Map<String, Object> job3 = new HashMap<>();
        job3.put("title", "Data Analyst");
        job3.put("company", "Financial Services Co");
        job3.put("location", "Kampala, Uganda");
        job3.put("jobType", "Part-time");
        job3.put("experienceLevel", "Entry-level");
        job3.put("salary", "UGX 1,500,000 - 2,500,000");
        job3.put("contactEmail", "careers@financialservices.ug");
        job3.put("postedAt", now);
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
        // Sample user 1 - Alumni (can be mentor)
        Map<String, Object> user1 = new HashMap<>();
        user1.put("fullName", "Dr. Sarah Nakamya");
        user1.put("email", "sarah.nakamya@example.com");
        user1.put("bio", "Software Engineering Professor with 10+ years industry experience");
        user1.put("currentJob", "Professor of Computer Science");
        user1.put("company", "Makerere University");
        user1.put("graduationYear", "2008");
        user1.put("skills", Arrays.asList("Software Engineering", "Java", "Python", "Research"));
        user1.put("profileImageUrl", "https://via.placeholder.com/150x150.png?text=SN");
        user1.put("userType", "alumni");
        user1.put("isAlumni", true);
        user1.put("showInDirectory", true);
        user1.put("allowMentorRequests", true);
        user1.put("createdAt", System.currentTimeMillis());

        // Sample user 2 - Alumni (can be mentor)
        Map<String, Object> user2 = new HashMap<>();
        user2.put("fullName", "Prof. Moses Kiremire");
        user2.put("email", "moses.kiremire@example.com");
        user2.put("bio", "Business Administration expert with 15+ years experience");
        user2.put("currentJob", "Senior Lecturer");
        user2.put("company", "Makerere University");
        user2.put("graduationYear", "2005");
        user2.put("skills", Arrays.asList("Business Strategy", "Leadership", "Finance", "Mentoring"));
        user2.put("profileImageUrl", "https://via.placeholder.com/150x150.png?text=MK");
        user2.put("userType", "alumni");
        user2.put("isAlumni", true);
        user2.put("showInDirectory", true);
        user2.put("allowMentorRequests", true);
        user2.put("createdAt", System.currentTimeMillis());

        // Sample user 3 - Staff (can be mentor)
        Map<String, Object> user3 = new HashMap<>();
        user3.put("fullName", "Dr. Emily Ochieng");
        user3.put("email", "emily.ochieng@example.com");
        user3.put("bio", "Career Development Officer helping students transition to industry");
        user3.put("currentJob", "Career Development Officer");
        user3.put("company", "MUST Career Services");
        user3.put("graduationYear", "2010");
        user3.put("skills", Arrays.asList("Career Counseling", "Resume Writing", "Interview Prep", "Networking"));
        user3.put("profileImageUrl", "https://via.placeholder.com/150x150.png?text=EO");
        user3.put("userType", "staff");
        user3.put("isAlumni", false);
        user3.put("showInDirectory", true);
        user3.put("allowMentorRequests", true);
        user3.put("createdAt", System.currentTimeMillis());

        // Sample user 4 - Student (cannot be mentor)
        Map<String, Object> user4 = new HashMap<>();
        user4.put("fullName", "John Okello");
        user4.put("email", "john.okello@example.com");
        user4.put("bio", "Final year student looking to start career in tech");
        user4.put("currentJob", "");
        user4.put("company", "");
        user4.put("graduationYear", "2024");
        user4.put("skills", Arrays.asList("Java", "Android", "Firebase"));
        user4.put("profileImageUrl", "https://via.placeholder.com/150x150.png?text=JO");
        user4.put("userType", "student");
        user4.put("isAlumni", false);
        user4.put("showInDirectory", false);
        user4.put("allowMentorRequests", false);
        user4.put("createdAt", System.currentTimeMillis());

        // Sample user 5 - Alumni
        Map<String, Object> user5 = new HashMap<>();
        user5.put("fullName", "Grace Ampaire");
        user5.put("email", "grace.ampaire@example.com");
        user5.put("bio", "Data Scientist working at leading tech company");
        user5.put("currentJob", "Senior Data Scientist");
        user5.put("company", "Tech Innovations Ltd");
        user5.put("graduationYear", "2018");
        user5.put("skills", Arrays.asList("Data Science", "Python", "Machine Learning", "SQL"));
        user5.put("profileImageUrl", "https://via.placeholder.com/150x150.png?text=GA");
        user5.put("userType", "alumni");
        user5.put("isAlumni", true);
        user5.put("showInDirectory", true);
        user5.put("allowMentorRequests", true);
        user5.put("createdAt", System.currentTimeMillis());

        // Add to Firestore (use specific IDs for testing)
        db.collection("users").document("sample_alumni_1").set(user1);
        db.collection("users").document("sample_alumni_2").set(user2);
        db.collection("users").document("sample_staff_1").set(user3);
        db.collection("users").document("sample_student_1").set(user4);
        db.collection("users").document("sample_alumni_3").set(user5);

        Log.d(TAG, "Sample users seeded - 3 alumni, 1 staff, 1 student");
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
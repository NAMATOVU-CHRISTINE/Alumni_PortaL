package com.namatovu.alumniportal;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.namatovu.alumniportal.adapters.RecentActivityAdapter;
import com.namatovu.alumniportal.models.RecentActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * RecentActivitiesActivity - Shows user's recent activities and updates
 */
public class RecentActivitiesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewActivities;
    private RecentActivityAdapter activityAdapter;
    private List<RecentActivity> activities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_activities);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadRecentActivities();
    }

    private void initializeViews() {
        recyclerViewActivities = findViewById(R.id.recyclerViewActivities);
        activities = new ArrayList<>();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🔔 Recent Activity");
        }
    }

    private void setupRecyclerView() {
        activityAdapter = new RecentActivityAdapter(this, activities);
        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewActivities.setAdapter(activityAdapter);
    }

    private void loadRecentActivities() {
        // Sample recent activities - replace with real data
        activities.clear();
        
        activities.add(new RecentActivity(
            "🎯", 
            "New Job Opportunity", 
            "Software Developer position at TechCorp matches your skills",
            "2 hours ago",
            RecentActivity.Type.OPPORTUNITY
        ));
        
        activities.add(new RecentActivity(
            "💬", 
            "New Message", 
            "Dr. Mukasa replied to your mentorship request",
            "5 hours ago",
            RecentActivity.Type.MESSAGE
        ));
        
        activities.add(new RecentActivity(
            "📚", 
            "Knowledge Update", 
            "New article: 'Machine Learning Fundamentals' added",
            "1 day ago",
            RecentActivity.Type.KNOWLEDGE
        ));
        
        activities.add(new RecentActivity(
            "👥", 
            "New Connection", 
            "Jane Nakato accepted your connection request",
            "2 days ago",
            RecentActivity.Type.CONNECTION
        ));
        
        activities.add(new RecentActivity(
            "🎓", 
            "Profile Update", 
            "Your profile completeness increased to 85%",
            "3 days ago",
            RecentActivity.Type.PROFILE
        ));
        
        activities.add(new RecentActivity(
            "🌟", 
            "Achievement", 
            "You've completed 5 mentorship sessions!",
            "1 week ago",
            RecentActivity.Type.ACHIEVEMENT
        ));

        activityAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
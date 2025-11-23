package com.namatovu.alumniportal;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private LinearLayout emptyStateLayout;
    private TextView tvActivityCount;

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
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvActivityCount = findViewById(R.id.tvActivityCount);
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
        // Start with empty list - will be populated when users perform actual actions
        activities.clear();
        
        // TODO: Load real user activities from database/API when available
        // This will show notifications for:
        // - New job opportunities matching user profile
        // - Messages from mentors/connections
        // - Knowledge hub updates
        // - New connections/network updates
        // - Profile completion milestones
        // - Achievement unlocks
        
        activityAdapter.notifyDataSetChanged();
        updateUI();
    }
    
    private void updateUI() {
        if (activities.isEmpty()) {
            // Show empty state
            recyclerViewActivities.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvActivityCount.setText("0 items");
        } else {
            // Show activities
            recyclerViewActivities.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            tvActivityCount.setText(activities.size() + " items");
        }
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
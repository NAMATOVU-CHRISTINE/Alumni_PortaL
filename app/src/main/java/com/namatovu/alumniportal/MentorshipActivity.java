package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.adapters.MentorshipAdapter;
import com.namatovu.alumniportal.ViewProfileActivity;
import com.namatovu.alumniportal.databinding.ActivityMentorshipBinding;
import com.namatovu.alumniportal.models.MentorshipConnection;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MentorshipActivity extends AppCompatActivity {
    private static final String TAG = "MentorshipActivity";
    
    private ActivityMentorshipBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MentorshipAdapter adapter;
    private List<MentorshipConnection> allConnections;
    private List<MentorshipConnection> filteredConnections;
    private String currentUserId;
    private String currentTab = "as_mentee"; // Default to "as_mentee" instead of "all"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMentorshipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("MentorshipActivity", "HomeActivity");

        // Initialize lists
        allConnections = new ArrayList<>();
        filteredConnections = new ArrayList<>();

        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupFAB();
        loadMentorshipConnections();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Clear default title since we use custom layout
        }
        
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationIconTint(getColor(android.R.color.white));
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTabs() {
        // Only add 2 tabs: As Mentor and As Mentee
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("As Mentor"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("As Mentee"));
        
        // Set default to "As Mentee" tab
        currentTab = "as_mentee";
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));
        
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "as_mentor";
                        break;
                    case 1:
                        currentTab = "as_mentee";
                        break;
                }
                filterConnections();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        MentorshipAdapter.OnMentorshipActionListener listener = new MentorshipAdapter.OnMentorshipActionListener() {
            @Override
            public void onAccept(MentorshipConnection connection) {
                updateMentorshipStatus(connection, "accepted");
                AnalyticsHelper.logMentorConnection("accept", connection.getMenteeId());
            }

            @Override
            public void onReject(MentorshipConnection connection) {
                updateMentorshipStatus(connection, "rejected");
                AnalyticsHelper.logMentorConnection("reject", connection.getMenteeId());
            }

            @Override
            public void onViewProfile(String userId) {
                Intent intent = new Intent(MentorshipActivity.this, ViewProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }

            @Override
            public void onStartSession(MentorshipConnection connection) {
                Toast.makeText(MentorshipActivity.this, "Session management coming soon!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompleteConnection(MentorshipConnection connection) {
                updateMentorshipStatus(connection, "completed");
            }
        };
        
        adapter = new MentorshipAdapter(filteredConnections, currentUserId, listener);
        
        binding.recyclerViewMentors.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMentors.setAdapter(adapter);
    }

    private void setupFAB() {
        binding.findMentorFab.setOnClickListener(v -> {
            // Navigate to alumni directory or mentor search
            Intent intent = new Intent(this, ProfileActivity.class); // Placeholder until AlumniDirectoryActivity exists
            intent.putExtra("mode", "mentor_search");
            startActivity(intent);
            
            // Log analytics
            AnalyticsHelper.logMentorConnection("find_mentor_fab_clicked", currentUserId);
        });
        
        // Setup empty state button
        if (binding.emptyStateFindMentorButton != null) {
            binding.emptyStateFindMentorButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class); // Placeholder
                intent.putExtra("mode", "mentor_search");
                startActivity(intent);
                
                AnalyticsHelper.logMentorConnection("find_mentor_empty_state_clicked", currentUserId);
            });
        }
    }

    private void loadMentorshipConnections() {
        // Show loading state - using visibility changes on existing views
        binding.emptyStateLayout.setVisibility(View.GONE);

        // First try to load any mentorship connections at all
        db.collection("mentor_connections")
                .limit(20) // Start with a small number
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allConnections.clear();
                    
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " total connection documents");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Try to parse the document data
                            Map<String, Object> data = document.getData();
                            Log.d(TAG, "Connection document data: " + data.toString());
                            
                            MentorshipConnection connection = document.toObject(MentorshipConnection.class);
                            connection.setConnectionId(document.getId());
                            
                            // Add all connections for now, we'll filter later
                            allConnections.add(connection);
                            
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing connection document: " + document.getId(), e);
                        }
                    }
                    
                    // Filter connections for current user
                    filterConnectionsForCurrentUser();
                    
                    Log.d(TAG, "Loaded " + allConnections.size() + " total connections, " + 
                         filteredConnections.size() + " for current user");
                })
                .addOnFailureListener(e -> {
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    
                    Toast.makeText(this, "Failed to load connections: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading mentorship connections", e);
                    
                    AnalyticsHelper.logError("mentorship_load_failed", e.getMessage(), "MentorshipActivity");
                });
    }
    
    private void filterConnectionsForCurrentUser() {
        filteredConnections.clear();
        
        for (MentorshipConnection connection : allConnections) {
            // Check if current user is involved in this connection
            if (currentUserId.equals(connection.getMentorId()) || 
                currentUserId.equals(connection.getMenteeId())) {
                filteredConnections.add(connection);
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        if (filteredConnections.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            // Update empty state message based on current tab
            updateEmptyStateMessage();
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
    }
    
    private void updateEmptyStateMessage() {
        String title, message;
        switch (currentTab) {
            case "as_mentor":
                title = "No mentees yet";
                message = "When students request your mentorship, they'll appear here. Share your knowledge and help the next generation grow!";
                break;
            case "as_mentee":
                title = "No mentors yet";
                message = "Connect with experienced alumni to accelerate your career growth and gain valuable insights from industry professionals.";
                break;
            default:
                title = "No mentorship connections yet";
                message = "Start connecting with alumni mentors to grow your network and accelerate your career journey!";
                break;
        }
        
        if (binding.emptyStateTitle != null) {
            binding.emptyStateTitle.setText(title);
        }
        if (binding.emptyStateMessage != null) {
            binding.emptyStateMessage.setText(message);
        }
    }

    private void filterConnections() {
        filteredConnections.clear();
        
        for (MentorshipConnection connection : allConnections) {
            boolean shouldInclude = false;
            
            switch (currentTab) {
                case "as_mentor":
                    shouldInclude = currentUserId.equals(connection.getMentorId());
                    break;
                case "as_mentee":
                    shouldInclude = currentUserId.equals(connection.getMenteeId());
                    break;
            }
            
            if (shouldInclude) {
                filteredConnections.add(connection);
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
        
        // Update toolbar subtitle with connection count
        String subtitle = filteredConnections.size() + " connection" + (filteredConnections.size() != 1 ? "s" : "");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private String getEmptyMessage() {
        switch (currentTab) {
            case "as_mentor":
                return "No mentees yet. Your mentorship requests will appear here.";
            case "as_mentee":
                return "No mentors yet. Tap the + button to find a mentor.";
            default:
                return "No mentorship connections yet. Start connecting with alumni!";
        }
    }

    private void updateMentorshipStatus(MentorshipConnection connection, String newStatus) {
        if (connection.getConnectionId() == null) return;
        
        connection.setStatus(newStatus);
        if ("accepted".equals(newStatus)) {
            connection.accept();
        } else if ("completed".equals(newStatus)) {
            connection.complete();
        } else if ("rejected".equals(newStatus)) {
            connection.reject();
        }
        
        db.collection("mentor_connections").document(connection.getConnectionId())
                .update("status", newStatus, "acceptedAt", connection.getAcceptedAt(), 
                       "completedAt", connection.getCompletedAt())
                .addOnSuccessListener(aVoid -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Connection " + newStatus, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Mentorship status updated to: " + newStatus);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update connection", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update mentorship status", e);
                    AnalyticsHelper.logError("mentorship_update_failed", e.getMessage(), "MentorshipActivity");
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadMentorshipConnections();
    }
}
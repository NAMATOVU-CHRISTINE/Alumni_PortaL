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
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private EmailService emailService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMentorshipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        emailService = new EmailService(this);
        
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
        // Clear any existing tabs first
        binding.tabLayout.removeAllTabs();
        
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
                // Open chat activity for mentor-mentee communication
                Intent chatIntent = new Intent(MentorshipActivity.this, ChatActivity.class);
                chatIntent.putExtra("connectionId", connection.getConnectionId());
                
                // Determine who is the "other" user for chat
                boolean isCurrentUserMentor = currentUserId.equals(connection.getMentorId());
                String otherUserId = isCurrentUserMentor ? connection.getMenteeId() : connection.getMentorId();
                String otherUserName = isCurrentUserMentor ? connection.getMenteeName() : connection.getMentorName();
                
                chatIntent.putExtra("otherUserId", otherUserId);
                chatIntent.putExtra("otherUserName", otherUserName);
                startActivity(chatIntent);
            }

            @Override
            public void onCompleteConnection(MentorshipConnection connection) {
                updateMentorshipStatus(connection, "completed");
            }
            
            @Override
            public void onRequestMentorship(MentorshipConnection connection) {
                requestMentorship(connection);
            }
        };
        
        adapter = new MentorshipAdapter(filteredConnections, currentUserId, listener);
        
        binding.recyclerViewMentors.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMentors.setAdapter(adapter);
    }

    private void setupFAB() {
        binding.findMentorFab.setOnClickListener(v -> {
            // Navigate to mentor search activity
            Intent intent = new Intent(this, MentorSearchActivity.class);
            startActivity(intent);
            
            // Log analytics
            AnalyticsHelper.logMentorConnection("find_mentor_fab_clicked", currentUserId);
        });
    }

    private void loadMentorshipConnections() {
        // Show loading state - using visibility changes on existing views
        binding.emptyStateLayout.setVisibility(View.GONE);

        // Check if user is authenticated
        if (currentUserId.isEmpty()) {
            Log.w(TAG, "User not authenticated");
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            updateEmptyStateMessage();
            return;
        }

        if (currentTab.equals("as_mentee")) {
            // Load all available users to request mentorship from
            loadAvailableMentors();
        } else {
            // Load existing mentorship connections
            loadExistingConnections();
        }
    }
    
    private void loadAvailableMentors() {
        // First, load existing connections for this user
        db.collection("mentorships")
                .whereEqualTo("menteeId", currentUserId)
                .get()
                .addOnSuccessListener(existingConnections -> {
                    // Store existing mentor IDs to filter them out from available list
                    List<String> existingMentorIds = new ArrayList<>();
                    allConnections.clear();
                    
                    // Add existing connections first
                    for (QueryDocumentSnapshot doc : existingConnections) {
                        try {
                            MentorshipConnection connection = doc.toObject(MentorshipConnection.class);
                            connection.setConnectionId(doc.getId());
                            allConnections.add(connection);
                            existingMentorIds.add(connection.getMentorId());
                            Log.d(TAG, "Added existing connection with mentor: " + connection.getMentorName());
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing existing connection: " + doc.getId(), e);
                        }
                    }
                    
                    // Now load all users to show as potential mentors (excluding those with existing connections)
                    db.collection("users")
                            .limit(50)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " total users");
                                
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    try {
                                        String userId = document.getId();
                                        
                                        // Skip current user and users with existing connections
                                        if (userId.equals(currentUserId) || existingMentorIds.contains(userId)) {
                                            continue;
                                        }
                                        
                                        Map<String, Object> userData = document.getData();
                                        String fullName = (String) userData.get("fullName");
                                        String currentJob = (String) userData.get("currentJob");
                                        String company = (String) userData.get("company");
                                        
                                        // Create a MentorshipConnection object for display purposes
                                        MentorshipConnection connection = new MentorshipConnection();
                                        connection.setMentorId(userId);
                                        connection.setMenteeId(currentUserId);
                                        connection.setMentorName(fullName != null ? fullName : "Unknown User");
                                        connection.setMentorTitle(currentJob != null ? currentJob : "Alumni");
                                        connection.setMentorCompany(company != null ? company : "");
                                        connection.setStatus("available"); // Special status for available mentors
                                        connection.setConnectionId(null); // No connection exists yet
                                        
                                        allConnections.add(connection);
                                        
                                    } catch (Exception e) {
                                        Log.w(TAG, "Error parsing user document: " + document.getId(), e);
                                    }
                                }
                                
                                // Apply current tab filter
                                filterConnections();
                                
                                Log.d(TAG, "Loaded " + allConnections.size() + " total connections (existing + available)");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading users", e);
                                // Still show existing connections even if loading users fails
                                filterConnections();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading available mentors", e);
                    
                    allConnections.clear();
                    filteredConnections.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    Toast.makeText(this, "Unable to load available mentors", Toast.LENGTH_SHORT).show();
                    AnalyticsHelper.logError("mentors_load_failed", e.getMessage(), "MentorshipActivity");
                });
    }
    
    private void loadExistingConnections() {
        Log.d(TAG, "Loading existing connections for user: " + currentUserId);
        
        // Query connections where current user is either mentor or mentee
        db.collection("mentorships")
                .where(
                    com.google.firebase.firestore.Filter.or(
                        com.google.firebase.firestore.Filter.equalTo("mentorId", currentUserId),
                        com.google.firebase.firestore.Filter.equalTo("menteeId", currentUserId)
                    )
                )
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allConnections.clear();
                    
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " connections for user");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            MentorshipConnection connection = document.toObject(MentorshipConnection.class);
                            connection.setConnectionId(document.getId());
                            allConnections.add(connection);
                            
                            Log.d(TAG, "Loaded connection: ID=" + document.getId() + 
                                      ", Status=" + connection.getStatus() + 
                                      ", MentorId=" + connection.getMentorId() + 
                                      ", MenteeId=" + connection.getMenteeId());
                            
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing connection document: " + document.getId(), e);
                        }
                    }
                    
                    // Apply current tab filter
                    filterConnections();
                    
                    Log.d(TAG, "Loaded " + allConnections.size() + " total connections");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading mentorship connections", e);
                    
                    // Show empty state instead of error for better UX
                    allConnections.clear();
                    filteredConnections.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    // Only show error toast if it's not a permission error
                    if (!e.getMessage().contains("PERMISSION_DENIED")) {
                        Toast.makeText(this, "Unable to load connections", Toast.LENGTH_SHORT).show();
                    }
                    
                    AnalyticsHelper.logError("mentorship_load_failed", e.getMessage(), "MentorshipActivity");
                });
    }
    
    private void filterConnections() {
        filteredConnections.clear();
        
        Log.d(TAG, "Filtering connections for tab: " + currentTab + ", user: " + currentUserId);
        Log.d(TAG, "Total connections to filter: " + allConnections.size());
        
        for (MentorshipConnection connection : allConnections) {
            boolean shouldInclude = false;
            
            // Add detailed logging for debugging
            Log.d(TAG, "Checking connection - ID: " + connection.getConnectionId() + 
                      ", Status: " + connection.getStatus() + 
                      ", MentorId: " + connection.getMentorId() + 
                      ", MenteeId: " + connection.getMenteeId());
            
            switch (currentTab) {
                case "as_mentor":
                    // Show pending requests and existing connections where current user is the mentor
                    shouldInclude = currentUserId.equals(connection.getMentorId()) && 
                                    ("pending".equals(connection.getStatus()) || 
                                     "accepted".equals(connection.getStatus()) || 
                                     "active".equals(connection.getStatus()) ||
                                     "completed".equals(connection.getStatus()));
                    
                    Log.d(TAG, "As Mentor - Current user is mentor: " + currentUserId.equals(connection.getMentorId()) +
                              ", Status check: " + ("pending".equals(connection.getStatus()) || 
                                                   "accepted".equals(connection.getStatus()) || 
                                                   "active".equals(connection.getStatus()) ||
                                                   "completed".equals(connection.getStatus())) +
                              ", Should include: " + shouldInclude);
                    break;
                    
                case "as_mentee":
                    // Show available mentors or existing connections where current user is mentee
                    shouldInclude = "available".equals(connection.getStatus()) || 
                                    currentUserId.equals(connection.getMenteeId());
                    
                    Log.d(TAG, "As Mentee - Available: " + "available".equals(connection.getStatus()) +
                              ", Current user is mentee: " + currentUserId.equals(connection.getMenteeId()) +
                              ", Should include: " + shouldInclude);
                    break;
            }
            
            if (shouldInclude) {
                filteredConnections.add(connection);
                Log.d(TAG, "Added connection to filtered list");
            }
        }
        
        Log.d(TAG, "Filtered connections count: " + filteredConnections.size());
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
        
        // Update toolbar subtitle with connection count
        String subtitle = filteredConnections.size() + " connection" + (filteredConnections.size() != 1 ? "s" : "");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
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
        
        db.collection("mentorships").document(connection.getConnectionId())
                .update("status", newStatus, "acceptedAt", connection.getAcceptedAt(), 
                       "completedAt", connection.getCompletedAt())
                .addOnSuccessListener(aVoid -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Connection " + newStatus, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Mentorship status updated to: " + newStatus);
                    
                    // Send email notification to mentee about status change
                    if ("accepted".equals(newStatus) || "rejected".equals(newStatus)) {
                        emailService.sendStatusUpdateEmail(connection.getMenteeId(), connection.getMenteeName(), connection.getMentorName(), newStatus, connection.getConnectionId());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update connection", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update mentorship status", e);
                    AnalyticsHelper.logError("mentorship_update_failed", e.getMessage(), "MentorshipActivity");
                });
    }

    private void requestMentorship(MentorshipConnection connection) {
        Log.d(TAG, "Starting mentorship request for mentor: " + connection.getMentorId());
        
        // Validate inputs before proceeding
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot send mentorship request - currentUserId is null or empty");
            return;
        }
        
        if (connection.getMentorId() == null || connection.getMentorId().isEmpty()) {
            Toast.makeText(this, "Error: Invalid mentor selected", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot send mentorship request - mentor ID is null or empty");
            return;
        }
        
        if (currentUserId.equals(connection.getMentorId())) {
            Toast.makeText(this, "Cannot request mentorship from yourself", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User attempted to request mentorship from themselves");
            return;
        }
        
        // Check if a request already exists to prevent duplicates
        Log.d(TAG, "Checking for existing mentorship requests");
        db.collection("mentorships")
                .whereEqualTo("mentorId", connection.getMentorId())
                .whereEqualTo("menteeId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Request already exists
                        Log.d(TAG, "Mentorship request already exists");
                        Toast.makeText(this, "You already have a request with " + connection.getMentorName(), Toast.LENGTH_SHORT).show();
                        
                        // Refresh to show the existing request
                        loadMentorshipConnections();
                        return;
                    }
                    
                    // No existing request, proceed with creating new one
                    Log.d(TAG, "No existing request found, proceeding with new request");
                    createNewMentorshipRequest(connection);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing requests", e);
                    Toast.makeText(this, "Error checking existing requests", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void createNewMentorshipRequest(MentorshipConnection connection) {
        // Get the current user's information
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    Log.d(TAG, "User document retrieved, exists: " + userDoc.exists());
                    if (userDoc.exists()) {
                        User currentUser = userDoc.toObject(User.class);
                        String menteeName = currentUser != null ? currentUser.getFullName() : "Unknown User";
                        Log.d(TAG, "Current user name: " + menteeName);
                        
                        // Create a new mentorship request
                        MentorshipConnection newConnection = new MentorshipConnection();
                        newConnection.setMentorId(connection.getMentorId());
                        newConnection.setMenteeId(currentUserId);
                        newConnection.setMentorName(connection.getMentorName());
                        newConnection.setMenteeName(menteeName);
                        newConnection.setMentorTitle(connection.getMentorTitle());
                        newConnection.setMentorCompany(connection.getMentorCompany());
                        newConnection.setStatus("pending");
                        newConnection.setMessage("Hi! I would like to request mentorship from you. I believe your experience would be valuable for my professional growth.");
                        newConnection.request(); // This sets the requestedAt timestamp
                        
                        Log.d(TAG, "Created new connection object, attempting to save to Firestore");
                        
                        // Save to Firebase using toMap() for better compatibility
                        Map<String, Object> connectionData = newConnection.toMap();
                        Log.d(TAG, "Connection data map created with " + connectionData.size() + " fields");
                        
                        // Try using a different collection structure that might have broader permissions
                        db.collection("mentorships")
                                .add(connectionData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Successfully saved to Firestore with ID: " + documentReference.getId());
                                    newConnection.setConnectionId(documentReference.getId());
                                    
                                    // Update the document with the ID
                                    documentReference.update("connectionId", documentReference.getId())
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Connection ID updated successfully");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Failed to update connection ID, but request was saved", e);
                                            });
                                    
                                    Log.d(TAG, "Mentorship request created with ID: " + documentReference.getId());
                                    
                                    // Send email notification to mentor using EmailService
                                    Log.d(TAG, "Sending email notification to mentor: " + connection.getMentorName());
                                    emailService.sendMentorshipRequestEmail(connection.getMentorId(), menteeName, connection.getMentorName(), documentReference.getId());
                                    
                                    Toast.makeText(this, "Mentorship request sent to " + connection.getMentorName() + "!", Toast.LENGTH_SHORT).show();
                                    
                                    // Refresh the data to show the new connection
                                    loadMentorshipConnections();
                                    
                                    // Log analytics
                                    AnalyticsHelper.logMentorConnection("request_sent", connection.getMentorId());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to send mentorship request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Failed to create mentorship request", e);
                                    Log.e(TAG, "Error details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                                    AnalyticsHelper.logError("mentorship_request_failed", e.getMessage(), "MentorshipActivity");
                                });
                    } else {
                        String errorMsg = "Could not retrieve user information - user document does not exist";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, errorMsg + " for userId: " + currentUserId);
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMsg = "Failed to get user information: " + e.getMessage();
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to get current user", e);
                    Log.e(TAG, "Error details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadMentorshipConnections();
    }
}
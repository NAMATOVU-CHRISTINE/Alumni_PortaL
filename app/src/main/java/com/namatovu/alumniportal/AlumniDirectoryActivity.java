package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.adapters.AlumniAdapter;
import com.namatovu.alumniportal.databinding.ActivityAlumniDirectoryBinding;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlumniDirectoryActivity extends AppCompatActivity {
    private static final String TAG = "AlumniDirectoryActivity";
    
    private ActivityAlumniDirectoryBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AlumniAdapter adapter;
    private List<User> allUsers;
    private List<User> filteredUsers;
    
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlumniDirectoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("AlumniDirectoryActivity", "HomeActivity");

        // Initialize lists
        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        loadAlumniData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void setupRecyclerView() {
        adapter = new AlumniAdapter(filteredUsers, new AlumniAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // Open profile details
                Intent intent = new Intent(AlumniDirectoryActivity.this, ViewProfileActivity.class);
                intent.putExtra("userId", user.getUserId());
                intent.putExtra("username", user.getUsername());
                startActivity(intent);
                
                // Log analytics
                AnalyticsHelper.logNavigation("ViewProfileActivity", "AlumniDirectoryActivity");
            }
            
            @Override
            public void onEmailClick(User user) {
                // Open email app with implicit intent
                sendEmailToUser(user);
            }
        });
        
        binding.alumniRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.alumniRecyclerView.setAdapter(adapter);
    }
    
    private void sendEmailToUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            Toast.makeText(this, "Email not available for this user", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String subject = "Hello from Alumni Portal - " + user.getFullName();
        String body = "Hi " + user.getFullName() + ",\n\n" +
                     "I found your profile on the Alumni Portal and would like to connect.\n\n" +
                     "Best regards";
        
        // Create implicit intent for email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{user.getEmail()});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchAndFilters() {
        // Real-time search as user types
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Handle search action on keyboard
        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void loadAlumniData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.noResultsText.setVisibility(View.GONE);

        // Load ALL users from the database
        // We'll filter in the app to avoid complex index requirements
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allUsers.clear();
                    int totalUsers = 0;
                    int alumniCount = 0;
                    int visibleCount = 0;
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            totalUsers++;
                            String userId = document.getId();
                            
                            // Skip current user - don't show them in the directory
                            if (mAuth.getCurrentUser() != null && userId.equals(mAuth.getCurrentUser().getUid())) {
                                continue;
                            }
                            
                            User user = document.toObject(User.class);
                            user.setUserId(userId);
                            
                            // Set default connection status
                            user.setConnectionStatus("not_connected");
                            user.setConnected(false);
                            
                            // Debug logging
                            Log.d(TAG, "User: " + user.getFullName() + 
                                  ", isAlumni: " + user.isAlumni() + 
                                  ", userType: " + user.getUserType() +
                                  ", showInDirectory: " + user.getPrivacySetting("showInDirectory"));
                            
                            // Show alumni and staff in directory (not current students)
                            // Directory includes:
                            // - "alumni" = Graduated students
                            // - "staff" = Faculty/staff members
                            // - null/empty = Default to showing (for backward compatibility)
                            // Excluded: "student" = Current students
                            String userTypeValue = user.getUserType();
                            
                            // Skip only if explicitly marked as "student"
                            if ("student".equalsIgnoreCase(userTypeValue)) {
                                Log.d(TAG, "Skipping current student: " + user.getFullName());
                                continue;
                            }
                            
                            // Filter out incomplete profiles - require at least name
                            String fullName = user.getFullName();
                            
                            // Skip if name is null or empty (this is the only required field)
                            if (fullName == null || fullName.trim().isEmpty()) {
                                Log.d(TAG, "Skipping profile with no name");
                                continue; // Skip profiles without names
                            }
                            
                            // Check if user has opted to be visible in directory
                            // Default to true if privacy settings are not explicitly set to false
                            boolean isVisible = true;
                            Map<String, Object> privacySettings = user.getPrivacySettings();
                            if (privacySettings != null && privacySettings.containsKey("showInDirectory")) {
                                Object showInDirObj = privacySettings.get("showInDirectory");
                                if (showInDirObj instanceof Boolean) {
                                    isVisible = (Boolean) showInDirObj;
                                }
                            }
                            
                            if ("alumni".equalsIgnoreCase(userTypeValue)) {
                                alumniCount++;
                            }
                            
                            if (isVisible) {
                                visibleCount++;
                                allUsers.add(user);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing user document: " + document.getId(), e);
                        }
                    }
                    
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // Load connection status for all users
                    loadConnectionStatus();
                    
                    filterUsers();
                    
                    String message = "Found " + totalUsers + " total users, " + 
                                   alumniCount + " alumni, " + 
                                   visibleCount + " visible in directory";
                    Log.d(TAG, message);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.noResultsText.setVisibility(View.VISIBLE);
                    binding.noResultsText.setText("Failed to load alumni directory");
                    
                    Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading alumni data", e);
                    
                    AnalyticsHelper.logError("alumni_load_failed", e.getMessage(), "AlumniDirectoryActivity");
                });
    }

    private void filterUsers() {
        filteredUsers.clear();
        String lowerSearchQuery = searchQuery.toLowerCase();
        
        Log.d(TAG, "Filtering with: search='" + searchQuery + "'");
        
        for (User user : allUsers) {
            // Enhanced search - includes name, major, job, company, skills, and bio
            boolean matchesSearch = searchQuery.isEmpty() || 
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerSearchQuery)) ||
                    (user.getMajor() != null && user.getMajor().toLowerCase().contains(lowerSearchQuery)) ||
                    (user.getCurrentJob() != null && user.getCurrentJob().toLowerCase().contains(lowerSearchQuery)) ||
                    (user.getCompany() != null && user.getCompany().toLowerCase().contains(lowerSearchQuery)) ||
                    (user.getBio() != null && user.getBio().toLowerCase().contains(lowerSearchQuery)) ||
                    (user.getLocation() != null && user.getLocation().toLowerCase().contains(lowerSearchQuery)) ||
                    (user.getSkillsAsString() != null && user.getSkillsAsString().toLowerCase().contains(lowerSearchQuery));
            
            if (matchesSearch) {
                filteredUsers.add(user);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show/hide no results message
        if (filteredUsers.isEmpty() && !allUsers.isEmpty()) {
            binding.noResultsText.setVisibility(View.VISIBLE);
            binding.noResultsText.setText("No members match your search criteria");
        } else if (allUsers.isEmpty()) {
            binding.noResultsText.setVisibility(View.VISIBLE);
            binding.noResultsText.setText("No profiles available yet");
        } else {
            binding.noResultsText.setVisibility(View.GONE);
        }
        
        // Log search analytics
        if (!searchQuery.isEmpty()) {
            AnalyticsHelper.logAlumniSearch(searchQuery, filteredUsers.size());
        }
        
        binding.resultCountText.setText(filteredUsers.size() + " members found");
    }

    /**
     * Load connection status for all users
     * Checks if current user has accepted mentorship connections with each user
     */
    private void loadConnectionStatus() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            return;
        }
        
        // Query for accepted mentorship connections
        db.collection("mentorshipConnections")
                .whereIn("status", java.util.Arrays.asList("accepted", "active"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a set of connected user IDs
                    java.util.Set<String> connectedUserIds = new java.util.HashSet<>();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String mentorId = doc.getString("mentorId");
                        String menteeId = doc.getString("menteeId");
                        
                        // If current user is mentor, mentee is connected
                        if (currentUserId.equals(mentorId) && menteeId != null) {
                            connectedUserIds.add(menteeId);
                        }
                        // If current user is mentee, mentor is connected
                        else if (currentUserId.equals(menteeId) && mentorId != null) {
                            connectedUserIds.add(mentorId);
                        }
                    }
                    
                    // Update connection status for all users
                    for (User user : allUsers) {
                        if (connectedUserIds.contains(user.getUserId())) {
                            user.setConnected(true);
                            user.setConnectionStatus("connected");
                        } else {
                            user.setConnected(false);
                            user.setConnectionStatus("not_connected");
                        }
                    }
                    
                    Log.d(TAG, "Loaded connection status for " + connectedUserIds.size() + " connected users");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading connection status", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only reload if we don't have data yet
        if (allUsers.isEmpty()) {
            loadAlumniData();
        }
    }
}
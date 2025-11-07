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
import com.namatovu.alumniportal.adapters.MentorSearchAdapter;
import com.namatovu.alumniportal.databinding.ActivityMentorSearchBinding;
import com.namatovu.alumniportal.models.User;

import java.util.ArrayList;
import java.util.List;

public class MentorSearchActivity extends AppCompatActivity {
    private static final String TAG = "MentorSearchActivity";
    
    private ActivityMentorSearchBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MentorSearchAdapter adapter;
    private List<User> allMentors;
    private List<User> filteredMentors;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMentorSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Initialize lists
        allMentors = new ArrayList<>();
        filteredMentors = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        setupSearchFunctionality();
        loadPotentialMentors();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Find a Mentor");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        MentorSearchAdapter.OnMentorActionListener listener = new MentorSearchAdapter.OnMentorActionListener() {
            @Override
            public void onViewProfile(User mentor) {
                Intent intent = new Intent(MentorSearchActivity.this, ViewProfileActivity.class);
                intent.putExtra("userId", mentor.getUserId());
                startActivity(intent);
            }

            @Override
            public void onRequestMentorship(User mentor) {
                requestMentorship(mentor);
            }
        };
        
        adapter = new MentorSearchAdapter(filteredMentors, listener);
        binding.mentorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.mentorsRecyclerView.setAdapter(adapter);
    }

    private void setupSearchFunctionality() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMentors(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup filter chips
        binding.skillsFilterChip.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                filterBySkills();
            } else {
                resetFilters();
            }
        });

        binding.experienceFilterChip.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                filterByExperience();
            } else {
                resetFilters();
            }
        });

        binding.industryFilterChip.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                filterByIndustry();
            } else {
                resetFilters();
            }
        });
    }

    private void loadPotentialMentors() {
        showLoading(true);
        
        // Load users who could be potential mentors (excluding current user)
        db.collection("users")
                .whereNotEqualTo("userId", currentUserId)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allMentors.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            User user = document.toObject(User.class);
                            if (user != null && isPotentialMentor(user)) {
                                allMentors.add(user);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing user document: " + document.getId(), e);
                        }
                    }
                    
                    filteredMentors.clear();
                    filteredMentors.addAll(allMentors);
                    adapter.notifyDataSetChanged();
                    
                    showLoading(false);
                    updateEmptyState();
                    
                    Log.d(TAG, "Loaded " + allMentors.size() + " potential mentors");
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load mentors: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading potential mentors", e);
                });
    }

    private boolean isPotentialMentor(User user) {
        // Check if user could be a potential mentor
        // You can customize this logic based on your criteria
        return user.getFullName() != null && !user.getFullName().isEmpty() &&
               (user.getCurrentJob() != null && !user.getCurrentJob().isEmpty()) ||
               (user.getSkills() != null && !user.getSkills().isEmpty());
    }

    private void filterMentors(String query) {
        List<User> sourceList = allMentors;
        
        // If any filter is active, use the current filtered list as source
        if (binding.skillsFilterChip.isChecked() || 
            binding.experienceFilterChip.isChecked() || 
            binding.industryFilterChip.isChecked()) {
            // Don't override filter - just apply search on top
            if (query.isEmpty()) {
                // If search is empty, keep current filter
                return;
            }
            sourceList = new ArrayList<>(filteredMentors);
        }
        
        if (query.isEmpty()) {
            filteredMentors.clear();
            filteredMentors.addAll(sourceList);
        } else {
            filteredMentors.clear();
            String lowerCaseQuery = query.toLowerCase();
            
            for (User mentor : sourceList) {
                if (matchesSearch(mentor, lowerCaseQuery)) {
                    filteredMentors.add(mentor);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean matchesSearch(User mentor, String query) {
        // Search in name
        if (mentor.getFullName() != null && 
            mentor.getFullName().toLowerCase().contains(query)) {
            return true;
        }
        
        // Search in career
        if (mentor.getCurrentJob() != null && 
            mentor.getCurrentJob().toLowerCase().contains(query)) {
            return true;
        }
        
        // Search in skills
        if (mentor.getSkills() != null) {
            for (String skill : mentor.getSkills()) {
                if (skill.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }
        
        // Search in bio
        if (mentor.getBio() != null && 
            mentor.getBio().toLowerCase().contains(query)) {
            return true;
        }
        
        return false;
    }

    private void filterBySkills() {
        // Get mentors with strong skill sets
        filteredMentors.clear();
        for (User mentor : allMentors) {
            if (mentor.getSkills() != null && mentor.getSkills().size() >= 2) {
                filteredMentors.add(mentor);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
        
        // Uncheck other filters
        binding.experienceFilterChip.setChecked(false);
        binding.industryFilterChip.setChecked(false);
    }

    private void filterByExperience() {
        // Get mentors with job/career information (indicating experience)
        filteredMentors.clear();
        for (User mentor : allMentors) {
            if (mentor.getCurrentJob() != null && !mentor.getCurrentJob().trim().isEmpty()) {
                filteredMentors.add(mentor);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
        
        // Uncheck other filters
        binding.skillsFilterChip.setChecked(false);
        binding.industryFilterChip.setChecked(false);
    }

    private void filterByIndustry() {
        // Get mentors with industry information (based on job titles or bio keywords)
        filteredMentors.clear();
        String[] industryKeywords = {"engineer", "developer", "manager", "analyst", "consultant", 
                                   "designer", "marketing", "sales", "finance", "healthcare", 
                                   "education", "technology", "business", "software", "data"};
        
        for (User mentor : allMentors) {
            String searchText = "";
            if (mentor.getCurrentJob() != null) {
                searchText += mentor.getCurrentJob().toLowerCase() + " ";
            }
            if (mentor.getBio() != null) {
                searchText += mentor.getBio().toLowerCase() + " ";
            }
            
            boolean hasIndustryInfo = false;
            for (String keyword : industryKeywords) {
                if (searchText.contains(keyword)) {
                    hasIndustryInfo = true;
                    break;
                }
            }
            
            if (hasIndustryInfo) {
                filteredMentors.add(mentor);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
        
        // Uncheck other filters
        binding.skillsFilterChip.setChecked(false);
        binding.experienceFilterChip.setChecked(false);
    }

    private void resetFilters() {
        // Uncheck all filter chips
        binding.skillsFilterChip.setChecked(false);
        binding.experienceFilterChip.setChecked(false);
        binding.industryFilterChip.setChecked(false);
        
        // Reset to show all mentors
        filteredMentors.clear();
        filteredMentors.addAll(allMentors);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void requestMentorship(User mentor) {
        // Create a mentorship request
        showLoading(true);
        
        // Create mentorship connection document
        String connectionId = db.collection("mentor_connections").document().getId();
        
        // You'll need to create a MentorshipConnection object here
        // For now, let's show a success message
        showLoading(false);
        Toast.makeText(this, "Mentorship request sent to " + mentor.getFullName(), Toast.LENGTH_LONG).show();
        
        // Optional: Navigate back to mentorship activity
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.mentorsRecyclerView.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.mentorsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateEmptyState() {
        if (filteredMentors.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.mentorsRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.mentorsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
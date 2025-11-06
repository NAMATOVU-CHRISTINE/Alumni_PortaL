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

public class AlumniDirectoryActivity extends AppCompatActivity {
    private static final String TAG = "AlumniDirectoryActivity";
    
    private ActivityAlumniDirectoryBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AlumniAdapter adapter;
    private List<User> allUsers;
    private List<User> filteredUsers;
    
    private String selectedMajor = "All";
    private String selectedYear = "All";
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

        setupRecyclerView();
        setupSearchAndFilters();
        loadAlumniData();
    }

    private void setupRecyclerView() {
        adapter = new AlumniAdapter(filteredUsers, user -> {
            // Open profile details
            Intent intent = new Intent(this, ViewProfileActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("username", user.getUsername());
            startActivity(intent);
            
            // Log analytics
            AnalyticsHelper.logNavigation("ViewProfileActivity", "AlumniDirectoryActivity");
        });
        
        binding.alumniRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.alumniRecyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
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

        // Major filter
        binding.majorFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedMajor = parent.getItemAtPosition(position).toString();
                filterUsers();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Year filter
        binding.yearFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
                filterUsers();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Clear filters button
        binding.clearFiltersButton.setOnClickListener(v -> {
            binding.searchEditText.setText("");
            binding.majorFilterSpinner.setSelection(0);
            binding.yearFilterSpinner.setSelection(0);
            selectedMajor = "All";
            selectedYear = "All";
            searchQuery = "";
            filterUsers();
        });
    }

    private void loadAlumniData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.noResultsText.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("isAlumni", true)
                .whereEqualTo("privacySettings.showInDirectory", true)
                .orderBy("fullName", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allUsers.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setUserId(document.getId());
                        
                        // Only show users who have opted to be visible in directory
                        if (user.getPrivacySetting("showInDirectory")) {
                            allUsers.add(user);
                        }
                    }
                    
                    binding.progressBar.setVisibility(View.GONE);
                    filterUsers();
                    
                    Log.d(TAG, "Loaded " + allUsers.size() + " alumni profiles");
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.noResultsText.setVisibility(View.VISIBLE);
                    binding.noResultsText.setText("Failed to load alumni directory");
                    
                    Toast.makeText(this, "Failed to load alumni data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading alumni data", e);
                    
                    AnalyticsHelper.logError("alumni_load_failed", e.getMessage(), "AlumniDirectoryActivity");
                });
    }

    private void filterUsers() {
        filteredUsers.clear();
        
        for (User user : allUsers) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (user.getMajor() != null && user.getMajor().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (user.getCurrentJob() != null && user.getCurrentJob().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (user.getCompany() != null && user.getCompany().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (user.getSkillsAsString().toLowerCase().contains(searchQuery.toLowerCase()));
            
            boolean matchesMajor = selectedMajor.equals("All") || 
                    (user.getMajor() != null && user.getMajor().equals(selectedMajor));
            
            boolean matchesYear = selectedYear.equals("All") || 
                    (user.getGraduationYear() != null && user.getGraduationYear().equals(selectedYear));
            
            if (matchesSearch && matchesMajor && matchesYear) {
                filteredUsers.add(user);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show/hide no results message
        if (filteredUsers.isEmpty() && !allUsers.isEmpty()) {
            binding.noResultsText.setVisibility(View.VISIBLE);
            binding.noResultsText.setText("No alumni match your search criteria");
        } else if (allUsers.isEmpty()) {
            binding.noResultsText.setVisibility(View.VISIBLE);
            binding.noResultsText.setText("No alumni profiles available");
        } else {
            binding.noResultsText.setVisibility(View.GONE);
        }
        
        // Log search analytics
        if (!searchQuery.isEmpty()) {
            AnalyticsHelper.logAlumniSearch(searchQuery, filteredUsers.size());
        }
        
        binding.resultCountText.setText(filteredUsers.size() + " alumni found");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadAlumniData();
    }
}
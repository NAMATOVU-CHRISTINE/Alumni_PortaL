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
// import com.namatovu.alumniportal.adapters.JobPostingAdapter;
// import com.namatovu.alumniportal.databinding.ActivityJobBoardBinding;
import com.namatovu.alumniportal.models.JobPosting;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.List;

public class JobBoardActivity extends AppCompatActivity {
    private static final String TAG = "JobBoardActivity";
    
    // private ActivityJobBoardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    // private JobPostingAdapter adapter;
    private List<JobPosting> allJobs;
    private List<JobPosting> filteredJobs;
    
    private String selectedJobType = "All";
    private String selectedExperienceLevel = "All";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("JobBoardActivity", "HomeActivity");

        // Initialize lists
        allJobs = new ArrayList<>();
        filteredJobs = new ArrayList<>();

        setupRecyclerView();
        setupSearchAndFilters();
        setupFAB();
        loadJobPostings();
    }

    private void setupRecyclerView() {
        adapter = new JobPostingAdapter(filteredJobs, new JobPostingAdapter.OnJobClickListener() {
            @Override
            public void onJobClick(JobPosting job) {
                // Open job details
                Intent intent = new Intent(JobBoardActivity.this, JobDetailsActivity.class);
                intent.putExtra("jobId", job.getJobId());
                startActivity(intent);
                
                // Log analytics
                AnalyticsHelper.logJobPostingView(job.getTitle(), job.getCompany());
            }

            @Override
            public void onApplyClick(JobPosting job) {
                // Handle job application
                if (job.getApplicationUrl() != null && !job.getApplicationUrl().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse(job.getApplicationUrl()));
                    startActivity(intent);
                } else if (job.getContactEmail() != null && !job.getContactEmail().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("mailto:" + job.getContactEmail()));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Application for " + job.getTitle());
                    startActivity(intent);
                } else {
                    Toast.makeText(JobBoardActivity.this, "Contact information not available", Toast.LENGTH_SHORT).show();
                }
                
                // Increment application count
                incrementApplicationCount(job);
            }
        });
        
        binding.jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.jobsRecyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                filterJobs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Job type filter
        binding.jobTypeFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedJobType = parent.getItemAtPosition(position).toString();
                filterJobs();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Experience level filter
        binding.experienceFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedExperienceLevel = parent.getItemAtPosition(position).toString();
                filterJobs();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Clear filters button
        binding.clearFiltersButton.setOnClickListener(v -> {
            binding.searchEditText.setText("");
            binding.jobTypeFilterSpinner.setSelection(0);
            binding.experienceFilterSpinner.setSelection(0);
            selectedJobType = "All";
            selectedExperienceLevel = "All";
            searchQuery = "";
            filterJobs();
        });
    }

    private void setupFAB() {
        binding.addJobFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostJobActivity.class);
            startActivity(intent);
        });
    }

    private void loadJobPostings() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.noResultsText.setVisibility(View.GONE);

        db.collection("job_postings")
                .whereEqualTo("isActive", true)
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allJobs.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        JobPosting job = document.toObject(JobPosting.class);
                        job.setJobId(document.getId());
                        
                        // Only show jobs that are still valid
                        if (job.isValidForDisplay()) {
                            allJobs.add(job);
                        }
                    }
                    
                    binding.progressBar.setVisibility(View.GONE);
                    filterJobs();
                    
                    Log.d(TAG, "Loaded " + allJobs.size() + " active job postings");
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.noResultsText.setVisibility(View.VISIBLE);
                    binding.noResultsText.setText("Failed to load job postings");
                    
                    Toast.makeText(this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading job postings", e);
                    
                    AnalyticsHelper.logError("jobs_load_failed", e.getMessage(), "JobBoardActivity");
                });
    }

    private void filterJobs() {
        filteredJobs.clear();
        
        for (JobPosting job : allJobs) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                    (job.getTitle() != null && job.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (job.getCompany() != null && job.getCompany().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (job.getDescription() != null && job.getDescription().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (job.getLocation() != null && job.getLocation().toLowerCase().contains(searchQuery.toLowerCase()));
            
            boolean matchesJobType = selectedJobType.equals("All") || 
                    (job.getJobType() != null && job.getJobType().equalsIgnoreCase(selectedJobType));
            
            boolean matchesExperience = selectedExperienceLevel.equals("All") || 
                    (job.getExperienceLevel() != null && job.getExperienceLevel().equalsIgnoreCase(selectedExperienceLevel));
            
            if (matchesSearch && matchesJobType && matchesExperience) {
                filteredJobs.add(job);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show/hide no results message
        if (filteredJobs.isEmpty() && !allJobs.isEmpty()) {
            binding.noResultsText.setVisibility(View.VISIBLE);
            binding.noResultsText.setText("No jobs match your search criteria");
        } else if (allJobs.isEmpty()) {
            binding.noResultsText.setVisibility(View.VISIBLE);
            binding.noResultsText.setText("No job postings available");
        } else {
            binding.noResultsText.setVisibility(View.GONE);
        }
        
        binding.resultCountText.setText(filteredJobs.size() + " jobs found");
    }

    private void incrementApplicationCount(JobPosting job) {
        if (job.getJobId() != null) {
            db.collection("job_postings").document(job.getJobId())
                    .update("applicationCount", job.getApplicationCount() + 1)
                    .addOnSuccessListener(aVoid -> {
                        job.incrementApplicationCount();
                        Log.d(TAG, "Application count incremented for job: " + job.getTitle());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to increment application count", e);
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadJobPostings();
    }
}
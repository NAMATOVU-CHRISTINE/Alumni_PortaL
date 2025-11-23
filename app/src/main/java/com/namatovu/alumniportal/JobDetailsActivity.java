package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.databinding.ActivityJobDetailsBinding;
import com.namatovu.alumniportal.models.JobPosting;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

public class JobDetailsActivity extends AppCompatActivity {
    private static final String TAG = "JobDetailsActivity";
    private static final int EDIT_JOB_REQUEST = 100;
    
    private ActivityJobDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String jobId;
    private JobPosting currentJob;
    private String currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        
        // Get job ID from intent
        jobId = getIntent().getStringExtra("jobId");
        
        setupToolbar();
        setupButtons();
        
        if (jobId != null) {
            loadJobDetails();
        } else {
            Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Job Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupButtons() {
        binding.applyButton.setOnClickListener(v -> applyForJob());
        binding.editButton.setOnClickListener(v -> editJob());
        binding.deleteButton.setOnClickListener(v -> confirmDeleteJob());
    }
    
    private void loadJobDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);
        
        db.collection("job_postings").document(jobId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                binding.progressBar.setVisibility(View.GONE);
                
                if (documentSnapshot.exists()) {
                    currentJob = documentSnapshot.toObject(JobPosting.class);
                    if (currentJob != null) {
                        currentJob.setJobId(documentSnapshot.getId());
                        displayJobDetails();
                        updateOwnerButtons();
                        incrementViewCount();
                    }
                } else {
                    Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load job", e);
                finish();
            });
    }
    
    private void displayJobDetails() {
        binding.contentLayout.setVisibility(View.VISIBLE);
        
        binding.jobTitleText.setText(currentJob.getTitle());
        binding.companyText.setText(currentJob.getCompany());
        binding.locationText.setText(currentJob.getFormattedLocation());
        binding.jobTypeText.setText(currentJob.getJobType());
        binding.experienceLevelText.setText(currentJob.getExperienceLevel());
        binding.salaryText.setText(currentJob.getFormattedSalary());
        binding.postedByText.setText("Posted by " + currentJob.getPostedByName());
        binding.postedAtText.setText(currentJob.getTimeAgo());
        binding.descriptionText.setText(currentJob.getDescription());
        
        if (currentJob.getRequirements() != null && !currentJob.getRequirements().isEmpty()) {
            binding.requirementsText.setText(currentJob.getRequirements());
            binding.requirementsSection.setVisibility(View.VISIBLE);
        } else {
            binding.requirementsSection.setVisibility(View.GONE);
        }
        
        if (currentJob.getContactEmail() != null && !currentJob.getContactEmail().isEmpty()) {
            binding.contactEmailText.setText(currentJob.getContactEmail());
            binding.contactSection.setVisibility(View.VISIBLE);
        } else {
            binding.contactSection.setVisibility(View.GONE);
        }
        
        binding.viewCountText.setText(currentJob.getViewCount() + " views");
        binding.applicationCountText.setText(currentJob.getApplicationCount() + " applications");
    }
    
    private void updateOwnerButtons() {
        boolean isOwner = currentJob != null && 
                         currentJob.getPostedBy() != null && 
                         currentJob.getPostedBy().equals(currentUserId);
        
        if (isOwner) {
            binding.editButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.applyButton.setVisibility(View.GONE);
        } else {
            binding.editButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.GONE);
            binding.applyButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void applyForJob() {
        if (currentJob.getApplicationUrl() != null && !currentJob.getApplicationUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(currentJob.getApplicationUrl()));
            startActivity(intent);
        } else if (currentJob.getContactEmail() != null && !currentJob.getContactEmail().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("mailto:" + currentJob.getContactEmail()));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Application for " + currentJob.getTitle());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No application method available", Toast.LENGTH_SHORT).show();
        }
        
        incrementApplicationCount();
    }
    
    private void editJob() {
        Intent intent = new Intent(this, PostJobActivity.class);
        intent.putExtra("jobId", jobId);
        startActivityForResult(intent, EDIT_JOB_REQUEST);
    }
    
    private void confirmDeleteJob() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Job")
            .setMessage("Are you sure you want to delete this job posting? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteJob())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteJob() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("job_postings").document(jobId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Job deleted successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Job deleted: " + jobId);
                
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to delete job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to delete job", e);
            });
    }
    
    private void incrementViewCount() {
        if (jobId != null) {
            db.collection("job_postings").document(jobId)
                .update("viewCount", currentJob.getViewCount() + 1)
                .addOnSuccessListener(aVoid -> {
                    currentJob.incrementViewCount();
                    binding.viewCountText.setText(currentJob.getViewCount() + " views");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment view count", e));
        }
    }
    
    private void incrementApplicationCount() {
        if (jobId != null) {
            db.collection("job_postings").document(jobId)
                .update("applicationCount", currentJob.getApplicationCount() + 1)
                .addOnSuccessListener(aVoid -> {
                    currentJob.incrementApplicationCount();
                    binding.applicationCountText.setText(currentJob.getApplicationCount() + " applications");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment application count", e));
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_JOB_REQUEST && resultCode == RESULT_OK) {
            // Reload job details after edit
            loadJobDetails();
        }
    }
}

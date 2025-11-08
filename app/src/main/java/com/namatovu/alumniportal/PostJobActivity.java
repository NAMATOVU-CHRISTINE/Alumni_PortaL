package com.namatovu.alumniportal;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.databinding.ActivityPostJobBinding;
import com.namatovu.alumniportal.models.JobPosting;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

public class PostJobActivity extends AppCompatActivity {
    private static final String TAG = "PostJobActivity";
    
    private ActivityPostJobBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String currentUserName;
    private String jobIdToEdit = null; // For edit mode
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostJobBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        
        // Check if editing existing job
        jobIdToEdit = getIntent().getStringExtra("jobId");
        
        setupToolbar();
        setupSpinners();
        setupButtons();
        loadCurrentUser();
        
        if (jobIdToEdit != null) {
            loadJobForEditing();
        }
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(jobIdToEdit != null ? "Edit Job" : "Post a Job");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupSpinners() {
        // Job Type Spinner
        String[] jobTypes = {"Full-time", "Part-time", "Contract", "Internship", "Freelance"};
        ArrayAdapter<String> jobTypeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, jobTypes);
        binding.jobTypeSpinner.setAdapter(jobTypeAdapter);
        
        // Experience Level Spinner
        String[] experienceLevels = {"Entry Level", "Mid Level", "Senior Level", "Executive"};
        ArrayAdapter<String> experienceAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, experienceLevels);
        binding.experienceLevelSpinner.setAdapter(experienceAdapter);
    }
    
    private void setupButtons() {
        binding.postJobButton.setOnClickListener(v -> {
            if (validateInputs()) {
                if (jobIdToEdit != null) {
                    updateJob();
                } else {
                    postJob();
                }
            }
        });
        
        binding.cancelButton.setOnClickListener(v -> finish());
    }
    
    private void loadCurrentUser() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, "Please log in to post a job", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    currentUserName = user != null ? user.getFullName() : "Unknown User";
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load user", e);
                currentUserName = "Unknown User";
            });
    }
    
    private void loadJobForEditing() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("job_postings").document(jobIdToEdit)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                binding.progressBar.setVisibility(View.GONE);
                
                if (documentSnapshot.exists()) {
                    JobPosting job = documentSnapshot.toObject(JobPosting.class);
                    if (job != null) {
                        populateFields(job);
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
    
    private void populateFields(JobPosting job) {
        binding.titleEditText.setText(job.getTitle());
        binding.companyEditText.setText(job.getCompany());
        binding.descriptionEditText.setText(job.getDescription());
        binding.requirementsEditText.setText(job.getRequirements());
        binding.locationEditText.setText(job.getLocation());
        binding.salaryEditText.setText(job.getSalary());
        binding.applicationUrlEditText.setText(job.getApplicationUrl());
        binding.contactEmailEditText.setText(job.getContactEmail());
        binding.remoteCheckbox.setChecked(job.isRemote());
        
        // Set spinners
        if (job.getJobType() != null) {
            int jobTypePosition = getSpinnerPosition(binding.jobTypeSpinner, job.getJobType());
            if (jobTypePosition >= 0) {
                binding.jobTypeSpinner.setSelection(jobTypePosition);
            }
        }
        
        if (job.getExperienceLevel() != null) {
            int expPosition = getSpinnerPosition(binding.experienceLevelSpinner, job.getExperienceLevel());
            if (expPosition >= 0) {
                binding.experienceLevelSpinner.setSelection(expPosition);
            }
        }
    }
    
    private int getSpinnerPosition(android.widget.Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }
    
    private boolean validateInputs() {
        String title = binding.titleEditText.getText().toString().trim();
        String company = binding.companyEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String location = binding.locationEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            binding.titleEditText.setError("Job title is required");
            binding.titleEditText.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(company)) {
            binding.companyEditText.setError("Company name is required");
            binding.companyEditText.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(description)) {
            binding.descriptionEditText.setError("Job description is required");
            binding.descriptionEditText.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(location)) {
            binding.locationEditText.setError("Location is required");
            binding.locationEditText.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void postJob() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.postJobButton.setEnabled(false);
        
        // Create job posting
        JobPosting job = createJobFromInputs();
        
        Log.d(TAG, "Attempting to post job. User ID: " + currentUserId);
        Log.d(TAG, "Job data: " + job.toMap().toString());
        
        // Save to Firestore
        db.collection("job_postings")
            .add(job.toMap())
            .addOnSuccessListener(documentReference -> {
                binding.progressBar.setVisibility(View.GONE);
                
                Log.d(TAG, "Job posted successfully with ID: " + documentReference.getId());
                
                // Verify the job was actually saved
                documentReference.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Log.d(TAG, "Job verified in Firestore: " + snapshot.getData());
                        Toast.makeText(this, "Job posted successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Job document doesn't exist after creation!");
                        Toast.makeText(this, "Job may not have saved properly", Toast.LENGTH_LONG).show();
                    }
                });
                
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.postJobButton.setEnabled(true);
                
                Log.e(TAG, "Failed to post job: " + e.getClass().getSimpleName(), e);
                Log.e(TAG, "Error message: " + e.getMessage());
                Toast.makeText(this, "Failed to post job: " + e.getMessage(), Toast.LENGTH_LONG).show();
                
                AnalyticsHelper.logError("job_post_failed", e.getMessage(), "PostJobActivity");
            });
    }
    
    private void updateJob() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.postJobButton.setEnabled(false);
        
        // Create updated job posting
        JobPosting job = createJobFromInputs();
        
        // Update in Firestore
        db.collection("job_postings").document(jobIdToEdit)
            .set(job.toMap())
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                
                Log.d(TAG, "Job updated successfully");
                Toast.makeText(this, "Job updated successfully!", Toast.LENGTH_SHORT).show();
                
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.postJobButton.setEnabled(true);
                
                Toast.makeText(this, "Failed to update job: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to update job", e);
            });
    }
    
    private JobPosting createJobFromInputs() {
        String title = binding.titleEditText.getText().toString().trim();
        String company = binding.companyEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String requirements = binding.requirementsEditText.getText().toString().trim();
        String location = binding.locationEditText.getText().toString().trim();
        String salary = binding.salaryEditText.getText().toString().trim();
        String applicationUrl = binding.applicationUrlEditText.getText().toString().trim();
        String contactEmail = binding.contactEmailEditText.getText().toString().trim();
        String jobType = binding.jobTypeSpinner.getSelectedItem().toString();
        String experienceLevel = binding.experienceLevelSpinner.getSelectedItem().toString();
        boolean isRemote = binding.remoteCheckbox.isChecked();
        
        JobPosting job = new JobPosting(title, company, description, currentUserId, currentUserName);
        job.setRequirements(requirements);
        job.setLocation(location);
        job.setSalary(salary);
        job.setApplicationUrl(applicationUrl);
        job.setContactEmail(contactEmail);
        job.setJobType(jobType);
        job.setExperienceLevel(experienceLevel);
        job.setRemote(isRemote);
        
        return job;
    }
}

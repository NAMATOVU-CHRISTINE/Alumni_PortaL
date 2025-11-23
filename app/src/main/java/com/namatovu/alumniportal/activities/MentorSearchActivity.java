package com.namatovu.alumniportal.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.adapters.MentorAdapter;
import com.namatovu.alumniportal.database.AlumniDatabase;
import com.namatovu.alumniportal.database.entities.MentorEntity;
import com.namatovu.alumniportal.repository.AlumniRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MentorSearchActivity extends AppCompatActivity {
    
    private EditText editTextSearch;
    private Spinner spinnerCategory;
    private Spinner spinnerGraduationYear;
    private ChipGroup chipGroupFilters;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private MentorAdapter adapter;
    private List<MentorEntity> allMentors = new ArrayList<>();
    private List<MentorEntity> filteredMentors = new ArrayList<>();
    
    private AlumniDatabase localDb;
    private FirebaseFirestore db;
    private ExecutorService executorService;
    
    private String selectedCategory = "All";
    private String selectedYear = "All";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_search);
        
        // Initialize
        localDb = AlumniDatabase.getInstance(this);
        db = FirebaseFirestore.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        
        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        
        // Load from local database first (fast!)
        loadMentorsFromLocal();
        
        // Then sync from Firebase in background
        syncMentorsFromFirebase();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Find a Mentor");
        }
    }
    
    private void initViews() {
        try {
            editTextSearch = findViewById(R.id.editTextSearch);
            spinnerCategory = findViewById(R.id.spinnerCategory);
            spinnerGraduationYear = findViewById(R.id.spinnerGraduationYear);
            chipGroupFilters = findViewById(R.id.chipGroupFilters);
            recyclerView = findViewById(R.id.recyclerView);
            progressBar = findViewById(R.id.progressBar);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        } catch (Exception e) {
            // Layout resources may not be available
        }
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            syncMentorsFromFirebase();
        });
    }
    
    private void setupRecyclerView() {
        adapter = new MentorAdapter(this, filteredMentors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupSearch() {
        if (editTextSearch == null) return;
        
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchMentors(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupFilters() {
        if (spinnerCategory == null || spinnerGraduationYear == null || chipGroupFilters == null) return;
        
        // Setup category spinner
        List<String> categories = Arrays.asList(
            "All", "Technology", "Business", "Healthcare", "Education", 
            "Engineering", "Finance", "Marketing", "Design", "Other"
        );
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                applyFilters();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup graduation year spinner
        List<String> years = new ArrayList<>();
        years.add("All");
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        for (int year = currentYear; year >= currentYear - 50; year--) {
            years.add(String.valueOf(year));
        }
        
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGraduationYear.setAdapter(yearAdapter);
        
        spinnerGraduationYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = years.get(position);
                applyFilters();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup filter chips
        setupFilterChips();
    }
    
    private void setupFilterChips() {
        String[] filters = {"Available Now", "Top Rated", "Most Experienced"};
        
        for (String filter : filters) {
            Chip chip = new Chip(this);
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                applyFilters();
            });
            chipGroupFilters.addView(chip);
        }
    }
    
    private void loadMentorsFromLocal() {
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                List<MentorEntity> mentors = localDb.mentorDao().getAllMentors();
                
                runOnUiThread(() -> {
                    allMentors.clear();
                    if (mentors != null && !mentors.isEmpty()) {
                        allMentors.addAll(mentors);
                        applyFilters();
                    } else {
                        // If no local mentors, sync from Firebase immediately
                        syncMentorsFromFirebase();
                    }
                    progressBar.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading mentors", Toast.LENGTH_SHORT).show();
                    // Try Firebase sync on error
                    syncMentorsFromFirebase();
                });
            }
        });
    }
    
    private void syncMentorsFromFirebase() {
        // Load mentors from users collection (alumni and staff only)
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MentorEntity> mentors = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Only include alumni and staff as mentors
                        String userType = document.getString("userType");
                        boolean isMentor = "alumni".equalsIgnoreCase(userType) || "staff".equalsIgnoreCase(userType);
                        
                        if (!isMentor) {
                            continue; // Skip students
                        }
                        
                        // Filter out incomplete profiles - require essential fields
                        String fullName = document.getString("fullName");
                        String currentJob = document.getString("currentJob");
                        String company = document.getString("company");
                        String bio = document.getString("bio");
                        
                        // Skip if essential fields are null or empty
                        if (fullName == null || fullName.trim().isEmpty() ||
                            currentJob == null || currentJob.trim().isEmpty() ||
                            company == null || company.trim().isEmpty() ||
                            bio == null || bio.trim().isEmpty()) {
                            continue; // Skip incomplete profiles
                        }
                        
                        MentorEntity mentor = new MentorEntity();
                        mentor.setMentorId(document.getId());
                        mentor.setFullName(fullName);
                        mentor.setEmail(document.getString("email"));
                        mentor.setProfileImageUrl(document.getString("profileImageUrl"));
                        mentor.setCurrentJob(currentJob);
                        mentor.setCompany(company);
                        mentor.setBio(bio);
                        mentor.setGraduationYear(document.getString("graduationYear"));
                        
                        // Set default values for fields not in users collection
                        mentor.setExpertise(mentor.getCurrentJob() != null ? mentor.getCurrentJob() : "");
                        mentor.setCategory(mentor.getCompany() != null ? mentor.getCompany() : "");
                        mentor.setCourse(mentor.getGraduationYear() != null ? mentor.getGraduationYear() : "");
                        mentor.setYearsOfExperience(0);
                        mentor.setMenteeCount(0);
                        mentor.setRating(0.0);
                        mentor.setAvailable(true);
                        
                        mentor.setLastSyncTime(System.currentTimeMillis());
                        mentors.add(mentor);
                    }
                    
                    // Save to local database in background
                    executorService.execute(() -> {
                        localDb.mentorDao().deleteAllMentors();
                        localDb.mentorDao().insertMentors(mentors);
                        
                        runOnUiThread(() -> {
                            allMentors.clear();
                            allMentors.addAll(mentors);
                            applyFilters();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(this, "Mentors updated", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Error syncing mentors", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void searchMentors(String query) {
        if (query.isEmpty()) {
            applyFilters();
            return;
        }
        
        executorService.execute(() -> {
            List<MentorEntity> results = localDb.mentorDao().searchMentors(query);
            
            runOnUiThread(() -> {
                filteredMentors.clear();
                filteredMentors.addAll(results);
                adapter.notifyDataSetChanged();
            });
        });
    }
    
    private void applyFilters() {
        executorService.execute(() -> {
            List<MentorEntity> results = new ArrayList<>(allMentors);
            
            // Filter by category
            if (!selectedCategory.equals("All")) {
                results.removeIf(mentor -> 
                    mentor.getCategory() == null || !mentor.getCategory().equals(selectedCategory));
            }
            
            // Filter by graduation year
            if (!selectedYear.equals("All")) {
                results.removeIf(mentor -> 
                    mentor.getGraduationYear() == null || !mentor.getGraduationYear().equals(selectedYear));
            }
            
            // Filter by chips
            for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupFilters.getChildAt(i);
                if (chip.isChecked()) {
                    String filter = chip.getText().toString();
                    
                    if (filter.equals("Available Now")) {
                        results.removeIf(mentor -> !mentor.isAvailable());
                    } else if (filter.equals("Top Rated")) {
                        results.sort((m1, m2) -> Double.compare(m2.getRating(), m1.getRating()));
                    } else if (filter.equals("Most Experienced")) {
                        results.sort((m1, m2) -> Integer.compare(m2.getYearsOfExperience(), m1.getYearsOfExperience()));
                    }
                }
            }
            
            runOnUiThread(() -> {
                filteredMentors.clear();
                filteredMentors.addAll(results);
                adapter.notifyDataSetChanged();
            });
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

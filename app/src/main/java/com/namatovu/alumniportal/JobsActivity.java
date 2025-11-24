package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.adapters.OpportunityAdapter;
import com.namatovu.alumniportal.adapters.FeaturedOpportunityAdapter;
import com.namatovu.alumniportal.models.Opportunity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * JobsActivity - Career opportunities for students and alumni
 * Features: Search, filtering, featured opportunities, and job posting
 */
public class JobsActivity extends AppCompatActivity implements 
    OpportunityAdapter.OnOpportunityClickListener,
    FeaturedOpportunityAdapter.OnFeaturedOpportunityClickListener {

    private static final String TAG = "JobsActivity";
    
    private RecyclerView recyclerViewFeatured, recyclerViewOpportunities;
    private FeaturedOpportunityAdapter featuredAdapter;
    private OpportunityAdapter opportunityAdapter;
    private EditText etSearch;
    private Spinner spinnerSort;
    private ChipGroup chipGroupCategories;
    private ExtendedFloatingActionButton fabAddOpportunity;
    private View emptyStateLayout;
    private TextView tvResultsCount;
    private MaterialButton btnClearFilters;
    
    private List<Opportunity> allOpportunities;
    private List<Opportunity> featuredOpportunities;
    private String currentCategory = "All";
    private String currentSortOption = "Date";
    
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupToolbar();
        setupRecyclerViews();
        setupSearchAndFilters();
        setupFab();
        loadOpportunitiesFromFirestore();
        
        updateEmptyState();
    }

    private void initializeViews() {
        try {
            recyclerViewFeatured = findViewById(R.id.recyclerViewFeatured);
            recyclerViewOpportunities = findViewById(R.id.recyclerViewOpportunities);
            etSearch = findViewById(R.id.etSearch);
            spinnerSort = findViewById(R.id.spinnerSort);
            chipGroupCategories = findViewById(R.id.chipGroupCategories);
            fabAddOpportunity = findViewById(R.id.fabAddOpportunity);
            emptyStateLayout = findViewById(R.id.emptyStateLayout);
            tvResultsCount = findViewById(R.id.tvResultsCount);
            btnClearFilters = findViewById(R.id.btnClearFilters);
            
            // Verify critical views exist
            if (recyclerViewFeatured == null || recyclerViewOpportunities == null || 
                etSearch == null || spinnerSort == null || chipGroupCategories == null) {
                Log.e(TAG, "Critical views not found in layout");
                Toast.makeText(this, "Error loading layout", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            allOpportunities = new ArrayList<>();
            featuredOpportunities = new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("💼 Career Opportunities");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerViews() {
        try {
            if (recyclerViewFeatured == null || recyclerViewOpportunities == null) {
                Log.e(TAG, "RecyclerViews not found");
                return;
            }
            
            // Featured opportunities (horizontal)
            featuredAdapter = new FeaturedOpportunityAdapter(this, featuredOpportunities);
            featuredAdapter.setOnFeaturedOpportunityClickListener(this);
            
            LinearLayoutManager featuredLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerViewFeatured.setLayoutManager(featuredLayoutManager);
            recyclerViewFeatured.setAdapter(featuredAdapter);
            
            // All opportunities (vertical)
            opportunityAdapter = new OpportunityAdapter(this, allOpportunities);
            opportunityAdapter.setOnOpportunityClickListener(this);
            
            LinearLayoutManager opportunityLayoutManager = new LinearLayoutManager(this);
            recyclerViewOpportunities.setLayoutManager(opportunityLayoutManager);
            recyclerViewOpportunities.setAdapter(opportunityAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerViews", e);
        }
    }

    private void setupSearchAndFilters() {
        try {
            // Setup search
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterOpportunities();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            // Setup sort spinner
            if (spinnerSort != null) {
                try {
                    String[] sortOptions = {"Date", "Deadline", "Relevance", "Company"};
                    ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
                    sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSort.setAdapter(sortAdapter);
                    
                    // Set listener after a brief delay to avoid initialization issues
                    spinnerSort.post(() -> {
                        try {
                            spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (position >= 0 && position < sortOptions.length) {
                                        currentSortOption = sortOptions[position];
                                        sortAndFilterOpportunities();
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting spinner listener", e);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error setting up sort spinner", e);
                }
            }

            // Setup category chips
            if (chipGroupCategories != null) {
                chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
                    try {
                        if (checkedIds.isEmpty()) {
                            currentCategory = "All";
                        } else {
                            Chip selectedChip = findViewById(checkedIds.get(0));
                            if (selectedChip != null) {
                                String chipText = selectedChip.getText().toString();
                                
                                // Extract category from chip text
                                if (chipText.equals("All")) {
                                    currentCategory = "All";
                                } else if (chipText.contains("Internships")) {
                                    currentCategory = "Internship";
                                } else if (chipText.contains("Jobs")) {
                                    currentCategory = "Job";
                                } else if (chipText.contains("Graduate Training")) {
                                    currentCategory = "Graduate Training";
                                } else if (chipText.contains("Apprenticeships")) {
                                    currentCategory = "Apprenticeship";
                                }
                            }
                        }
                        filterOpportunities();
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling chip selection", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up search and filters", e);
        }
    }

    private void setupFab() {
        try {
            // TODO: Check user role - only show for mentors/admins
            if (fabAddOpportunity != null) {
                fabAddOpportunity.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AddOpportunityActivity.class);
                    startActivityForResult(intent, 100);
                });
                
                // Long press to force reload (for debugging)
                fabAddOpportunity.setOnLongClickListener(v -> {
                    Toast.makeText(this, "Reloading opportunities...", Toast.LENGTH_SHORT).show();
                    loadOpportunitiesFromFirestore();
                    return true;
                });
            }
            
            // Setup clear filters button
            if (btnClearFilters != null) {
                btnClearFilters.setOnClickListener(v -> clearFilters());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up FAB", e);
        }
    }
    
    private void clearFilters() {
        try {
            if (etSearch != null) {
                etSearch.setText("");
            }
            currentCategory = "All";
            currentSortOption = "Date";
            
            // Reset chip selection
            if (chipGroupCategories != null) {
                chipGroupCategories.clearCheck();
                Chip chipAll = findViewById(R.id.chipAll);
                if (chipAll != null) {
                    chipAll.setChecked(true);
                }
            }
            
            // Reset spinner
            if (spinnerSort != null) {
                spinnerSort.setSelection(0);
            }
            
            filterOpportunities();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing filters", e);
        }
    }

    private void filterOpportunities() {
        try {
            String query = etSearch != null ? etSearch.getText().toString().toLowerCase().trim() : "";
            
            Log.d(TAG, "Filtering opportunities. Query: '" + query + "', Category: '" + currentCategory + "'");
            Log.d(TAG, "Total opportunities before filter: " + allOpportunities.size());
            
            List<Opportunity> filtered = new ArrayList<>();
            for (Opportunity opportunity : allOpportunities) {
                String oppCategory = opportunity.getCategory() != null ? opportunity.getCategory() : "";
                boolean matchesCategory = currentCategory.equals("All") || oppCategory.equals(currentCategory);
                boolean matchesSearch = query.isEmpty() || 
                    (opportunity.getTitle() != null && opportunity.getTitle().toLowerCase().contains(query)) ||
                    (opportunity.getCompany() != null && opportunity.getCompany().toLowerCase().contains(query)) ||
                    (opportunity.getDescription() != null && opportunity.getDescription().toLowerCase().contains(query));
                
                Log.d(TAG, "Opportunity: " + opportunity.getTitle() + 
                      ", Category match: " + matchesCategory + 
                      " (opp=" + oppCategory + ", filter=" + currentCategory + ")" +
                      ", Search match: " + matchesSearch);
                
                if (matchesCategory && matchesSearch) {
                    filtered.add(opportunity);
                }
            }
            
            Log.d(TAG, "Filtered opportunities count: " + filtered.size());
            
            // Apply sorting
            sortOpportunities(filtered);
            
            if (opportunityAdapter != null) {
                opportunityAdapter.updateOpportunities(filtered);
            }
            updateResultsCount(filtered.size());
            updateEmptyState();
        } catch (Exception e) {
            Log.e(TAG, "Error filtering opportunities", e);
        }
    }
    
    private void updateResultsCount(int count) {
        if (tvResultsCount != null) {
            String resultsText;
            if (count == 0) {
                resultsText = "No results";
            } else if (count == 1) {
                resultsText = "1 result";
            } else {
                resultsText = count + " results";
            }
            tvResultsCount.setText(resultsText);
        }
    }

    private void sortAndFilterOpportunities() {
        filterOpportunities(); // This will apply both filtering and sorting
    }

    private void sortOpportunities(List<Opportunity> opportunities) {
        switch (currentSortOption) {
            case "Date":
                Collections.sort(opportunities, (o1, o2) -> {
                    if (o1.getDatePosted() == null && o2.getDatePosted() == null) return 0;
                    if (o1.getDatePosted() == null) return 1;
                    if (o2.getDatePosted() == null) return -1;
                    return o2.getDatePosted().compareTo(o1.getDatePosted()); // Newest first
                });
                break;
            case "Deadline":
                Collections.sort(opportunities, (o1, o2) -> {
                    if (o1.getApplicationDeadline() == null && o2.getApplicationDeadline() == null) return 0;
                    if (o1.getApplicationDeadline() == null) return 1;
                    if (o2.getApplicationDeadline() == null) return -1;
                    return o1.getApplicationDeadline().compareTo(o2.getApplicationDeadline()); // Earliest first
                });
                break;
            case "Company":
                Collections.sort(opportunities, (o1, o2) -> o1.getCompany().compareToIgnoreCase(o2.getCompany()));
                break;
            case "Relevance":
                // For now, sort by featured status and then by date
                Collections.sort(opportunities, (o1, o2) -> {
                    if (o1.isFeatured() && !o2.isFeatured()) return -1;
                    if (!o1.isFeatured() && o2.isFeatured()) return 1;
                    if (o1.getDatePosted() == null && o2.getDatePosted() == null) return 0;
                    if (o1.getDatePosted() == null) return 1;
                    if (o2.getDatePosted() == null) return -1;
                    return o2.getDatePosted().compareTo(o1.getDatePosted());
                });
                break;
        }
    }

    private void updateEmptyState() {
        int itemCount = opportunityAdapter.getItemCount();
        
        if (itemCount == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewOpportunities.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewOpportunities.setVisibility(View.VISIBLE);
        }
    }

    private void loadOpportunitiesFromFirestore() {
        Log.d(TAG, "Loading opportunities from Firestore");
        
        db.collection("job_opportunities")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allOpportunities.clear();
                featuredOpportunities.clear();
                
                Log.d(TAG, "Firestore query returned " + queryDocumentSnapshots.size() + " documents");
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Log.d(TAG, "Processing document: " + document.getId());
                        Log.d(TAG, "Document data: " + document.getData().toString());
                        
                        String title = document.getString("title");
                        String company = document.getString("company");
                        String category = document.getString("category");
                        String description = document.getString("description");
                        Date deadline = document.getDate("applicationDeadline");
                        
                        Log.d(TAG, "Parsed fields - Title: " + title + ", Company: " + company + 
                              ", Category: " + category + ", Deadline: " + deadline);
                        
                        // Check if deadline has passed
                        if (deadline != null && deadline.getTime() < System.currentTimeMillis()) {
                            Log.d(TAG, "Opportunity expired, deleting: " + title);
                            // Auto-delete expired opportunity
                            db.collection("job_opportunities").document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted expired opportunity: " + title))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete expired opportunity", e));
                            continue; // Skip adding to list
                        }
                        
                        if (title != null && company != null && category != null && description != null && deadline != null) {
                            Opportunity opp = new Opportunity(title, company, category, description, deadline);
                            opp.setId(document.getId());
                            
                            // Set optional fields
                            String location = document.getString("location");
                            if (location != null && !location.isEmpty()) opp.setLocation(location);
                            
                            String salaryRange = document.getString("salaryRange");
                            if (salaryRange != null && !salaryRange.isEmpty()) opp.setSalaryRange(salaryRange);
                            
                            String requirements = document.getString("requirements");
                            if (requirements != null && !requirements.isEmpty()) opp.setRequirements(requirements);
                            
                            String applicationLink = document.getString("applicationInstructions");
                            if (applicationLink != null && !applicationLink.isEmpty()) opp.setApplicationLink(applicationLink);
                            
                            String postedBy = document.getString("postedBy");
                            if (postedBy != null) opp.setPostedBy(postedBy);
                            
                            Date datePosted = document.getDate("datePosted");
                            if (datePosted != null) opp.setDatePosted(datePosted);
                            
                            Boolean isFeatured = document.getBoolean("isFeatured");
                            if (isFeatured != null) opp.setFeatured(isFeatured);
                            
                            Long applicationsCount = document.getLong("applicationsCount");
                            if (applicationsCount != null) opp.setApplicationsCount(applicationsCount.intValue());
                            
                            allOpportunities.add(opp);
                            Log.d(TAG, "Added opportunity: " + title);
                            
                            if (opp.isFeatured()) {
                                featuredOpportunities.add(opp);
                                Log.d(TAG, "Added to featured: " + title);
                            }
                        } else {
                            Log.w(TAG, "Skipping document with missing required fields: " + document.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing opportunity document: " + document.getId(), e);
                    }
                }
                
                Log.d(TAG, "Loaded " + allOpportunities.size() + " opportunities from Firestore");
                Log.d(TAG, "Featured opportunities: " + featuredOpportunities.size());
                
                // Update adapters directly without filtering
                // (filtering will be applied when user interacts with search/filter UI)
                Log.d(TAG, "Updating featured adapter with " + featuredOpportunities.size() + " items");
                featuredAdapter.updateOpportunities(new ArrayList<>(featuredOpportunities));
                
                Log.d(TAG, "Updating opportunity adapter with " + allOpportunities.size() + " items");
                opportunityAdapter.updateOpportunities(new ArrayList<>(allOpportunities));
                
                updateEmptyState();
                
                Log.d(TAG, "RecyclerView visibility: " + recyclerViewOpportunities.getVisibility());
                Log.d(TAG, "Empty state visibility: " + emptyStateLayout.getVisibility());
                Log.d(TAG, "Adapter item count: " + opportunityAdapter.getItemCount());
                
                if (allOpportunities.isEmpty()) {
                    Toast.makeText(this, "No opportunities found. Post one to get started!", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load opportunities from Firestore", e);
                Toast.makeText(this, "Failed to load opportunities: " + e.getMessage(), Toast.LENGTH_LONG).show();
                updateEmptyState();
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Reload opportunities from Firestore after adding new one
            loadOpportunitiesFromFirestore();
        } else if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            // Handle result from OpportunityDetailActivity
            String opportunityId = data.getStringExtra("opportunity_id");
            int updatedApplications = data.getIntExtra("updated_applications", 0);
            boolean isSaved = data.getBooleanExtra("is_saved", false);
            
            if (opportunityId != null) {
                // Find and update the opportunity in our lists
                updateOpportunityInLists(opportunityId, updatedApplications, isSaved);
            }
        }
    }

    private void updateOpportunityInLists(String opportunityId, int updatedApplications, boolean isSaved) {
        // Update in all opportunities list
        for (Opportunity opp : allOpportunities) {
            if (opp.getId().equals(opportunityId)) {
                opp.setApplicationsCount(updatedApplications);
                opp.setSaved(isSaved);
                break;
            }
        }
        
        // Update in featured opportunities list
        for (Opportunity opp : featuredOpportunities) {
            if (opp.getId().equals(opportunityId)) {
                opp.setApplicationsCount(updatedApplications);
                opp.setSaved(isSaved);
                break;
            }
        }
        
        // Refresh adapters
        opportunityAdapter.notifyDataSetChanged();
        featuredAdapter.notifyDataSetChanged();
    }

    // OpportunityAdapter.OnOpportunityClickListener implementation
    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        Intent intent = new Intent(this, OpportunityDetailActivity.class);
        intent.putExtra("opportunity_id", opportunity.getId());
        intent.putExtra("opportunity_title", opportunity.getTitle());
        intent.putExtra("opportunity_company", opportunity.getCompany());
        intent.putExtra("opportunity_category", opportunity.getCategory());
        intent.putExtra("opportunity_description", opportunity.getDescription());
        intent.putExtra("opportunity_location", opportunity.getLocation());
        intent.putExtra("opportunity_deadline", opportunity.getFormattedDeadline());
        intent.putExtra("opportunity_applications", opportunity.getApplicationsCount());
        startActivityForResult(intent, 200); // Use request code 200 for detail activity
    }

    @Override
    public void onSaveClick(Opportunity opportunity) {
        opportunity.toggleSaved();
        opportunityAdapter.notifyDataSetChanged();
        
        String message = opportunity.isSaved() ? 
            "Opportunity saved! 🔖" : "Opportunity removed from saved";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApplyClick(Opportunity opportunity) {
        // Handle apply action
        opportunity.incrementApplicationCount();
        opportunityAdapter.notifyDataSetChanged();
        
        Toast.makeText(this, "Application submitted! 🎉", Toast.LENGTH_SHORT).show();
        
        // TODO: Open application form or external link
    }

    // FeaturedOpportunityAdapter.OnFeaturedOpportunityClickListener implementation
    @Override
    public void onFeaturedOpportunityClick(Opportunity opportunity) {
        onOpportunityClick(opportunity); // Same as regular opportunity click
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload opportunities when returning to this activity
        loadOpportunitiesFromFirestore();
    }
}
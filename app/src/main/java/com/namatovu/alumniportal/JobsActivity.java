package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        initializeViews();
        setupToolbar();
        setupRecyclerViews();
        setupSearchAndFilters();
        setupFab();
        loadSampleOpportunities(); // TODO: Replace with real data loading
        
        updateEmptyState();
    }

    private void initializeViews() {
        recyclerViewFeatured = findViewById(R.id.recyclerViewFeatured);
        recyclerViewOpportunities = findViewById(R.id.recyclerViewOpportunities);
        etSearch = findViewById(R.id.etSearch);
        spinnerSort = findViewById(R.id.spinnerSort);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        fabAddOpportunity = findViewById(R.id.fabAddOpportunity);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        
        allOpportunities = new ArrayList<>();
        featuredOpportunities = new ArrayList<>();
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
    }

    private void setupSearchAndFilters() {
        // Setup search
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

        // Setup sort spinner
        String[] sortOptions = {"Date", "Deadline", "Relevance", "Company"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = sortOptions[position];
                sortAndFilterOpportunities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup category chips
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "All";
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
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
            filterOpportunities();
        });
    }

    private void setupFab() {
        // TODO: Check user role - only show for mentors/admins
        fabAddOpportunity.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddOpportunityActivity.class);
            startActivityForResult(intent, 100);
        });
        
        // Setup clear filters button
        if (btnClearFilters != null) {
            btnClearFilters.setOnClickListener(v -> clearFilters());
        }
    }
    
    private void clearFilters() {
        etSearch.setText("");
        currentCategory = "All";
        currentSortOption = "Date";
        
        // Reset chip selection
        chipGroupCategories.clearCheck();
        Chip chipAll = findViewById(R.id.chipAll);
        if (chipAll != null) {
            chipAll.setChecked(true);
        }
        
        // Reset spinner
        spinnerSort.setSelection(0);
        
        filterOpportunities();
    }

    private void filterOpportunities() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        
        List<Opportunity> filtered = new ArrayList<>();
        for (Opportunity opportunity : allOpportunities) {
            boolean matchesCategory = currentCategory.equals("All") || opportunity.getCategory().equals(currentCategory);
            boolean matchesSearch = query.isEmpty() || 
                opportunity.getTitle().toLowerCase().contains(query) ||
                opportunity.getCompany().toLowerCase().contains(query) ||
                opportunity.getDescription().toLowerCase().contains(query);
            
            if (matchesCategory && matchesSearch) {
                filtered.add(opportunity);
            }
        }
        
        // Apply sorting
        sortOpportunities(filtered);
        
        opportunityAdapter.updateOpportunities(filtered);
        updateResultsCount(filtered.size());
        updateEmptyState();
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

    // Sample data - TODO: Replace with real data loading
    private void loadSampleOpportunities() {
        // Create sample opportunities
        allOpportunities.clear();
        featuredOpportunities.clear();
        
        // Sample opportunity 1
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 15);
        
        Opportunity opp1 = new Opportunity(
            "Software Engineering Internship",
            "Microsoft",
            "Internship",
            "Join our dynamic team and gain hands-on experience in software development. Work on real projects that impact millions of users worldwide. This internship offers mentorship, learning opportunities, and potential for full-time conversion.",
            cal.getTime()
        );
        opp1.setLocation("Seattle, WA");
        opp1.setSalaryRange("$6,000/month");
        opp1.setFeatured(true);
        opp1.setApplicationsCount(45);
        
        // Sample opportunity 2
        cal.add(Calendar.DAY_OF_MONTH, 20);
        Opportunity opp2 = new Opportunity(
            "Frontend Developer",
            "Google",
            "Job",
            "We're looking for a talented frontend developer to join our innovative team. You'll work on cutting-edge web technologies and create amazing user experiences.",
            cal.getTime()
        );
        opp2.setLocation("Remote");
        opp2.setSalaryRange("$80,000 - $120,000");
        opp2.setApplicationsCount(67);
        
        // Sample opportunity 3
        cal.add(Calendar.DAY_OF_MONTH, 10);
        Opportunity opp3 = new Opportunity(
            "Graduate Management Trainee",
            "Unilever",
            "Graduate Training",
            "Fast-track your career with our comprehensive graduate program. Rotate through different departments and gain exposure to senior leadership.",
            cal.getTime()
        );
        opp3.setLocation("London, UK");
        opp3.setSalaryRange("£35,000");
        opp3.setFeatured(true);
        opp3.setApplicationsCount(123);
        
        // Add more sample opportunities
        allOpportunities.add(opp1);
        allOpportunities.add(opp2);
        allOpportunities.add(opp3);
        
        // Add featured opportunities
        for (Opportunity opp : allOpportunities) {
            if (opp.isFeatured()) {
                featuredOpportunities.add(opp);
            }
        }
        
        // Update adapters
        featuredAdapter.updateOpportunities(featuredOpportunities);
        opportunityAdapter.updateOpportunities(allOpportunities);
        
        updateEmptyState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            // Handle new opportunity added from AddOpportunityActivity
            String title = data.getStringExtra("title");
            String company = data.getStringExtra("company");
            String category = data.getStringExtra("category");
            String description = data.getStringExtra("description");
            String location = data.getStringExtra("location");
            String salaryRange = data.getStringExtra("salary_range");
            String requirements = data.getStringExtra("requirements");
            String applicationInstructions = data.getStringExtra("application_instructions");
            boolean isFeatured = data.getBooleanExtra("is_featured", false);
            long deadlineMillis = data.getLongExtra("deadline", 0);
            
            if (title != null && company != null && category != null && description != null) {
                // Create deadline date
                Date deadline;
                if (deadlineMillis > 0) {
                    deadline = new Date(deadlineMillis);
                } else {
                    // Fallback: 30 days from now
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_MONTH, 30);
                    deadline = cal.getTime();
                }
                
                // Create new opportunity with all data
                Opportunity newOpportunity = new Opportunity(title, company, category, description, deadline);
                newOpportunity.setPostedBy("current_user");
                newOpportunity.setFeatured(isFeatured);
                
                // Set optional fields
                if (location != null && !location.trim().isEmpty()) {
                    newOpportunity.setLocation(location.trim());
                }
                if (salaryRange != null && !salaryRange.trim().isEmpty()) {
                    newOpportunity.setSalaryRange(salaryRange.trim());
                }
                if (requirements != null && !requirements.trim().isEmpty()) {
                    newOpportunity.setRequirements(requirements.trim());
                }
                if (applicationInstructions != null && !applicationInstructions.trim().isEmpty()) {
                    newOpportunity.setApplicationLink(applicationInstructions.trim());
                }
                
                // Add to lists
                allOpportunities.add(0, newOpportunity); // Add to beginning
                
                // If featured, also add to featured list
                if (isFeatured) {
                    featuredOpportunities.add(0, newOpportunity);
                    featuredAdapter.updateOpportunities(featuredOpportunities);
                }
                
                // Refresh the filtered list
                filterOpportunities();
                
                Toast.makeText(this, "Opportunity posted successfully! 🎉", Toast.LENGTH_SHORT).show();
                
                // Scroll to top to show the new opportunity
                recyclerViewOpportunities.scrollToPosition(0);
            }
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
}
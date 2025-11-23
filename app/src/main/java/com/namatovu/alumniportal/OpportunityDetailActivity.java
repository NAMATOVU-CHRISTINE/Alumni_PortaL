package com.namatovu.alumniportal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

/**
 * OpportunityDetailActivity - Display full details of a selected opportunity
 */
public class OpportunityDetailActivity extends AppCompatActivity {

    private TextView tvCompanyLogo, tvCompany, tvDatePosted, tvTitle, tvApplicationsCount;
    private TextView tvLocation, tvSalary, tvDeadline, tvDescription, tvRequirements, tvBenefits;
    private TextView tvApplicationInstructions;
    private Chip chipCategory;
    private MaterialButton btnSave, btnShare, btnApply;
    private MaterialCardView cardRequirements, cardBenefits;

    private String opportunityId;
    private String opportunityTitle;
    private String opportunityCompany;
    private String opportunityCategory;
    private String opportunityDescription;
    private String opportunityLocation;
    private String opportunityDeadline;
    private int opportunityApplications;
    private boolean isSaved = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunity_detail);

        initializeViews();
        setupToolbar();
        loadOpportunityData();
        setupClickListeners();
    }

    private void initializeViews() {
        tvCompanyLogo = findViewById(R.id.tvCompanyLogo);
        tvCompany = findViewById(R.id.tvCompany);
        tvDatePosted = findViewById(R.id.tvDatePosted);
        tvTitle = findViewById(R.id.tvTitle);
        tvApplicationsCount = findViewById(R.id.tvApplicationsCount);
        tvLocation = findViewById(R.id.tvLocation);
        tvSalary = findViewById(R.id.tvSalary);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvDescription = findViewById(R.id.tvDescription);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvBenefits = findViewById(R.id.tvBenefits);
        tvApplicationInstructions = findViewById(R.id.tvApplicationInstructions);
        chipCategory = findViewById(R.id.chipCategory);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);
        btnApply = findViewById(R.id.btnApply);
        cardRequirements = findViewById(R.id.cardRequirements);
        cardBenefits = findViewById(R.id.cardBenefits);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Opportunity Details");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadOpportunityData() {
        Intent intent = getIntent();
        if (intent != null) {
            opportunityId = intent.getStringExtra("opportunity_id");
            opportunityTitle = intent.getStringExtra("opportunity_title");
            opportunityCompany = intent.getStringExtra("opportunity_company");
            opportunityCategory = intent.getStringExtra("opportunity_category");
            opportunityDescription = intent.getStringExtra("opportunity_description");
            opportunityLocation = intent.getStringExtra("opportunity_location");
            opportunityDeadline = intent.getStringExtra("opportunity_deadline");
            opportunityApplications = intent.getIntExtra("opportunity_applications", 0);

            populateViews();
        } else {
            // Handle case where no data is passed
            Toast.makeText(this, "Error loading opportunity details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateViews() {
        // Company logo - use first letter if no logo
        if (opportunityCompany != null && !opportunityCompany.isEmpty()) {
            tvCompanyLogo.setText(opportunityCompany.substring(0, 1).toUpperCase());
            tvCompany.setText(opportunityCompany);
        }

        // Set basic info
        if (opportunityTitle != null) tvTitle.setText(opportunityTitle);
        if (opportunityDescription != null) tvDescription.setText(opportunityDescription);
        
        // Set applications count
        tvApplicationsCount.setText("👥 " + opportunityApplications + " applications");
        
        // Set deadline
        if (opportunityDeadline != null) {
            tvDeadline.setText(opportunityDeadline);
        }

        // Set category chip
        if (opportunityCategory != null) {
            String categoryIcon = getCategoryIcon(opportunityCategory);
            chipCategory.setText(categoryIcon + " " + opportunityCategory);
        }

        // Set location
        if (opportunityLocation != null && !opportunityLocation.isEmpty()) {
            tvLocation.setText(opportunityLocation);
        } else {
            tvLocation.setText("Not specified");
        }

        // Default values for demo - these would come from the Opportunity object in a real implementation
        tvDatePosted.setText("Posted 2 days ago");
        tvSalary.setText("Competitive salary");
        tvRequirements.setText("• Bachelor's degree in relevant field\n• 2+ years of experience\n• Strong communication skills\n• Relevant technical skills");
        tvBenefits.setText("• Health insurance\n• Flexible working hours\n• Professional development\n• Great team environment");
        tvApplicationInstructions.setText("Click 'Apply Now' to submit your application. Make sure your resume is up to date and includes relevant experience.");

        updateSaveButton();
    }

    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "job": return "💼";
            case "internship": return "🎓";
            case "graduate training": return "📚";
            case "apprenticeship": return "🔧";
            default: return "💼";
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> toggleSave());
        btnShare.setOnClickListener(v -> shareOpportunity());
        btnApply.setOnClickListener(v -> applyToOpportunity());
    }

    private void toggleSave() {
        isSaved = !isSaved;
        updateSaveButton();
        
        String message = isSaved ? 
            "Opportunity saved! 🔖" : "Opportunity removed from saved";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // TODO: Persist save state to database/SharedPreferences
    }

    private void updateSaveButton() {
        if (isSaved) {
            btnSave.setText("✅ Saved");
            btnSave.setTextColor(getResources().getColor(R.color.must_green));
        } else {
            btnSave.setText("🔖 Save");
            btnSave.setTextColor(getResources().getColor(R.color.must_green));
        }
    }

    private void shareOpportunity() {
        String shareText = "Check out this opportunity: " + opportunityTitle + 
                          " at " + opportunityCompany + 
                          "\n\nDeadline: " + opportunityDeadline +
                          "\n\nShared via Alumni Portal";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Opportunity: " + opportunityTitle);
        
        startActivity(Intent.createChooser(shareIntent, "Share Opportunity"));
    }

    private void applyToOpportunity() {
        // Increment application count
        opportunityApplications++;
        tvApplicationsCount.setText("👥 " + opportunityApplications + " applications");
        
        // Show success message
        Toast.makeText(this, "Application submitted successfully! 🎉", Toast.LENGTH_LONG).show();
        
        // TODO: In a real app, this would:
        // 1. Open application form or external link
        // 2. Send application data to server
        // 3. Update opportunity in database
        // 4. Send confirmation email
        
        // For demo purposes, you could also open an email intent:
        // openEmailApplication();
    }

    private void openEmailApplication() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"careers@" + opportunityCompany.toLowerCase().replace(" ", "") + ".com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Application for " + opportunityTitle);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Hiring Manager,\n\nI am interested in applying for the " + opportunityTitle + " position at " + opportunityCompany + ".\n\nPlease find my resume attached.\n\nBest regards,\n[Your Name]");
        
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Return updated application count to previous activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("opportunity_id", opportunityId);
        resultIntent.putExtra("updated_applications", opportunityApplications);
        resultIntent.putExtra("is_saved", isSaved);
        setResult(RESULT_OK, resultIntent);
        
        super.onBackPressed();
    }
}

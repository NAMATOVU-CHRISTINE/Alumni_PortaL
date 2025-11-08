package com.namatovu.alumniportal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * AddOpportunityActivity - Form for mentors/admins to post new opportunities
 */
public class AddOpportunityActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etCompany, etLocation, etSalaryRange;
    private TextInputEditText etDescription, etRequirements, etApplicationInstructions;
    private TextInputEditText etDeadline;
    private ChipGroup chipGroupCategory;
    private MaterialCheckBox checkboxFeatured;
    private MaterialButton btnCancel, btnPost;
    
    private Calendar selectedDeadline;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_opportunity);

        initializeViews();
        setupToolbar();
        setupDatePicker();
        setupClickListeners();
        setDefaultCategory();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etCompany = findViewById(R.id.etCompany);
        etLocation = findViewById(R.id.etLocation);
        etSalaryRange = findViewById(R.id.etSalaryRange);
        etDescription = findViewById(R.id.etDescription);
        etRequirements = findViewById(R.id.etRequirements);
        etApplicationInstructions = findViewById(R.id.etApplicationInstructions);
        etDeadline = findViewById(R.id.etDeadline);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        checkboxFeatured = findViewById(R.id.checkboxFeatured);
        btnCancel = findViewById(R.id.btnCancel);
        btnPost = findViewById(R.id.btnPost);
        
        selectedDeadline = Calendar.getInstance();
        selectedDeadline.add(Calendar.DAY_OF_MONTH, 30); // Default 30 days from now
        etDeadline.setText(dateFormat.format(selectedDeadline.getTime()));
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDatePicker() {
        etDeadline.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDeadline.set(Calendar.YEAR, year);
                selectedDeadline.set(Calendar.MONTH, month);
                selectedDeadline.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDeadline.setText(dateFormat.format(selectedDeadline.getTime()));
            },
            selectedDeadline.get(Calendar.YEAR),
            selectedDeadline.get(Calendar.MONTH),
            selectedDeadline.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        btnPost.setOnClickListener(v -> postOpportunity());
    }

    private void setDefaultCategory() {
        // Default to Job category
        Chip chipJob = findViewById(R.id.chipJob);
        chipJob.setChecked(true);
    }

    private void postOpportunity() {
        if (!validateForm()) {
            return;
        }

        // Get form data
        String title = etTitle.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String salaryRange = etSalaryRange.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String requirements = etRequirements.getText().toString().trim();
        String applicationInstructions = etApplicationInstructions.getText().toString().trim();
        String category = getSelectedCategory();
        boolean isFeatured = checkboxFeatured.isChecked();

        // Create result intent with opportunity data
        Intent resultIntent = new Intent();
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("company", company);
        resultIntent.putExtra("location", location);
        resultIntent.putExtra("salary_range", salaryRange);
        resultIntent.putExtra("description", description);
        resultIntent.putExtra("requirements", requirements);
        resultIntent.putExtra("application_instructions", applicationInstructions);
        resultIntent.putExtra("category", category);
        resultIntent.putExtra("is_featured", isFeatured);
        resultIntent.putExtra("deadline", selectedDeadline.getTimeInMillis());

        setResult(Activity.RESULT_OK, resultIntent);
        
        Toast.makeText(this, "Opportunity posted successfully! 🎉", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate required fields
        if (TextUtils.isEmpty(etTitle.getText())) {
            etTitle.setError("Position title is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(etCompany.getText())) {
            etCompany.setError("Company name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(etDescription.getText())) {
            etDescription.setError("Job description is required");
            isValid = false;
        }

        if (chipGroupCategory.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate deadline
        if (selectedDeadline.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Deadline must be in the future", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private String getSelectedCategory() {
        int checkedId = chipGroupCategory.getCheckedChipId();
        
        if (checkedId == R.id.chipJob) {
            return "Job";
        } else if (checkedId == R.id.chipInternship) {
            return "Internship";
        } else if (checkedId == R.id.chipGraduateTraining) {
            return "Graduate Training";
        } else if (checkedId == R.id.chipApprenticeship) {
            return "Apprenticeship";
        }
        
        return "Job"; // Default fallback
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}

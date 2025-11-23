package com.namatovu.alumniportal;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * CustomizePreferencesActivity - Allows users to customize their preferences
 */
public class CustomizePreferencesActivity extends AppCompatActivity {

    private ChipGroup chipGroupInterests, chipGroupJobTypes, chipGroupLocations;
    private SwitchMaterial switchJobAlerts, switchEventNotifications, switchMentorshipNotifications;
    private MaterialButton btnSavePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_preferences);

        initializeViews();
        setupToolbar();
        setupPreferences();
        setupSaveButton();
    }

    private void initializeViews() {
        chipGroupInterests = findViewById(R.id.chipGroupInterests);
        chipGroupJobTypes = findViewById(R.id.chipGroupJobTypes);
        chipGroupLocations = findViewById(R.id.chipGroupLocations);
        switchJobAlerts = findViewById(R.id.switchJobAlerts);
        switchEventNotifications = findViewById(R.id.switchEventNotifications);
        switchMentorshipNotifications = findViewById(R.id.switchMentorshipNotifications);
        btnSavePreferences = findViewById(R.id.btnSavePreferences);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("⚙️ Customize Preferences");
        }
    }

    private void setupPreferences() {
        // Set up interest chips
        String[] interests = {"Technology", "Business", "Healthcare", "Education", "Engineering", "Finance", "Marketing", "Design"};
        for (String interest : interests) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setCheckable(true);
            chipGroupInterests.addView(chip);
        }

        // Set up job type chips
        String[] jobTypes = {"Full-time", "Part-time", "Internship", "Contract", "Remote", "Freelance"};
        for (String jobType : jobTypes) {
            Chip chip = new Chip(this);
            chip.setText(jobType);
            chip.setCheckable(true);
            chipGroupJobTypes.addView(chip);
        }

        // Set up location chips
        String[] locations = {"Kampala", "Entebbe", "Mukono", "Jinja", "Mbarara", "Remote", "International"};
        for (String location : locations) {
            Chip chip = new Chip(this);
            chip.setText(location);
            chip.setCheckable(true);
            chipGroupLocations.addView(chip);
        }

        // Set default notification preferences
        switchJobAlerts.setChecked(true);
        switchEventNotifications.setChecked(true);
        switchMentorshipNotifications.setChecked(true);
    }

    private void setupSaveButton() {
        btnSavePreferences.setOnClickListener(v -> {
            savePreferences();
        });
    }

    private void savePreferences() {
        // TODO: Save preferences to Firebase/SharedPreferences
        
        // Get selected interests
        StringBuilder interests = new StringBuilder();
        for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupInterests.getChildAt(i);
            if (chip.isChecked()) {
                if (interests.length() > 0) interests.append(", ");
                interests.append(chip.getText());
            }
        }

        // Get selected job types
        StringBuilder jobTypes = new StringBuilder();
        for (int i = 0; i < chipGroupJobTypes.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupJobTypes.getChildAt(i);
            if (chip.isChecked()) {
                if (jobTypes.length() > 0) jobTypes.append(", ");
                jobTypes.append(chip.getText());
            }
        }

        // Save notification preferences
        boolean jobAlerts = switchJobAlerts.isChecked();
        boolean eventNotifications = switchEventNotifications.isChecked();
        boolean mentorshipNotifications = switchMentorshipNotifications.isChecked();

        Toast.makeText(this, "Preferences saved successfully! 🎉", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
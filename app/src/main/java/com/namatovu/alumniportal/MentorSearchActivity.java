package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class MentorSearchActivity extends AppCompatActivity {

    private TextInputEditText skillsEditText;
    private TextInputEditText industryEditText;
    private TextInputEditText experienceEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_search);

        skillsEditText = findViewById(R.id.skillsEditText);
        industryEditText = findViewById(R.id.industryEditText);
        experienceEditText = findViewById(R.id.experienceEditText);
        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String skills = skillsEditText.getText().toString();
            String industry = industryEditText.getText().toString();
            String experience = experienceEditText.getText().toString();

            // TODO: Implement actual search logic
            Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        });
    }
}

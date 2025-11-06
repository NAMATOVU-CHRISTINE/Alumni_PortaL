package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Job details activity - shows detailed information about a specific job posting
 */
public class JobDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically for now
        TextView textView = new TextView(this);
        textView.setText("Job Details Activity - Under Development");
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        setContentView(textView);
        
        // Show message to user
        Toast.makeText(this, "Job Details feature coming soon!", Toast.LENGTH_SHORT).show();
        
        // TODO: Implement proper job details layout and functionality
        // - Load job details from intent extras
        // - Display job information (title, company, description, etc.)
        // - Add apply button functionality
        // - Add save/bookmark functionality
    }
}
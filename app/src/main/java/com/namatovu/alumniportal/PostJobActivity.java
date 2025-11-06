package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Post job activity - allows users to create and post new job opportunities
 */
public class PostJobActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically for now
        TextView textView = new TextView(this);
        textView.setText("Post Job Activity - Under Development");
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        setContentView(textView);
        
        // Show message to user
        Toast.makeText(this, "Post Job feature coming soon!", Toast.LENGTH_SHORT).show();
        
        // TODO: Implement proper job posting form
        // - Add form fields (title, company, description, requirements, etc.)
        // - Add validation
        // - Add image upload for company logo
        // - Add posting to Firebase
        // - Add draft save functionality
    }
}
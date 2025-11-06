package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Create article activity - allows users to create and publish new articles
 */
public class CreateArticleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically for now
        TextView textView = new TextView(this);
        textView.setText("Create Article Activity - Under Development");
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        setContentView(textView);
        
        // Show message to user
        Toast.makeText(this, "Create Article feature coming soon!", Toast.LENGTH_SHORT).show();
        
        // TODO: Implement proper article creation form
        // - Add rich text editor
        // - Add image/media upload functionality
        // - Add category selection
        // - Add tags functionality
        // - Add draft save functionality
        // - Add publish/preview options
    }
}
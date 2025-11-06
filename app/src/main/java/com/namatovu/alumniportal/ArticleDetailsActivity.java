package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Article details activity - shows detailed information about a specific news article
 */
public class ArticleDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically for now
        TextView textView = new TextView(this);
        textView.setText("Article Details Activity - Under Development");
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        setContentView(textView);
        
        // Show message to user
        Toast.makeText(this, "Article Details feature coming soon!", Toast.LENGTH_SHORT).show();
        
        // TODO: Implement proper article details layout
        // - Load article details from intent extras
        // - Display article content (title, author, content, images, etc.)
        // - Add comment functionality
        // - Add sharing functionality
        // - Add like/reaction functionality
    }
}
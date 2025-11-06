package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * View profile activity - displays detailed profile information for a user
 */
public class ViewProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically for now
        TextView textView = new TextView(this);
        textView.setText("View Profile Activity - Under Development");
        textView.setTextSize(18);
        textView.setPadding(32, 32, 32, 32);
        setContentView(textView);
        
        // Show message to user
        Toast.makeText(this, "View Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        
        // TODO: Implement proper profile view layout
        // - Load user profile from intent extras
        // - Display user information (photo, name, bio, experience, etc.)
        // - Add connect/message functionality
        // - Add mentor request functionality
        // - Add privacy controls
    }
}
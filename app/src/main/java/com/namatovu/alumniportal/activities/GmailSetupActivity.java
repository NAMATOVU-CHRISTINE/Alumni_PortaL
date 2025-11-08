package com.namatovu.alumniportal.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.namatovu.alumniportal.EmailService;
import com.namatovu.alumniportal.R;

/**
 * Activity to setup Gmail API authentication for email notifications
 */
public class GmailSetupActivity extends AppCompatActivity {
    private static final String TAG = "GmailSetupActivity";
    
    private EmailService emailService;
    private TextView statusText;
    private Button setupButton;
    private EditText authCodeInput;
    private Button completeButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_setup);
        
        emailService = new EmailService(this);
        
        initializeViews();
        updateStatus();
    }
    
    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        setupButton = findViewById(R.id.setupButton);
        authCodeInput = findViewById(R.id.authCodeInput);
        completeButton = findViewById(R.id.completeButton);
        
        setupButton.setOnClickListener(v -> startAuthentication());
        completeButton.setOnClickListener(v -> completeAuthentication());
    }
    
    private void updateStatus() {
        if (emailService.isGmailAuthenticated()) {
            statusText.setText("✅ Gmail API is configured and ready to send emails");
            setupButton.setText("Re-authenticate");
            authCodeInput.setVisibility(android.view.View.GONE);
            completeButton.setVisibility(android.view.View.GONE);
        } else {
            statusText.setText("❌ Gmail API authentication required for email notifications");
            setupButton.setText("Start Setup");
            authCodeInput.setVisibility(android.view.View.VISIBLE);
            completeButton.setVisibility(android.view.View.VISIBLE);
        }
    }
    
    private void startAuthentication() {
        setupButton.setEnabled(false);
        
        emailService.getGmailAuthUrl()
                .addOnSuccessListener(authUrl -> {
                    // Open browser with auth URL
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                    startActivity(intent);
                    
                    Toast.makeText(this, "Please authorize the app and copy the authorization code", Toast.LENGTH_LONG).show();
                    setupButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get auth URL", e);
                    Toast.makeText(this, "Failed to start authentication: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setupButton.setEnabled(true);
                });
    }
    
    private void completeAuthentication() {
        String authCode = authCodeInput.getText().toString().trim();
        if (authCode.isEmpty()) {
            Toast.makeText(this, "Please enter the authorization code", Toast.LENGTH_SHORT).show();
            return;
        }
        
        completeButton.setEnabled(false);
        
        emailService.completeGmailAuth(authCode)
                .addOnSuccessListener(success -> {
                    Toast.makeText(this, "Gmail API authentication completed successfully!", Toast.LENGTH_LONG).show();
                    updateStatus();
                    completeButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to complete authentication", e);
                    Toast.makeText(this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    completeButton.setEnabled(true);
                });
    }
}
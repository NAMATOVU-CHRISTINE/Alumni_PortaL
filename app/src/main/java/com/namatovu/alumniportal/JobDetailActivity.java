package com.namatovu.alumniportal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Job detail screen — loads a Job by ID from Firestore and displays it.
 */
public class JobDetailActivity extends AppCompatActivity {

    public static final String EXTRA_JOB_ID = "extra_job_id";

    private TextView jobTitleText;
    private TextView companyNameText;
    private TextView locationText;
    private TextView descriptionText;
    private Button applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        jobTitleText = findViewById(R.id.jobTitleText);
        companyNameText = findViewById(R.id.companyNameText);
        locationText = findViewById(R.id.locationText);
        descriptionText = findViewById(R.id.descriptionText);
        applyButton = findViewById(R.id.applyButton);

        String jobId = getIntent().getStringExtra(EXTRA_JOB_ID);
        if (jobId == null) {
            Toast.makeText(this, "Job not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("job_opportunities").document(jobId).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc.exists()) {
                        String title = doc.getString("title");
                        String company = doc.getString("company");
                        String location = doc.getString("location");
                        String description = doc.getString("description");
                        String applicationLink = doc.getString("applicationInstructions");
                        
                        if (title != null && company != null && location != null && description != null) {
                            jobTitleText.setText(title);
                            companyNameText.setText(company);
                            locationText.setText(location);
                            descriptionText.setText(description);
                            
                            if (applicationLink != null && !applicationLink.isEmpty()) {
                                applyButton.setOnClickListener(v -> {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(applicationLink));
                                    startActivity(i);
                                });
                            } else {
                                applyButton.setEnabled(false);
                            }
                        } else {
                            Toast.makeText(this, "Job data incomplete.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Job not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load job.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}

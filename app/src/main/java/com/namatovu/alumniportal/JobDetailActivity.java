package com.namatovu.alumniportal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Minimal JobDetailActivity stub to allow compilation. Replace with full implementation as needed.
 */
public class JobDetailActivity extends AppCompatActivity {

    public static final String EXTRA_JOB_ID = "extra_job_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If there's a layout, set it. Otherwise this is a no-op placeholder.
        try {
            setContentView(R.layout.activity_job_detail);
        } catch (Exception ignored) {
            // Layout might not exist yet; keep activity functional for compilation.
        }
    }
}

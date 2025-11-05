package com.namatovu.alumniportal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Minimal ForgotPasswordActivity stub to allow compilation. Replace with full implementation as needed.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If there's a layout, set it. Otherwise this is a no-op placeholder.
        try {
            setContentView(R.layout.activity_forgot_password);
        } catch (Exception ignored) {
            // Layout might not exist yet; keep activity functional for compilation.
        }
    }
}

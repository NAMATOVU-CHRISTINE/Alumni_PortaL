package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.databinding.ActivityProfileBinding;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;
import com.namatovu.alumniportal.models.User;

import java.util.List;

/**
 * Profile view activity — shows profile and navigates to edit screen.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup edit profile launcher to refresh on return
        editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Profile was updated, refresh immediately
                    Toast.makeText(this, "Refreshing profile...", Toast.LENGTH_SHORT).show();
                    loadProfile();
                }
            }
        );

        setSupportActionBar(binding.toolbar);
        
        // Setup navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup button click listeners
        binding.editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });
        
        binding.logoutButton.setOnClickListener(v -> {
            logoutUser();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Show loading state
        showLoading(true);
        
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            // Hide loading state
            showLoading(false);
            
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    try {
                        User u = doc.toObject(User.class);
                        if (u != null) updateUIWithUserData(u);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to deserialize user", e);
                        Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to load user profile", task.getException());
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (show) {
            // Disable buttons during loading
            binding.editProfileButton.setEnabled(false);
            binding.editProfileButton.setText("Loading...");
        } else {
            // Re-enable buttons
            binding.editProfileButton.setEnabled(true);
            binding.editProfileButton.setText("Edit Profile");
        }
    }

    private void updateUIWithUserData(User user) {
        binding.nameText.setText(user.getFullName());
        binding.emailText.setText(user.getEmail());
        binding.bioText.setText(user.getBio());
        binding.careerText.setText(user.getCurrentJob());

        // profile image
        String url = user.getProfileImageUrl();
        Log.d(TAG, "Loading profile image URL: " + url);
        if (url != null && !url.isEmpty()) {
            // Skip cache to ensure we get the latest image
            Glide.with(this)
                .load(url)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.profileImage);
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_person);
        }

        // skills
        binding.skillsChipGroup.removeAllViews();
        List<String> skills = user.getSkills();
        for (String s : skills) {
            Chip c = new Chip(this);
            c.setText(s);
            c.setClickable(false);
            c.setCheckable(false);
            // Apply custom styling
            c.setChipBackgroundColorResource(R.color.light_gray);
            c.setTextColor(getColor(R.color.black));
            c.setChipStrokeColorResource(R.color.must_green);
            c.setChipStrokeWidth(2.0f);
            binding.skillsChipGroup.addView(c);
        }
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            binding.nameText.setText("Loading...");
        }
    }
    
    private void logoutUser() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes", (dialog, which) -> {
                mAuth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}

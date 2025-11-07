package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.namatovu.alumniportal.databinding.ActivityHomeBinding;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;
import com.namatovu.alumniportal.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupCardClickListeners();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return; 
        }

        loadUserProfile(currentUser.getUid());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // For now, open ProfileActivity as settings page until SettingsActivity is created
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupCardClickListeners() {
        // Profile card - navigate to profile activity
        binding.profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        
        // Mentor card - navigate to mentorship system
        binding.mentorCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MentorshipActivity.class);
            startActivity(intent);
        });
        
        // Career Tips card - navigate to knowledge hub
        binding.careerTipsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewsFeedActivity.class);
            startActivity(intent);
        });
        
        // Jobs card - navigate to job board
        binding.jobsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobBoardActivity.class);
            startActivity(intent);
        });
        
        // Events card - navigate to events calendar
        binding.eventsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventsActivity.class);
            startActivity(intent);
        });
        
        // Knowledge Hub card - navigate to news feed
        binding.knowledgeHubCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewsFeedActivity.class);
            startActivity(intent);
        });
        
        // Recommendations card - navigate to job board
        binding.recommendationsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobBoardActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile(String userId) {
        setLoadingState(true);
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            setLoadingState(false);
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // --- THE DEFINITIVE CRASH FIX ---
                    // The toObject() call can throw a RuntimeException if the data in Firestore
                    // does not perfectly match the User.java class (e.g., a String in the DB where
                    // a List is expected in the code). This try-catch block prevents that crash.
                    try {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            updateUiWithUser(user);
                        }
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to deserialize User object for UID: " + userId, e);
                        Toast.makeText(HomeActivity.this, "Error: Could not read user profile data.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "User document not found.");
                    Toast.makeText(this, "Could not load profile.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error getting user document", task.getException());
                Toast.makeText(this, "Error loading profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUiWithUser(User user) {
        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            binding.welcomeText.setText(getString(R.string.welcome_message, user.getFullName()));
        } else {
            binding.welcomeText.setText(getString(R.string.welcome_default));
        }

        // Use ImageLoadingHelper for better image loading
        ImageLoadingHelper.loadProfileImage(
            this,
            user.getProfileImageUrl(),
            binding.homeProfileImage
        );

        updateProfileCompletion(user);
    }

    private void updateProfileCompletion(User user) {
        // Profile completion card has been removed from the new design
        // This method is kept for potential future use
    }

    private void setLoadingState(boolean isLoading) {
        if(isLoading) {
            binding.welcomeText.setText("Loading...");
            binding.homeSlogan.setVisibility(View.INVISIBLE);
        } else {
            binding.homeSlogan.setVisibility(View.VISIBLE);
        }
    }
}

package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
    
    // Motivational tips rotation
    private Handler motivationHandler;
    private Runnable motivationRunnable;
    private int currentTipIndex = 0;
    private String[] motivationalTips = {
        "Keep connecting, keep growing! 🌱",
        "Your network is your net worth 💎",
        "Every connection is a new opportunity 🚀",
        "Success is a journey, not a destination ⭐",
        "Learn from those who've walked your path 🎯",
        "Great things never come from comfort zones 💪",
        "The alumni network is your secret weapon 🔥",
        "Today's networking is tomorrow's opportunity 🌟"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupCardClickListeners();
        setupMotivationalTipsRotation();
        setupSettingsClickListener();

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

    private void setupSettingsClickListener() {
        binding.settingsIcon.setOnClickListener(v -> {
            // Open App Settings (placeholder for now - opens ProfileActivity)
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupMotivationalTipsRotation() {
        motivationHandler = new Handler(Looper.getMainLooper());
        
        motivationRunnable = new Runnable() {
            @Override
            public void run() {
                rotateMotivationalTip();
                motivationHandler.postDelayed(this, 6000); // 6 seconds interval
            }
        };
        
        // Start the rotation after 3 seconds initial delay
        motivationHandler.postDelayed(motivationRunnable, 10000);
    }

    private void rotateMotivationalTip() {
        // Fade out animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Update text
                currentTipIndex = (currentTipIndex + 1) % motivationalTips.length;
                binding.rotatingMotivationTip.setText(motivationalTips[currentTipIndex]);
                
                // Fade in animation
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(300);
                binding.rotatingMotivationTip.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        binding.rotatingMotivationTip.startAnimation(fadeOut);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume motivational tips rotation
        if (motivationHandler != null && motivationRunnable != null) {
            motivationHandler.postDelayed(motivationRunnable, 6000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop motivational tips rotation to save battery
        if (motivationHandler != null) {
            motivationHandler.removeCallbacks(motivationRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler
        if (motivationHandler != null) {
            motivationHandler.removeCallbacks(motivationRunnable);
        }
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

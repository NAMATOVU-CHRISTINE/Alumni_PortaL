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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.namatovu.alumniportal.databinding.ActivityHomeBinding;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.models.Recommendation;
import com.namatovu.alumniportal.models.RecentActivity;
import com.namatovu.alumniportal.adapters.RecommendationsAdapter;
import com.namatovu.alumniportal.adapters.HomeRecentActivitiesAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    // Adapters for dynamic content
    private RecommendationsAdapter recommendationsAdapter;
    private HomeRecentActivitiesAdapter recentActivitiesAdapter;
    
    // Motivational tips rotation
    private Handler motivationHandler;
    private Runnable motivationRunnable;
    private int currentTipIndex = 0;
    private String[] motivationalTips = {
       "Keep connecting, keep growing!",
    "Your network is your net worth",
"Every connection is a new opportunity",
"Success is a journey, not a destination",
"Learn from those who've walked your path",
"Great things never come from comfort zones",
"The alumni network is your secret weapon",
"Today's networking is tomorrow's opportunity",
"Small steps lead to big dreams ",
"Stay curious, stay connected",
"One chat can change your career",
"Share knowledge, inspire growth",
"Keep learning, always evolving",
"Dream big, connect wider",
"Inspiration begins with connection",
"Build bridges, not walls",
"A mentor today, a leader tomorrow",
"Stay humble, stay hungry",
"Your journey inspires others",
"Progress, not perfection",
"Collaboration creates innovation",
"Be the reason someone grows",
"Keep your network alive",
"One step closer to greatness",
"Inspire. Connect. Lead.",
"The best investment is in yourself",
"Be open to new beginnings",
"Lift others as you climb",
"The future is built through collaboration",
"Connect today for tomorrow’s success",
"Growth starts with hello",
"Every mentor was once a learner",
"Confidence grows through connection",
"The more you give, the more you grow",
"Empower others, empower yourself",
"Learn. Lead. Leave a legacy",
"Build your story, one connection at a time",
"Keep pushing, keep believing",
"Success loves preparation",
"Create impact, not noise",
"Connect, collaborate, celebrate!",
"Your story matters — share it!",
"Every day is a chance to grow",
"Lead with purpose, not position",
"Be bold enough to begin",
"Dream. Dare. Do.",
"Keep showing up — consistency wins",
"Your growth inspires generations",
"The world needs your ideas",
"Push boundaries, break limits",
"Kindness is powerful",
"One message can open doors",
"Be proud of how far you’ve come ",
"The journey is just beginning",
"Network with intention",
"Shine where you are",
"Your passion is your power",
"Make learning your lifestyle",
"Opportunities follow preparation",
"Your future self will thank you",
"You are building a legacy",
"Stay inspired, stay connected",
"Every success starts with a small step",
"Turn ideas into action",
"Be the spark that lights others",
"Grow through what you go through",
"Connection creates possibility �",
"Be fearless in pursuit of growth",
"Learn something new today",
"You belong here",
"Your knowledge can change lives",
"Every mentor was once a student",
"Take initiative, make impact",
"Share your story, inspire hope",
"Stay motivated, stay connected",
"Lead by example, inspire by action",
"Keep exploring new horizons",
"Your voice matters — use it",
"Create value wherever you go",
"Learning never stops",
"Mentorship builds bridges",
"Collaboration sparks innovation",
"Help others rise and you rise too 🧗‍♂️",
"Believe in your potential ",
"Stay persistent, stay strong",
"Network intentionally, grow exponentially",
"Make every connection count",
"Your ideas can spark change",
"Keep challenging yourself",
"Every step forward is progress",
"Knowledge shared is power multiplied",
"Be open, be kind, be bold",
"Learn from failures, celebrate successes",
"Small actions lead to big results",
"Mentors shape futures",
"Build meaningful relationships",
"Consistency beats intensity",
"Be adaptable, stay relevant",
"Your effort inspires others",
"Turn challenges into opportunities",
"Invest in growth daily",
"Be a lighthouse for others",
"Your journey shapes the community",
"Celebrate every achievement",
"Lead with empathy, act with purpose",
"Stay curious, never settle ",
"Your connections are your strength",
"Be the change you seek",
"Every connection is a seed for growth",
"Give, mentor, and inspire",
"Stay focused, stay passionate",
"Your journey inspires generations",
"Every action creates impact",
"Learning is a lifelong adventure",
"Your story can motivate others",
"Keep networking, keep thriving",
"Be a connector, not just a participant",
"Success is better when shared",
"Lead with integrity, grow with humility",
"Mentorship is a gift, both given and received",
"Create opportunities, don’t wait for them",
"Every day is a new chance",
"Your legacy starts with connection",
"Stay inspired, keep inspiring",
"Share your wisdom, light the path",
"Build bridges, not walls",
"Opportunities multiply through connection",
"Believe, act, achieve ",
"Keep growing, keep giving",
"Your network is your power"

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupRecyclerViews();
        setupCardClickListeners();
        setupSeeAllClickListeners();
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
        
        // Setup navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // Setup navigation view
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item.getItemId());
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        
        // Update nav header with user info
        updateNavHeader();
    }
    
    private void handleNavigationItemSelected(int itemId) {
        Intent intent = null;
        
        if (itemId == R.id.nav_home) {
            // Already on home
            return;
        } else if (itemId == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        } else if (itemId == R.id.nav_directory) {
            intent = new Intent(this, AlumniDirectoryActivity.class);
        } else if (itemId == R.id.nav_almater_directory) {
            intent = new Intent(this, AlumniDirectoryActivity.class);
        } else if (itemId == R.id.nav_mentorship) {
            intent = new Intent(this, MentorshipActivity.class);
        } else if (itemId == R.id.nav_jobs) {
            intent = new Intent(this, JobsActivity.class);
        } else if (itemId == R.id.nav_career_tips) {
            intent = new Intent(this, CareerTipsActivity.class);
        } else if (itemId == R.id.nav_knowledge) {
            intent = new Intent(this, KnowledgeActivity.class);
        } else if (itemId == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
            return;
        }
        
        if (intent != null) {
            startActivity(intent);
        }
    }
    
    private void updateNavHeader() {
        View headerView = binding.navigationView.getHeaderView(0);
        if (headerView != null && mAuth.getCurrentUser() != null) {
            android.widget.TextView navHeaderName = headerView.findViewById(R.id.navHeaderName);
            android.widget.TextView navHeaderEmail = headerView.findViewById(R.id.navHeaderEmail);
            android.widget.ImageView navHeaderProfileImage = headerView.findViewById(R.id.navHeaderProfileImage);
            
            if (navHeaderEmail != null) {
                navHeaderEmail.setText(mAuth.getCurrentUser().getEmail());
            }
            
            // Load user data
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && navHeaderName != null) {
                            String fullName = doc.getString("fullName");
                            navHeaderName.setText(fullName != null ? fullName : "Alumni User");
                            
                            String profileImageUrl = doc.getString("profileImageUrl");
                            if (profileImageUrl != null && navHeaderProfileImage != null) {
                                ImageLoadingHelper.loadProfileImage(this, profileImageUrl, navHeaderProfileImage);
                            }
                            
                            // Update last active time
                            updateUserLastActive();
                        }
                    });
        }
    }
    
    private void handleLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    private void setupRecyclerViews() {
        // Setup recommendations RecyclerView
        if (binding.recommendationsRecyclerView != null) {
            binding.recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recommendationsAdapter = new RecommendationsAdapter(this, new ArrayList<>());
            binding.recommendationsRecyclerView.setAdapter(recommendationsAdapter);
        }
        
        // Setup recent activities RecyclerView
        if (binding.recentActivitiesRecyclerView != null) {
            binding.recentActivitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recentActivitiesAdapter = new HomeRecentActivitiesAdapter(this, new ArrayList<>());
            binding.recentActivitiesRecyclerView.setAdapter(recentActivitiesAdapter);
        }
    }

    private void setupSettingsClickListener() {
        binding.settingsIcon.setOnClickListener(v -> {
            // Open SettingsActivity
            Intent intent = new Intent(this, SettingsActivity.class);
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
        motivationHandler.postDelayed(motivationRunnable, 3000);
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
        // Update last active time when user returns to the app
        updateUserLastActive();
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
        if (item.getItemId() == R.id.action_events_news) {
            // Open Events & News Activity
            Intent intent = new Intent(this, EventsNewsActivity.class);
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

    private void setupSeeAllClickListeners() {
        // Removed redundant "See All" buttons - users can access features directly from cards
    }

    private void setupCardClickListeners() {
        // Alumni Directory card
        binding.alumniDirectoryCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlumniDirectoryActivity.class);
            startActivity(intent);
        });
        
        // Recommendations card - navigate to jobs & opportunities
        binding.recommendationsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobsActivity.class);
            startActivity(intent);
        });
        
        // Jobs card in recommendations section
        if (binding.jobsCard != null) {
            binding.jobsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, JobsActivity.class);
                startActivity(intent);
            });
        }
        
        // Setup new enhanced functionality
        setupEnhancedClickListeners();
    }
    
    private void setupEnhancedClickListeners() {
        // View more activities button in the card
        if (binding.btnViewMoreActivities != null) {
            binding.btnViewMoreActivities.setOnClickListener(v -> {
                Intent intent = new Intent(this, RecentActivitiesActivity.class);
                startActivity(intent);
            });
        }
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

        // Load stats
        loadUserStats();
        
        // Start background services for demo (exam requirement)
        startBackgroundServices();

        // Load dynamic recommendations
        loadDynamicRecommendations(user);
        
        // Load dynamic recent activities
        loadDynamicRecentActivities();

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
    
    private void loadDynamicRecommendations(User user) {
        try {
            // Show empty state initially - will be populated when user starts using the app
            List<Recommendation> recommendations = new ArrayList<>();
            updateRecommendationsUI(recommendations);
        } catch (Exception e) {
            Log.e(TAG, "Error loading recommendations", e);
            // Show empty state
            showEmptyRecommendations();
        }
    }
    
    private void loadDynamicRecentActivities() {
        try {
            // Show empty state initially - will be populated with real user activities
            List<RecentActivity> activities = new ArrayList<>();
            updateRecentActivitiesUI(activities);
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent activities", e);
            // Show empty state
            showEmptyRecentActivities();
        }
    }
    
    private void updateRecommendationsUI(List<Recommendation> recommendations) {
        if (recommendationsAdapter != null && recommendations != null) {
            recommendationsAdapter.updateRecommendations(recommendations);
            
            // Update badge dynamically
            if (binding.recommendationsBadge != null) {
                if (recommendations.size() > 0) {
                    binding.recommendationsBadge.setText(recommendations.size() + " new");
                    binding.recommendationsBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.recommendationsBadge.setVisibility(View.GONE);
                }
            }
            
            // Update description dynamically
            if (binding.recommendationsDescription != null) {
                if (recommendations.size() > 0) {
                    binding.recommendationsDescription.setText("Based on your profile and interests, we found " + recommendations.size() + " exciting opportunities that match your skills.");
                    binding.recommendationsDescription.setVisibility(View.VISIBLE);
                } else {
                    binding.recommendationsDescription.setVisibility(View.GONE);
                }
            }
            
            // Show/hide empty state
            if (binding.recommendationsEmptyState != null) {
                binding.recommendationsEmptyState.setVisibility(
                    recommendations.isEmpty() ? View.VISIBLE : View.GONE
                );
            }
        }
        Log.d(TAG, "Updated UI with " + (recommendations != null ? recommendations.size() : 0) + " recommendations");
    }
    
    private void updateRecentActivitiesUI(List<RecentActivity> activities) {
        if (recentActivitiesAdapter != null && activities != null) {
            recentActivitiesAdapter.updateActivities(activities);
            
            // Hide the card if no activities
            if (binding.recentActivityCard != null) {
                binding.recentActivityCard.setVisibility(
                    activities.isEmpty() ? View.GONE : View.VISIBLE
                );
            }
        }
        Log.d(TAG, "Updated UI with " + (activities != null ? activities.size() : 0) + " activities");
    }
    
    private void showEmptyRecommendations() {
        if (binding.recommendationsCard != null) {
            binding.recommendationsCard.setVisibility(View.VISIBLE);
            // Show empty state
            if (binding.recommendationsEmptyState != null) {
                binding.recommendationsEmptyState.setVisibility(View.VISIBLE);
            }
            if (binding.recommendationsContent != null) {
                binding.recommendationsContent.setVisibility(View.GONE);
            }
        }
    }
    
    private void showEmptyRecentActivities() {
        if (binding.recentActivityCard != null) {
            binding.recentActivityCard.setVisibility(View.VISIBLE);
            // Show message that activities will appear when users start interacting
            if (binding.recentActivitiesRecyclerView != null) {
                binding.recentActivitiesRecyclerView.setVisibility(View.GONE);
            }
        }
    }
    
    private void loadUserStats() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        // Load connections count
        db.collection("mentorships")
            .where(com.google.firebase.firestore.Filter.or(
                com.google.firebase.firestore.Filter.equalTo("mentorId", userId),
                com.google.firebase.firestore.Filter.equalTo("menteeId", userId)
            ))
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener(docs -> {
                binding.tvConnectionsCount.setText(String.valueOf(docs.size()));
            });
        
        // Profile views would come from analytics - for now show 0
        binding.tvProfileViewsCount.setText("0");
    }
    
    private void startBackgroundServices() {
        // Start data sync service (demonstrates Service component)
        Intent syncIntent = new Intent(this, com.namatovu.alumniportal.services.DataSyncBackgroundService.class);
        startService(syncIntent);
        
        // Start web scraping service (demonstrates advanced feature)
        Intent scrapingIntent = new Intent(this, com.namatovu.alumniportal.services.WebScrapingService.class);
        startService(scrapingIntent);
        
        Log.d(TAG, "Background services started for exam demonstration");
    }
    
    /**
     * Update the current user's last active timestamp
     */
    private void updateUserLastActive() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        long currentTime = System.currentTimeMillis();
        
        // Update last active time in Firestore
        db.collection("users").document(userId)
            .update("lastActive", currentTime)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Last active time updated for user: " + userId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update last active time", e);
            });
    }
}
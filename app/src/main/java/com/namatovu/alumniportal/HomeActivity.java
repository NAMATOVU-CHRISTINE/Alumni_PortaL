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

import com.namatovu.alumniportal.databinding.ActivityHomeBinding;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;
import com.namatovu.alumniportal.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
"Today's networking is tomorrow's opportunity 🌟",
"Small steps lead to big dreams ",
"Stay curious, stay connected 💡",
"One chat can change your career 💬",
"Share knowledge, inspire growth 🌿",
"Keep learning, always evolving 📚",
"Dream big, connect wider 🌍",
"Inspiration begins with connection 💖",
"Build bridges, not walls 🌉",
"A mentor today, a leader tomorrow 👑",
"Stay humble, stay hungry 🙌",
"Your journey inspires others ✨",
"Progress, not perfection 🌻",
"Collaboration creates innovation ⚡",
"Be the reason someone grows 🌼",
"Keep your network alive 🔗",
"One step closer to greatness 🚶‍♀️",
"Inspire. Connect. Lead. 💫",
"The best investment is in yourself 💎",
"Be open to new beginnings 🌸",
"Lift others as you climb 🧗‍♀️",
"The future is built through collaboration 🤝",
"Connect today for tomorrow’s success 🕊️",
"Growth starts with hello 👋",
"Every mentor was once a learner 🪴",
"Confidence grows through connection 🌞",
"The more you give, the more you grow 🎁",
"Empower others, empower yourself 💪",
"Learn. Lead. Leave a legacy 🕯️",
"Build your story, one connection at a time 📖",
"Keep pushing, keep believing 🔥",
"Success loves preparation 🎯",
"Create impact, not noise 💥",
"Connect, collaborate, celebrate! 🎉",
"Your story matters — share it! 🗣️",
"Every day is a chance to grow 🌞",
"Lead with purpose, not position 💫",
"Be bold enough to begin 🚀",
"Dream. Dare. Do. 🌟",
"Keep showing up — consistency wins 🕒",
"Your growth inspires generations 🌿",
"The world needs your ideas 🌍",
"Push boundaries, break limits 💪",
"Kindness is powerful 🤍",
"One message can open doors ✉️",
"Be proud of how far you’ve come 🌈",
"The journey is just beginning 🌄",
"Network with intention 🤝",
"Shine where you are ✨",
"Your passion is your power 🔥",
"Make learning your lifestyle 📘",
"Opportunities follow preparation 🎯",
"Your future self will thank you 🙏",
"You are building a legacy 🕊️",
"Stay inspired, stay connected 💬",
"Every success starts with a small step 👣",
"Turn ideas into action ⚙️",
"Be the spark that lights others 🔥",
"Grow through what you go through 🌻",
"Connection creates possibility 🌐",
"Be fearless in pursuit of growth 🦋",
"Learn something new today 🧠",
"You belong here 💖",
"Your knowledge can change lives 🌟",
"Every mentor was once a student 🪴",
"Take initiative, make impact 🚀",
"Share your story, inspire hope 💌",
"Stay motivated, stay connected 💫",
"Lead by example, inspire by action 🌞",
"Keep exploring new horizons 🌄",
"Your voice matters — use it 🎤",
"Create value wherever you go 💎",
"Learning never stops 📚",
"Mentorship builds bridges 🌉",
"Collaboration sparks innovation ⚡",
"Help others rise and you rise too 🧗‍♂️",
"Believe in your potential 🌈",
"Stay persistent, stay strong 💪",
"Network intentionally, grow exponentially 🌐",
"Make every connection count 🔗",
"Your ideas can spark change 🔥",
"Keep challenging yourself 💫",
"Every step forward is progress 👣",
"Knowledge shared is power multiplied 📘",
"Be open, be kind, be bold 🌸",
"Learn from failures, celebrate successes 🎉",
"Small actions lead to big results 🌿",
"Mentors shape futures 🌟",
"Build meaningful relationships 🤝",
"Consistency beats intensity 🕒",
"Be adaptable, stay relevant 🌍",
"Your effort inspires others 💡",
"Turn challenges into opportunities ⚡",
"Invest in growth daily 🪴",
"Be a lighthouse for others 🌞",
"Your journey shapes the community 🌻",
"Celebrate every achievement ✨",
"Lead with empathy, act with purpose 💖",
"Stay curious, never settle 🌈",
"Your connections are your strength 💎",
"Be the change you seek 🌍",
"Every connection is a seed for growth 🌱",
"Give, mentor, and inspire 🎯",
"Stay focused, stay passionate 🔥",
"Your journey inspires generations 🕊️",
"Every action creates impact 💫",
"Learning is a lifelong adventure 📚",
"Your story can motivate others 🗣️",
"Keep networking, keep thriving 🚀",
"Be a connector, not just a participant 🤝",
"Success is better when shared 💎",
"Lead with integrity, grow with humility 🌸",
"Mentorship is a gift, both given and received 🎁",
"Create opportunities, don’t wait for them ⚡",
"Every day is a new chance 🌞",
"Your legacy starts with connection 🕯️",
"Stay inspired, keep inspiring 💫",
"Share your wisdom, light the path 🌟",
"Build bridges, not walls 🌉",
"Opportunities multiply through connection 🔗",
"Believe, act, achieve 🌈",
"Keep growing, keep giving 🌿",
"Your network is your power 💎"

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
            // Open SettingsActivity
            Intent intent = new Intent(this, SettingsActivity.class);
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
        // See All for Grow section - shows all growth opportunities
        binding.btnSeeAllGrow.setOnClickListener(v -> {
            Intent intent = new Intent(this, GrowSectionActivity.class);
            startActivity(intent);
        });
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
        
        // Career Tips card - navigate to career tips activity
        binding.careerTipsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CareerTipsActivity.class);
            startActivity(intent);
        });
        
        // Jobs card - navigate to comprehensive jobs & opportunities system
        binding.jobsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobsActivity.class);
            startActivity(intent);
        });
        
        // Events card - navigate to events calendar
        binding.eventsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventsActivity.class);
            startActivity(intent);
        });
        
        // Knowledge Hub card - navigate to knowledge section
        binding.knowledgeHubCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, KnowledgeActivity.class);
            startActivity(intent);
        });
        
        // Jobs & Opportunities card - navigate to comprehensive jobs & opportunities system
        binding.jobsOpportunitiesCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobsActivity.class);
            startActivity(intent);
        });
        
        // Recommendations card - navigate to comprehensive jobs & opportunities system
        binding.recommendationsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobsActivity.class);
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
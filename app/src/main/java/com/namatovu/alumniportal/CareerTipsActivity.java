package com.namatovu.alumniportal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.namatovu.alumniportal.databinding.ActivityCareerTipsBinding;
import com.namatovu.alumniportal.models.CareerTip;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Career Tips Activity - Interactive career guidance for alumni
 * Features: Auto-rotation, swipe gestures, save/share functionality, category filtering
 */
public class CareerTipsActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String TAG = "CareerTipsActivity";
    private static final String PREFS_NAME = "CareerTipsPrefs";
    private static final String SAVED_TIPS_KEY = "saved_tips";
    private static final int AUTO_ROTATE_DELAY = 7000; // 7 seconds
    private static final int MIN_SWIPE_DISTANCE = 120;
    private static final int MIN_SWIPE_VELOCITY = 200;

    private ActivityCareerTipsBinding binding;
    private List<CareerTip> allTips;
    private List<CareerTip> filteredTips;
    private int currentTipIndex = 0;
    private String currentCategory = "All";
    private Handler autoRotateHandler;
    private Runnable autoRotateRunnable;
    private GestureDetectorCompat gestureDetector;
    private SharedPreferences sharedPreferences;
    private Set<String> savedTipIds;
    private boolean isAutoRotateEnabled = true;

    // Animation objects
    private Animation slideInRight, slideInLeft, slideOutRight, slideOutLeft, fadeIn, fadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCareerTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize components
        initializeComponents();
        setupToolbar();
        setupAnimations();
        setupGestureDetector();
        setupClickListeners();
        setupCategoryChips();
        initializeTipsData();
        setupAutoRotation();
        
        // Display first tip
        displayCurrentTip();
        updateProgressIndicator();
        
        // Log analytics
        AnalyticsHelper.logNavigation("CareerTipsActivity", "HomeActivity");
    }

    /**
     * Initialize all components and preferences
     */
    private void initializeComponents() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        savedTipIds = sharedPreferences.getStringSet(SAVED_TIPS_KEY, new HashSet<>());
        autoRotateHandler = new Handler(Looper.getMainLooper());
        allTips = new ArrayList<>();
        filteredTips = new ArrayList<>();
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Career Tips");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Initialize all animations for smooth transitions
     */
    private void setupAnimations() {
        slideInRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideInLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        
        // Set animation durations
        slideInRight.setDuration(300);
        slideInLeft.setDuration(300);
        slideOutRight.setDuration(300);
        slideOutLeft.setDuration(300);
        fadeIn.setDuration(400);
        fadeOut.setDuration(400);
    }

    /**
     * Setup gesture detector for swipe navigation
     */
    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, this);
        binding.tipCard.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    /**
     * Setup all click listeners for buttons and interactions
     */
    private void setupClickListeners() {
        // Navigation buttons
        binding.btnPrevious.setOnClickListener(v -> {
            pauseAutoRotation();
            showPreviousTip();
        });

        binding.btnNext.setOnClickListener(v -> {
            pauseAutoRotation();
            showNextTip();
        });

        // Action buttons
        binding.btnSave.setOnClickListener(v -> toggleSaveTip());
        binding.btnShare.setOnClickListener(v -> shareCurrentTip());
        binding.btnSavedTips.setOnClickListener(v -> openSavedTips());

        // Auto-rotate toggle
        binding.btnAutoRotate.setOnClickListener(v -> toggleAutoRotation());

        // Play/Pause for auto-rotation
        binding.btnPlayPause.setOnClickListener(v -> toggleAutoRotation());
    }

    /**
     * Setup category filter chips
     */
    private void setupCategoryChips() {
        String[] categories = {"All", "Networking", "Job Search", "Entrepreneurship", 
                             "Skill Development", "Productivity", "Financial Management"};
        
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            
            // Set MUST green theme for chips
            chip.setChipBackgroundColorResource(R.color.light_green);
            chip.setTextColor(getColor(R.color.must_green));
            
            if (category.equals("All")) {
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.must_green);
                chip.setTextColor(getColor(R.color.white));
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectCategory(category);
                    updateChipStyles(category);
                }
            });
            
            binding.categoryChips.addView(chip);
        }
    }

    /**
     * Update chip styles when category is selected
     */
    private void updateChipStyles(String selectedCategory) {
        for (int i = 0; i < binding.categoryChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.categoryChips.getChildAt(i);
            if (chip.getText().toString().equals(selectedCategory)) {
                chip.setChipBackgroundColorResource(R.color.must_green);
                chip.setTextColor(getColor(R.color.white));
            } else {
                chip.setChecked(false);
                chip.setChipBackgroundColorResource(R.color.light_green);
                chip.setTextColor(getColor(R.color.must_green));
            }
        }
    }

    /**
     * Initialize comprehensive tips data with categories and emojis
     */
    private void initializeTipsData() {
        allTips.clear();
        
        // Networking Tips
        allTips.add(new CareerTip("1", "Attend alumni events regularly to grow your network. 🤝", "Networking", false));
        allTips.add(new CareerTip("2", "Follow up within 24 hours after meeting new contacts. ⏰", "Networking", false));
        allTips.add(new CareerTip("3", "Share valuable content on LinkedIn to stay visible. 📱", "Networking", false));
        allTips.add(new CareerTip("4", "Join professional associations in your field. 🏢", "Networking", false));
        allTips.add(new CareerTip("5", "Offer help before asking for favors from your network. 🤲", "Networking", false));
        allTips.add(new CareerTip("6", "Keep your elevator pitch under 30 seconds. 🗣️", "Networking", false));
        allTips.add(new CareerTip("7", "Connect with colleagues from different departments. 🔄", "Networking", false));
        allTips.add(new CareerTip("8", "Send personalized connection requests, not generic ones. ✉️", "Networking", false));
        allTips.add(new CareerTip("9", "Remember personal details about your contacts. 🧠", "Networking", false));
        allTips.add(new CareerTip("10", "Schedule regular coffee chats with industry peers. ☕", "Networking", false));

        // Job Search Tips
        allTips.add(new CareerTip("11", "Tailor your resume for each job application. 📄", "Job Search", false));
        allTips.add(new CareerTip("12", "Research the company culture before interviews. 🔍", "Job Search", false));
        allTips.add(new CareerTip("13", "Practice the STAR method for behavioral questions. ⭐", "Job Search", false));
        allTips.add(new CareerTip("14", "Apply within the first week of job posting. 🚀", "Job Search", false));
        allTips.add(new CareerTip("15", "Use keywords from job descriptions in your resume. 🎯", "Job Search", false));
        allTips.add(new CareerTip("16", "Prepare thoughtful questions to ask interviewers. ❓", "Job Search", false));
        allTips.add(new CareerTip("17", "Clean up your social media profiles before applying. 🧹", "Job Search", false));
        allTips.add(new CareerTip("18", "Get referrals from employees at target companies. 👥", "Job Search", false));
        allTips.add(new CareerTip("19", "Follow up on applications with a polite email. 📧", "Job Search", false));
        allTips.add(new CareerTip("20", "Practice your interview skills with mock sessions. 🎭", "Job Search", false));

        // Entrepreneurship Tips
        allTips.add(new CareerTip("21", "Start small and validate your business idea first. 🌱", "Entrepreneurship", false));
        allTips.add(new CareerTip("22", "Build a strong personal brand from day one. 🏆", "Entrepreneurship", false));
        allTips.add(new CareerTip("23", "Focus on solving real problems for real people. 💡", "Entrepreneurship", false));
        allTips.add(new CareerTip("24", "Network with other entrepreneurs and mentors. 🤝", "Entrepreneurship", false));
        allTips.add(new CareerTip("25", "Keep your day job while building your side business. 💼", "Entrepreneurship", false));
        allTips.add(new CareerTip("26", "Learn to say no to opportunities that don't align. ❌", "Entrepreneurship", false));
        allTips.add(new CareerTip("27", "Invest in learning financial management skills. 💰", "Entrepreneurship", false));
        allTips.add(new CareerTip("28", "Test your minimum viable product early. 🧪", "Entrepreneurship", false));
        allTips.add(new CareerTip("29", "Build systems and processes for scalability. ⚙️", "Entrepreneurship", false));
        allTips.add(new CareerTip("30", "Customer feedback is more valuable than opinions. 👂", "Entrepreneurship", false));

        // Skill Development Tips
        allTips.add(new CareerTip("31", "Learn a new skill every quarter to stay competitive. 📚", "Skill Development", false));
        allTips.add(new CareerTip("32", "Take online courses during your commute time. 🚇", "Skill Development", false));
        allTips.add(new CareerTip("33", "Practice public speaking at every opportunity. 🎤", "Skill Development", false));
        allTips.add(new CareerTip("34", "Read industry publications and blogs regularly. 📖", "Skill Development", false));
        allTips.add(new CareerTip("35", "Find a mentor in your field for guidance. 👨‍🏫", "Skill Development", false));
        allTips.add(new CareerTip("36", "Attend workshops and seminars in your industry. 🎓", "Skill Development", false));
        allTips.add(new CareerTip("37", "Learn basic coding skills, regardless of your field. 💻", "Skill Development", false));
        allTips.add(new CareerTip("38", "Develop your emotional intelligence daily. 💝", "Skill Development", false));
        allTips.add(new CareerTip("39", "Practice active listening in all conversations. 👂", "Skill Development", false));
        allTips.add(new CareerTip("40", "Get certified in relevant technologies or methods. 📜", "Skill Development", false));

        // Productivity & Work-Life Balance Tips
        allTips.add(new CareerTip("41", "Use the Pomodoro Technique for focused work sessions. 🍅", "Productivity", false));
        allTips.add(new CareerTip("42", "Set boundaries between work and personal time. ⚖️", "Productivity", false));
        allTips.add(new CareerTip("43", "Plan your week every Sunday evening. 📅", "Productivity", false));
        allTips.add(new CareerTip("44", "Take regular breaks to maintain peak performance. ⏸️", "Productivity", false));
        allTips.add(new CareerTip("45", "Eliminate distractions during deep work hours. 🔇", "Productivity", false));
        allTips.add(new CareerTip("46", "Delegate tasks that others can do better. 🤲", "Productivity", false));
        allTips.add(new CareerTip("47", "Exercise regularly to boost mental clarity. 🏃‍♀️", "Productivity", false));
        allTips.add(new CareerTip("48", "Batch similar tasks together for efficiency. 📦", "Productivity", false));
        allTips.add(new CareerTip("49", "Say no to meetings without clear agendas. 🚫", "Productivity", false));
        allTips.add(new CareerTip("50", "Review and reflect on your week every Friday. 🤔", "Productivity", false));

        // Financial Management Tips
        allTips.add(new CareerTip("51", "Negotiate your salary every 1-2 years. 💵", "Financial Management", false));
        allTips.add(new CareerTip("52", "Invest in your 401k from your first paycheck. 🏦", "Financial Management", false));
        allTips.add(new CareerTip("53", "Build an emergency fund of 6 months expenses. 💰", "Financial Management", false));
        allTips.add(new CareerTip("54", "Track your expenses with budgeting apps. 📱", "Financial Management", false));
        allTips.add(new CareerTip("55", "Diversify your income with side projects. 🔄", "Financial Management", false));
        allTips.add(new CareerTip("56", "Research market rates before salary negotiations. 📊", "Financial Management", false));
        allTips.add(new CareerTip("57", "Automate your savings and investments. 🤖", "Financial Management", false));
        allTips.add(new CareerTip("58", "Invest in index funds for long-term growth. 📈", "Financial Management", false));
        allTips.add(new CareerTip("59", "Review your credit report annually. 📋", "Financial Management", false));
        allTips.add(new CareerTip("60", "Consider professional financial planning advice. 👨‍💼", "Financial Management", false));

        // Load saved tips status
        loadSavedTipsStatus();
        
        // Initialize with all tips
        filteredTips.addAll(allTips);
    }

    /**
     * Load saved tips status from preferences
     */
    private void loadSavedTipsStatus() {
        for (CareerTip tip : allTips) {
            tip.setSaved(savedTipIds.contains(tip.getId()));
        }
    }

    /**
     * Filter tips by selected category
     */
    private void selectCategory(String category) {
        currentCategory = category;
        filteredTips.clear();
        
        if ("All".equals(category)) {
            filteredTips.addAll(allTips);
        } else {
            for (CareerTip tip : allTips) {
                if (category.equals(tip.getCategory())) {
                    filteredTips.add(tip);
                }
            }
        }
        
        currentTipIndex = 0;
        displayCurrentTip();
        updateProgressIndicator();
        
        // Log category selection
        AnalyticsHelper.logEvent("career_tips_category_selected", "category", category);
    }

    /**
     * Display the current tip with animation
     */
    private void displayCurrentTip() {
        if (filteredTips.isEmpty()) {
            showEmptyState();
            return;
        }
        
        hideEmptyState();
        CareerTip currentTip = filteredTips.get(currentTipIndex);
        
        // Update UI elements
        binding.tipText.setText(currentTip.getText());
        binding.tipCategory.setText(currentTip.getCategory());
        binding.tipPosition.setText(String.format("%d of %d", currentTipIndex + 1, filteredTips.size()));
        
        // Update save button icon
        updateSaveButton(currentTip.isSaved());
        
        // Update navigation buttons
        binding.btnPrevious.setEnabled(currentTipIndex > 0);
        binding.btnNext.setEnabled(currentTipIndex < filteredTips.size() - 1);
        
        // Apply fade-in animation
        binding.tipCard.startAnimation(fadeIn);
        
        // Log tip view
        AnalyticsHelper.logEvent("career_tip_viewed", "tip_id", currentTip.getId());
    }

    /**
     * Show empty state when no tips are available
     */
    private void showEmptyState() {
        binding.tipCard.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
        binding.btnPrevious.setEnabled(false);
        binding.btnNext.setEnabled(false);
        binding.btnSave.setEnabled(false);
        binding.btnShare.setEnabled(false);
    }

    /**
     * Hide empty state and show tip card
     */
    private void hideEmptyState() {
        binding.tipCard.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.btnSave.setEnabled(true);
        binding.btnShare.setEnabled(true);
    }

    /**
     * Update progress indicator dots
     */
    private void updateProgressIndicator() {
        if (filteredTips.isEmpty()) return;
        
        int progress = (int) (((float) (currentTipIndex + 1) / filteredTips.size()) * 100);
        binding.progressBar.setProgress(progress);
    }

    /**
     * Show previous tip with slide animation
     */
    private void showPreviousTip() {
        if (currentTipIndex > 0) {
            binding.tipCard.startAnimation(slideOutRight);
            currentTipIndex--;
            
            slideOutRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    displayCurrentTip();
                    updateProgressIndicator();
                    binding.tipCard.startAnimation(slideInLeft);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    /**
     * Show next tip with slide animation
     */
    private void showNextTip() {
        if (currentTipIndex < filteredTips.size() - 1) {
            binding.tipCard.startAnimation(slideOutLeft);
            currentTipIndex++;
            
            slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    displayCurrentTip();
                    updateProgressIndicator();
                    binding.tipCard.startAnimation(slideInRight);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        } else {
            // Reached end, restart from beginning
            binding.tipCard.startAnimation(slideOutLeft);
            currentTipIndex = 0;
            
            slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    displayCurrentTip();
                    updateProgressIndicator();
                    binding.tipCard.startAnimation(slideInRight);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    /**
     * Setup auto-rotation functionality
     */
    private void setupAutoRotation() {
        autoRotateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoRotateEnabled && !filteredTips.isEmpty()) {
                    showNextTip();
                    autoRotateHandler.postDelayed(this, AUTO_ROTATE_DELAY);
                }
            }
        };
        
        startAutoRotation();
    }

    /**
     * Start auto-rotation
     */
    private void startAutoRotation() {
        if (isAutoRotateEnabled) {
            autoRotateHandler.postDelayed(autoRotateRunnable, AUTO_ROTATE_DELAY);
            binding.btnPlayPause.setText("⏸️");
        }
    }

    /**
     * Pause auto-rotation temporarily
     */
    private void pauseAutoRotation() {
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
        // Resume after 10 seconds of inactivity
        autoRotateHandler.postDelayed(() -> {
            if (isAutoRotateEnabled) {
                startAutoRotation();
            }
        }, 10000);
    }

    /**
     * Toggle auto-rotation on/off
     */
    private void toggleAutoRotation() {
        isAutoRotateEnabled = !isAutoRotateEnabled;
        
        if (isAutoRotateEnabled) {
            startAutoRotation();
            binding.btnPlayPause.setText("⏸️");
            binding.btnAutoRotate.setText("Auto: ON");
            Toast.makeText(this, "Auto-rotation enabled", Toast.LENGTH_SHORT).show();
        } else {
            autoRotateHandler.removeCallbacks(autoRotateRunnable);
            binding.btnPlayPause.setText("▶️");
            binding.btnAutoRotate.setText("Auto: OFF");
            Toast.makeText(this, "Auto-rotation disabled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Toggle save status of current tip
     */
    private void toggleSaveTip() {
        if (filteredTips.isEmpty()) return;
        
        CareerTip currentTip = filteredTips.get(currentTipIndex);
        boolean newSavedStatus = !currentTip.isSaved();
        currentTip.setSaved(newSavedStatus);
        
        // Update saved tips set
        if (newSavedStatus) {
            savedTipIds.add(currentTip.getId());
            Toast.makeText(this, "Tip saved! 💾", Toast.LENGTH_SHORT).show();
        } else {
            savedTipIds.remove(currentTip.getId());
            Toast.makeText(this, "Tip removed from saved", Toast.LENGTH_SHORT).show();
        }
        
        // Update preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SAVED_TIPS_KEY, savedTipIds);
        editor.apply();
        
        // Update UI
        updateSaveButton(newSavedStatus);
        
        // Log save action
        AnalyticsHelper.logEvent("career_tip_saved", "tip_id", currentTip.getId());
    }

    /**
     * Update save button appearance
     */
    private void updateSaveButton(boolean isSaved) {
        if (isSaved) {
            binding.btnSave.setText("💾 Saved");
            binding.btnSave.setBackgroundTintList(getColorStateList(R.color.must_green));
        } else {
            binding.btnSave.setText("💾 Save");
            binding.btnSave.setBackgroundTintList(getColorStateList(R.color.light_green));
        }
    }

    /**
     * Share current tip via intent
     */
    private void shareCurrentTip() {
        if (filteredTips.isEmpty()) return;
        
        CareerTip currentTip = filteredTips.get(currentTipIndex);
        String shareText = "💡 Career Tip: " + currentTip.getText() + 
                          "\n\n🏷️ Category: " + currentTip.getCategory() + 
                          "\n\nShared from Alumni Portal Career Tips";
        
        Intent shareIntent = Intent.createChooser(new Intent().apply {
            setAction(Intent.ACTION_SEND);
            putExtra(Intent.EXTRA_TEXT, shareText);
            setType("text/plain");
        }, "Share Career Tip");
        
        startActivity(shareIntent);
        
        // Log share action
        AnalyticsHelper.logEvent("career_tip_shared", "tip_id", currentTip.getId());
        Toast.makeText(this, "Tip shared! 📤", Toast.LENGTH_SHORT).show();
    }

    /**
     * Open saved tips activity
     */
    private void openSavedTips() {
        Intent intent = new Intent(this, SavedCareerTipsActivity.class);
        startActivity(intent);
    }

    // Gesture Detector Methods for Swipe Navigation

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) return false;
        
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        
        if (Math.abs(diffX) > Math.abs(diffY) && 
            Math.abs(diffX) > MIN_SWIPE_DISTANCE && 
            Math.abs(velocityX) > MIN_SWIPE_VELOCITY) {
            
            pauseAutoRotation();
            
            if (diffX > 0) {
                // Swipe right - previous tip
                showPreviousTip();
            } else {
                // Swipe left - next tip
                showNextTip();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause auto-rotation when activity is not visible
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume auto-rotation when activity becomes visible
        if (isAutoRotateEnabled) {
            startAutoRotation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handlers
        if (autoRotateHandler != null) {
            autoRotateHandler.removeCallbacks(autoRotateRunnable);
        }
    }
}
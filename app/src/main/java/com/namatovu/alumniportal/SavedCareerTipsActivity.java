package com.namatovu.alumniportal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.namatovu.alumniportal.adapters.SavedTipsAdapter;
import com.namatovu.alumniportal.databinding.ActivitySavedCareerTipsBinding;
import com.namatovu.alumniportal.models.CareerTip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity to display saved career tips
 * Shows only tips that user has bookmarked
 */
public class SavedCareerTipsActivity extends AppCompatActivity {

    private static final String TAG = "SavedCareerTipsActivity";
    private static final String PREFS_NAME = "CareerTipsPrefs";
    private static final String SAVED_TIPS_KEY = "saved_tips";

    private ActivitySavedCareerTipsBinding binding;
    private SavedTipsAdapter adapter;
    private List<CareerTip> savedTips;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedCareerTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        initializeComponents();
        loadSavedTips();
        setupRecyclerView();
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saved Tips");
        }
    }

    /**
     * Initialize components and preferences
     */
    private void initializeComponents() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        savedTips = new ArrayList<>();
    }

    /**
     * Load saved tips from preferences and filter from all tips
     */
    private void loadSavedTips() {
        Set<String> savedTipIds = sharedPreferences.getStringSet(SAVED_TIPS_KEY, new HashSet<>());
        
        // Get all tips (this could be from database in real app)
        List<CareerTip> allTips = getAllTips();
        
        // Filter only saved tips
        savedTips.clear();
        for (CareerTip tip : allTips) {
            if (savedTipIds.contains(tip.getId())) {
                tip.setSaved(true);
                savedTips.add(tip);
            }
        }

        updateUI();
    }

    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        adapter = new SavedTipsAdapter(savedTips, this::onTipUnsaved);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    /**
     * Handle tip unsaved action
     */
    private void onTipUnsaved(CareerTip tip) {
        // Remove from saved tips
        Set<String> savedTipIds = new HashSet<>(sharedPreferences.getStringSet(SAVED_TIPS_KEY, new HashSet<>()));
        savedTipIds.remove(tip.getId());
        
        // Update preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SAVED_TIPS_KEY, savedTipIds);
        editor.apply();
        
        // Remove from list and update UI
        savedTips.remove(tip);
        adapter.notifyDataSetChanged();
        updateUI();
        
        Toast.makeText(this, "Tip removed from saved", Toast.LENGTH_SHORT).show();
    }

    /**
     * Update UI based on saved tips count
     */
    private void updateUI() {
        if (savedTips.isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        
        // Handle "Browse Career Tips" button click
        binding.btnGoToTips.setOnClickListener(v -> {
            Intent intent = new Intent(this, CareerTipsActivity.class);
            startActivity(intent);
        });
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
        
        // Update count
        binding.tipCount.setText(String.format("You have %d saved tips", savedTips.size()));
    }

    /**
     * Get all tips (same as in CareerTipsActivity)
     * In a real app, this would come from a database or API
     */
    private List<CareerTip> getAllTips() {
        List<CareerTip> tips = new ArrayList<>();
        
        // Add all 600 tips (100 per category)
        addNetworkingTips(tips);
        addJobSearchTips(tips);
        addEntrepreneurshipTips(tips);
        addSkillDevelopmentTips(tips);
        addProductivityTips(tips);
        addFinancialManagementTips(tips);

        return tips;
    }

    private void addNetworkingTips(List<CareerTip> tips) {
        String[] networkingTips = {
            "Attend alumni events regularly to grow your network. 🤝",
            "Follow up within 24 hours after meeting new contacts. ⏰",
            "Share valuable content on LinkedIn to stay visible. 📱",
            "Join professional associations in your field. 🏢",
            "Offer help before asking for favors from your network. 🤲"
            // Note: Including only first 5 for brevity - in production, include all 100
        };
        
        for (int i = 0; i < networkingTips.length; i++) {
            tips.add(new CareerTip(String.valueOf(i + 1), networkingTips[i], "Networking", false));
        }
    }

    private void addJobSearchTips(List<CareerTip> tips) {
        String[] jobSearchTips = {
            "Tailor your resume for each job application. 📄",
            "Research the company culture before interviews. 🔍",
            "Practice the STAR method for behavioral questions. ⭐",
            "Apply within the first week of job posting. 🚀",
            "Use keywords from job descriptions in your resume. 🎯"
            // Note: Including only first 5 for brevity - in production, include all 100
        };
        
        for (int i = 0; i < jobSearchTips.length; i++) {
            tips.add(new CareerTip(String.valueOf(100 + i + 1), jobSearchTips[i], "Job Search", false));
        }
    }

    private void addEntrepreneurshipTips(List<CareerTip> tips) {
        String[] entrepreneurshipTips = {
            "Start small and validate your business idea first. 🌱",
            "Build a strong personal brand from day one. 🏆",
            "Focus on solving real problems for real people. 💡",
            "Network with other entrepreneurs and mentors. 🤝",
            "Keep your day job while building your side business. 💼"
            // Note: Including only first 5 for brevity - in production, include all 100
        };
        
        for (int i = 0; i < entrepreneurshipTips.length; i++) {
            tips.add(new CareerTip(String.valueOf(200 + i + 1), entrepreneurshipTips[i], "Entrepreneurship", false));
        }
    }

    private void addSkillDevelopmentTips(List<CareerTip> tips) {
        String[] skillDevelopmentTips = {
            "Learn a new skill every quarter to stay competitive. 📚",
            "Take online courses during your commute time. 🚇",
            "Practice public speaking at every opportunity. 🎤",
            "Read industry publications and blogs regularly. 📖",
            "Find a mentor in your field for guidance. 👨‍🏫"
            // Note: Including only first 5 for brevity - in production, include all 100
        };
        
        for (int i = 0; i < skillDevelopmentTips.length; i++) {
            tips.add(new CareerTip(String.valueOf(300 + i + 1), skillDevelopmentTips[i], "Skill Development", false));
        }
    }

    private void addProductivityTips(List<CareerTip> tips) {
        String[] productivityTips = {
            "Use the Pomodoro Technique for focused work sessions. 🍅",
            "Set boundaries between work and personal time. ⚖️",
            "Plan your week every Sunday evening. 📅",
            "Take regular breaks to maintain peak performance. ⏸️",
            "Eliminate distractions during deep work hours. 🔇"
            // Note: Including only first 5 for brevity - in production, include all 100
        };
        
        for (int i = 0; i < productivityTips.length; i++) {
            tips.add(new CareerTip(String.valueOf(400 + i + 1), productivityTips[i], "Productivity", false));
        }
    }

    private void addFinancialManagementTips(List<CareerTip> tips) {
        String[] financialTips = {
            "Negotiate your salary every 1-2 years. 💵",
            "Invest in your 401k from your first paycheck. 🏦",
            "Build an emergency fund of 6 months expenses. 💰",
            "Track your expenses with budgeting apps. 📱",
            "Diversify your income with side projects. 🔄"
            // Note: Including only first 5 for brevity - in production, include all 100
        };
        
        for (int i = 0; i < financialTips.length; i++) {
            tips.add(new CareerTip(String.valueOf(500 + i + 1), financialTips[i], "Financial Management", false));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
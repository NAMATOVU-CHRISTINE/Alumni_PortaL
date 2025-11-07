package com.namatovu.alumniportal;

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
        
        // Networking Tips
        tips.add(new CareerTip("1", "Attend alumni events regularly to grow your network. 🤝", "Networking", false));
        tips.add(new CareerTip("2", "Follow up within 24 hours after meeting new contacts. ⏰", "Networking", false));
        tips.add(new CareerTip("3", "Share valuable content on LinkedIn to stay visible. 📱", "Networking", false));
        tips.add(new CareerTip("4", "Join professional associations in your field. 🏢", "Networking", false));
        tips.add(new CareerTip("5", "Offer help before asking for favors from your network. 🤲", "Networking", false));
        tips.add(new CareerTip("6", "Keep your elevator pitch under 30 seconds. 🗣️", "Networking", false));
        tips.add(new CareerTip("7", "Connect with colleagues from different departments. 🔄", "Networking", false));
        tips.add(new CareerTip("8", "Send personalized connection requests, not generic ones. ✉️", "Networking", false));
        tips.add(new CareerTip("9", "Remember personal details about your contacts. 🧠", "Networking", false));
        tips.add(new CareerTip("10", "Schedule regular coffee chats with industry peers. ☕", "Networking", false));

        // Job Search Tips
        tips.add(new CareerTip("11", "Tailor your resume for each job application. 📄", "Job Search", false));
        tips.add(new CareerTip("12", "Research the company culture before interviews. 🔍", "Job Search", false));
        tips.add(new CareerTip("13", "Practice the STAR method for behavioral questions. ⭐", "Job Search", false));
        tips.add(new CareerTip("14", "Apply within the first week of job posting. 🚀", "Job Search", false));
        tips.add(new CareerTip("15", "Use keywords from job descriptions in your resume. 🎯", "Job Search", false));
        tips.add(new CareerTip("16", "Prepare thoughtful questions to ask interviewers. ❓", "Job Search", false));
        tips.add(new CareerTip("17", "Clean up your social media profiles before applying. 🧹", "Job Search", false));
        tips.add(new CareerTip("18", "Get referrals from employees at target companies. 👥", "Job Search", false));
        tips.add(new CareerTip("19", "Follow up on applications with a polite email. 📧", "Job Search", false));
        tips.add(new CareerTip("20", "Practice your interview skills with mock sessions. 🎭", "Job Search", false));

        // Entrepreneurship Tips
        tips.add(new CareerTip("21", "Start small and validate your business idea first. 🌱", "Entrepreneurship", false));
        tips.add(new CareerTip("22", "Build a strong personal brand from day one. 🏆", "Entrepreneurship", false));
        tips.add(new CareerTip("23", "Focus on solving real problems for real people. 💡", "Entrepreneurship", false));
        tips.add(new CareerTip("24", "Network with other entrepreneurs and mentors. 🤝", "Entrepreneurship", false));
        tips.add(new CareerTip("25", "Keep your day job while building your side business. 💼", "Entrepreneurship", false));
        tips.add(new CareerTip("26", "Learn to say no to opportunities that don't align. ❌", "Entrepreneurship", false));
        tips.add(new CareerTip("27", "Invest in learning financial management skills. 💰", "Entrepreneurship", false));
        tips.add(new CareerTip("28", "Test your minimum viable product early. 🧪", "Entrepreneurship", false));
        tips.add(new CareerTip("29", "Build systems and processes for scalability. ⚙️", "Entrepreneurship", false));
        tips.add(new CareerTip("30", "Customer feedback is more valuable than opinions. 👂", "Entrepreneurship", false));

        // Skill Development Tips
        tips.add(new CareerTip("31", "Learn a new skill every quarter to stay competitive. 📚", "Skill Development", false));
        tips.add(new CareerTip("32", "Take online courses during your commute time. 🚇", "Skill Development", false));
        tips.add(new CareerTip("33", "Practice public speaking at every opportunity. 🎤", "Skill Development", false));
        tips.add(new CareerTip("34", "Read industry publications and blogs regularly. 📖", "Skill Development", false));
        tips.add(new CareerTip("35", "Find a mentor in your field for guidance. 👨‍🏫", "Skill Development", false));
        tips.add(new CareerTip("36", "Attend workshops and seminars in your industry. 🎓", "Skill Development", false));
        tips.add(new CareerTip("37", "Learn basic coding skills, regardless of your field. 💻", "Skill Development", false));
        tips.add(new CareerTip("38", "Develop your emotional intelligence daily. 💝", "Skill Development", false));
        tips.add(new CareerTip("39", "Practice active listening in all conversations. 👂", "Skill Development", false));
        tips.add(new CareerTip("40", "Get certified in relevant technologies or methods. 📜", "Skill Development", false));

        // Productivity & Work-Life Balance Tips
        tips.add(new CareerTip("41", "Use the Pomodoro Technique for focused work sessions. 🍅", "Productivity", false));
        tips.add(new CareerTip("42", "Set boundaries between work and personal time. ⚖️", "Productivity", false));
        tips.add(new CareerTip("43", "Plan your week every Sunday evening. 📅", "Productivity", false));
        tips.add(new CareerTip("44", "Take regular breaks to maintain peak performance. ⏸️", "Productivity", false));
        tips.add(new CareerTip("45", "Eliminate distractions during deep work hours. 🔇", "Productivity", false));
        tips.add(new CareerTip("46", "Delegate tasks that others can do better. 🤲", "Productivity", false));
        tips.add(new CareerTip("47", "Exercise regularly to boost mental clarity. 🏃‍♀️", "Productivity", false));
        tips.add(new CareerTip("48", "Batch similar tasks together for efficiency. 📦", "Productivity", false));
        tips.add(new CareerTip("49", "Say no to meetings without clear agendas. 🚫", "Productivity", false));
        tips.add(new CareerTip("50", "Review and reflect on your week every Friday. 🤔", "Productivity", false));

        // Financial Management Tips
        tips.add(new CareerTip("51", "Negotiate your salary every 1-2 years. 💵", "Financial Management", false));
        tips.add(new CareerTip("52", "Invest in your 401k from your first paycheck. 🏦", "Financial Management", false));
        tips.add(new CareerTip("53", "Build an emergency fund of 6 months expenses. 💰", "Financial Management", false));
        tips.add(new CareerTip("54", "Track your expenses with budgeting apps. 📱", "Financial Management", false));
        tips.add(new CareerTip("55", "Diversify your income with side projects. 🔄", "Financial Management", false));
        tips.add(new CareerTip("56", "Research market rates before salary negotiations. 📊", "Financial Management", false));
        tips.add(new CareerTip("57", "Automate your savings and investments. 🤖", "Financial Management", false));
        tips.add(new CareerTip("58", "Invest in index funds for long-term growth. 📈", "Financial Management", false));
        tips.add(new CareerTip("59", "Review your credit report annually. 📋", "Financial Management", false));
        tips.add(new CareerTip("60", "Consider professional financial planning advice. 👨‍💼", "Financial Management", false));

        return tips;
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
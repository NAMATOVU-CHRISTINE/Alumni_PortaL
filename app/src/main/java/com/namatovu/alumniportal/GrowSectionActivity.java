package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.namatovu.alumniportal.adapters.GrowCategoryAdapter;
import com.namatovu.alumniportal.databinding.ActivityGrowSectionBinding;
import com.namatovu.alumniportal.models.GrowCategory;

import java.util.ArrayList;
import java.util.List;

public class GrowSectionActivity extends AppCompatActivity implements GrowCategoryAdapter.OnCategoryClickListener {

    private ActivityGrowSectionBinding binding;
    private GrowCategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGrowSectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        loadGrowCategories();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Growth Opportunities");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new GrowCategoryAdapter(this, this);
        binding.recyclerViewGrowCategories.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewGrowCategories.setAdapter(adapter);
    }

    private void loadGrowCategories() {
        List<GrowCategory> categories = new ArrayList<>();
        
        categories.add(new GrowCategory(
            "Find Mentors",
            "Connect with experienced professionals",
            "👥",
            "#E3F2FD",
            MentorshipActivity.class
        ));
        
        categories.add(new GrowCategory(
            "Jobs & Opportunities",
            "Discover career opportunities",
            "💼",
            "#F3E5F5",
            JobsActivity.class
        ));
        
        categories.add(new GrowCategory(
            "Knowledge Hub",
            "Access learning resources",
            "📚",
            "#FFF9C4",
            KnowledgeActivity.class
        ));
        
        categories.add(new GrowCategory(
            "Career Tips",
            "Get professional advice",
            "💡",
            "#E8F5E8",
            CareerTipsActivity.class
        ));
        
        categories.add(new GrowCategory(
            "Skill Building",
            "Develop your capabilities",
            "🎯",
            "#FFE0B2",
            CareerTipsActivity.class
        ));
        
        categories.add(new GrowCategory(
            "Industry Insights",
            "Stay updated with trends",
            "📈",
            "#FFCDD2",
            KnowledgeActivity.class
        ));

        adapter.updateCategories(categories);
    }

    @Override
    public void onCategoryClick(GrowCategory category) {
        try {
            Intent intent = new Intent(this, category.getTargetActivity());
            startActivity(intent);
        } catch (Exception e) {
            // Fallback for activities that don't exist yet
            Intent intent = new Intent(this, JobsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
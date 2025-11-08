package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.namatovu.alumniportal.databinding.ActivityEventsNewsBinding;
import com.namatovu.alumniportal.models.Event;
import com.namatovu.alumniportal.models.News;
import com.namatovu.alumniportal.models.EventsAnalytics;
import com.namatovu.alumniportal.adapters.EventNewsAdapter;
import com.namatovu.alumniportal.adapters.NewsListAdapter;
import com.namatovu.alumniportal.utils.EventsDataProvider;

import java.util.List;
import java.util.ArrayList;

/**
 * Events, News & Insights Activity
 * Displays university news, events, mentorship programs, and analytics
 */
public class EventsNewsActivity extends AppCompatActivity {
    
    private static final String TAG = "EventsNewsActivity";
    private ActivityEventsNewsBinding binding;
    
    // Adapters
    private EventNewsAdapter eventsAdapter;
    private NewsListAdapter newsAdapter;
    
    // Data
    private List<Event> allEvents;
    private List<News> allNews;
    private List<Event> filteredEvents;
    private List<News> filteredNews;
    private EventsAnalytics analytics;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventsNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupToolbar();
        setupRecyclerViews();
        setupSearchFunctionality();
        setupSwipeRefresh();
        setupClickListeners();
        
        // Load initial data
        loadData();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerViews() {
        // Setup Events RecyclerView
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();
        eventsAdapter = new EventNewsAdapter(this, filteredEvents);
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.eventsRecyclerView.setAdapter(eventsAdapter);
        
        // Setup News RecyclerView
        allNews = new ArrayList<>();
        filteredNews = new ArrayList<>();
        newsAdapter = new NewsListAdapter(this, filteredNews);
        binding.newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.newsRecyclerView.setAdapter(newsAdapter);
    }
    
    private void setupSearchFunctionality() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContent(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.must_green);
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }
    
    private void setupClickListeners() {
        // View All Events button
        binding.viewAllEventsBtn.setOnClickListener(v -> {
            // Navigate to dedicated events page or show all events
            Toast.makeText(this, "View All Events", Toast.LENGTH_SHORT).show();
        });
        
        // View All News button
        binding.viewAllNewsBtn.setOnClickListener(v -> {
            // Navigate to dedicated news page or show all news
            Toast.makeText(this, "View All News", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadData() {
        showLoading(true);
        
        // Load data from provider (simulating async data loading)
        new Thread(() -> {
            try {
                // Simulate network delay
                Thread.sleep(1000);
                
                // Load events and news
                allEvents = EventsDataProvider.getEvents();
                allNews = EventsDataProvider.getNews();
                analytics = EventsDataProvider.getAnalytics(allEvents, allNews);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    updateUI();
                    showLoading(false);
                });
                
            } catch (InterruptedException e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(EventsNewsActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void refreshData() {
        // Refresh data from provider
        new Thread(() -> {
            try {
                // Simulate network delay
                Thread.sleep(500);
                
                // Reload data
                allEvents = EventsDataProvider.getEvents();
                allNews = EventsDataProvider.getNews();
                analytics = EventsDataProvider.getAnalytics(allEvents, allNews);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    updateUI();
                    binding.swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(EventsNewsActivity.this, "Data refreshed", Toast.LENGTH_SHORT).show();
                });
                
            } catch (InterruptedException e) {
                runOnUiThread(() -> {
                    binding.swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(EventsNewsActivity.this, "Error refreshing data", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void updateUI() {
        // Update analytics
        updateAnalytics();
        
        // Update lists (show initial filtered view)
        filteredEvents.clear();
        filteredEvents.addAll(allEvents);
        eventsAdapter.notifyDataSetChanged();
        
        filteredNews.clear();
        filteredNews.addAll(allNews);
        newsAdapter.notifyDataSetChanged();
        
        // Update empty states
        updateEmptyStates();
    }
    
    private void updateAnalytics() {
        if (analytics != null) {
            binding.totalEventsText.setText(String.valueOf(analytics.getTotalEvents()));
            binding.totalArticlesText.setText(String.valueOf(analytics.getTotalArticles()));
            binding.topCategoryText.setText(analytics.getOverallTopCategory());
        }
    }
    
    private void filterContent(String query) {
        // Filter events
        filteredEvents.clear();
        if (query.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            for (Event event : allEvents) {
                if (event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    event.getSummary().toLowerCase().contains(query.toLowerCase()) ||
                    event.getCategory().getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }
        }
        
        // Filter news
        filteredNews.clear();
        if (query.isEmpty()) {
            filteredNews.addAll(allNews);
        } else {
            for (News news : allNews) {
                if (news.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    news.getSummary().toLowerCase().contains(query.toLowerCase()) ||
                    news.getCategory().getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                    filteredNews.add(news);
                }
            }
        }
        
        // Notify adapters
        eventsAdapter.notifyDataSetChanged();
        newsAdapter.notifyDataSetChanged();
        
        // Update empty states
        updateEmptyStates();
    }
    
    private void updateEmptyStates() {
        // Events empty state
        binding.eventsEmptyState.setVisibility(
            filteredEvents.isEmpty() ? View.VISIBLE : View.GONE
        );
        binding.eventsRecyclerView.setVisibility(
            filteredEvents.isEmpty() ? View.GONE : View.VISIBLE
        );
        
        // News empty state
        binding.newsEmptyState.setVisibility(
            filteredNews.isEmpty() ? View.VISIBLE : View.GONE
        );
        binding.newsRecyclerView.setVisibility(
            filteredNews.isEmpty() ? View.GONE : View.VISIBLE
        );
    }
    
    private void showLoading(boolean show) {
        binding.loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
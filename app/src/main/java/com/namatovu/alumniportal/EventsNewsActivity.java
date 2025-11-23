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
        // Category chip listeners
        binding.eventCategoryChips.setOnCheckedChangeListener((group, checkedId) -> {
            filterEventsByCategory(checkedId);
        });
    }
    
    private void filterEventsByCategory(int chipId) {
        String selectedCategory = null;
        
        if (chipId == R.id.chipAll) {
            selectedCategory = null;
        } else if (chipId == R.id.chipMentorship) {
            selectedCategory = "MENTORSHIP";
        } else if (chipId == R.id.chipLeadership) {
            selectedCategory = "LEADERSHIP";
        } else if (chipId == R.id.chipNetworking) {
            selectedCategory = "NETWORKING";
        } else if (chipId == R.id.chipCareer) {
            selectedCategory = "CAREER";
        } else if (chipId == R.id.chipTechnology) {
            selectedCategory = "TECHNOLOGY";
        }
        
        filterEventsBySelectedCategory(selectedCategory);
    }
    
    private void filterEventsBySelectedCategory(String category) {
        filteredEvents.clear();
        
        if (category == null) {
            filteredEvents.addAll(allEvents);
        } else {
            for (Event event : allEvents) {
                if (event.getCategory().name().equals(category)) {
                    filteredEvents.add(event);
                }
            }
        }
        
        eventsAdapter.notifyDataSetChanged();
        updateEmptyStates();
    }
    
    private void loadData() {
        showLoading(true);
        
        // Trigger scraping of MUST website news in background
        EventsDataProvider.refreshNewsFromMUSTWebsite();
        
        // Load events and news data
        allEvents = EventsDataProvider.getEvents();
        allNews = EventsDataProvider.getNews();
        analytics = EventsDataProvider.getAnalytics(allEvents, allNews);
        
        // Update UI
        updateUI();
        showLoading(false);
    }
    
    private void refreshData() {
        // Reload static data
        allEvents = EventsDataProvider.getEvents();
        allNews = EventsDataProvider.getNews();
        analytics = EventsDataProvider.getAnalytics(allEvents, allNews);
        
        updateUI();
        binding.swipeRefreshLayout.setRefreshing(false);
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
        // Analytics display removed - focus on content
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
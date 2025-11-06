package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.adapters.NewsAdapter;
import com.namatovu.alumniportal.databinding.ActivityNewsFeedBinding;
import com.namatovu.alumniportal.models.NewsArticle;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewsFeedActivity extends AppCompatActivity {
    private static final String TAG = "NewsFeedActivity";
    
    private ActivityNewsFeedBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NewsAdapter adapter;
    private List<NewsArticle> allArticles;
    private List<NewsArticle> filteredArticles;
    private String searchQuery = "";
    private String selectedCategory = "all";
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("NewsFeedActivity", "HomeActivity");

        // Initialize lists
        allArticles = new ArrayList<>();
        filteredArticles = new ArrayList<>();

        setupToolbar();
        setupSearch();
        setupCategoryFilters();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFAB();
        loadNews();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alumni News");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                filterNews();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilters() {
        String[] categories = {"All", "University", "Alumni", "Achievements", "Announcements", "Careers", "Research"};
        
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChecked("All".equals(category));
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck other chips
                    for (int i = 0; i < binding.categoryChipGroup.getChildCount(); i++) {
                        Chip otherChip = (Chip) binding.categoryChipGroup.getChildAt(i);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                    selectedCategory = category.toLowerCase();
                    filterNews();
                }
            });
            
            binding.categoryChipGroup.addView(chip);
        }
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(filteredArticles, currentUserId, new NewsAdapter.OnNewsActionListener() {
            @Override
            public void onNewsClick(NewsArticle article) {
                Intent intent = new Intent(NewsFeedActivity.this, ArticleDetailsActivity.class);
                intent.putExtra("articleId", article.getArticleId());
                startActivity(intent);
                
                // Increment view count
                incrementViewCount(article);
            }

            @Override
            public void onLikeClick(NewsArticle article) {
                toggleLike(article);
            }

            @Override
            public void onShareClick(NewsArticle article) {
                shareArticle(article);
            }

            @Override
            public void onCommentClick(NewsArticle article) {
                Intent intent = new Intent(NewsFeedActivity.this, ArticleDetailsActivity.class);
                intent.putExtra("articleId", article.getArticleId());
                intent.putExtra("focusComments", true);
                startActivity(intent);
            }

            @Override
            public void onAuthorClick(String authorId) {
                Intent intent = new Intent(NewsFeedActivity.this, ViewProfileActivity.class);
                intent.putExtra("userId", authorId);
                startActivity(intent);
            }
        });
        
        binding.newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.newsRecyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadNews);
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupFAB() {
        binding.createArticleFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateArticleActivity.class);
            startActivity(intent);
        });
    }

    private void loadNews() {
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        binding.noNewsText.setVisibility(View.GONE);

        db.collection("news")
                .whereEqualTo("isPublished", true)
                .orderBy("isPinned", Query.Direction.DESCENDING)
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        NewsArticle article = document.toObject(NewsArticle.class);
                        article.setArticleId(document.getId());
                        allArticles.add(article);
                    }
                    
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    filterNews();
                    
                    Log.d(TAG, "Loaded " + allArticles.size() + " news articles");
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    binding.noNewsText.setVisibility(View.VISIBLE);
                    binding.noNewsText.setText("Failed to load news");
                    
                    Toast.makeText(this, "Failed to load news", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading news", e);
                    
                    AnalyticsHelper.logError("news_load_failed", e.getMessage(), "NewsFeedActivity");
                });
    }

    private void filterNews() {
        filteredArticles.clear();
        
        for (NewsArticle article : allArticles) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                    (article.getTitle() != null && article.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (article.getContent() != null && article.getContent().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (article.getAuthorName() != null && article.getAuthorName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (article.getTags() != null && article.getTags().toString().toLowerCase().contains(searchQuery.toLowerCase()));
            
            boolean matchesCategory = "all".equals(selectedCategory) || 
                    (article.getCategory() != null && article.getCategory().equalsIgnoreCase(selectedCategory));
            
            if (matchesSearch && matchesCategory) {
                filteredArticles.add(article);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show/hide no news message
        if (filteredArticles.isEmpty()) {
            binding.noNewsText.setVisibility(View.VISIBLE);
            if (!allArticles.isEmpty()) {
                binding.noNewsText.setText("No articles match your search criteria");
            } else {
                binding.noNewsText.setText("No news articles available");
            }
        } else {
            binding.noNewsText.setVisibility(View.GONE);
        }
        
        binding.articleCountText.setText(filteredArticles.size() + " articles");
    }

    private void toggleLike(NewsArticle article) {
        if (article.getArticleId() == null || currentUserId.isEmpty()) return;
        
        List<String> likedByUserIds = article.getLikedByUserIds();
        if (likedByUserIds == null) {
            likedByUserIds = new ArrayList<>();
        }
        
        boolean isCurrentlyLiked = likedByUserIds.contains(currentUserId);
        
        if (isCurrentlyLiked) {
            likedByUserIds.remove(currentUserId);
            article.decrementLikeCount();
        } else {
            likedByUserIds.add(currentUserId);
            article.incrementLikeCount();
        }
        
        article.setLikedByUserIds(likedByUserIds);
        
        db.collection("news").document(article.getArticleId())
                .update("likedByUserIds", likedByUserIds, "likeCount", article.getLikeCount())
                .addOnSuccessListener(aVoid -> {
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Revert changes on failure
                    if (isCurrentlyLiked) {
                        likedByUserIds.add(currentUserId);
                        article.incrementLikeCount();
                    } else {
                        likedByUserIds.remove(currentUserId);
                        article.decrementLikeCount();
                    }
                    article.setLikedByUserIds(likedByUserIds);
                    adapter.notifyDataSetChanged();
                    
                    Toast.makeText(this, "Failed to update like", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to toggle like", e);
                });
    }

    private void shareArticle(NewsArticle article) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
        
        String shareText = article.getTitle() + "\n\n" +
                          article.getGeneratedSummary() + "\n\n" +
                          "Read more in the Alumni Portal app";
        
        if (article.getSourceUrl() != null) {
            shareText += "\nSource: " + article.getSourceUrl();
        }
        
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Article"));
    }

    private void incrementViewCount(NewsArticle article) {
        if (article.getArticleId() != null) {
            db.collection("news").document(article.getArticleId())
                    .update("viewCount", article.getViewCount() + 1)
                    .addOnSuccessListener(aVoid -> {
                        article.incrementViewCount();
                        Log.d(TAG, "View count incremented for article: " + article.getTitle());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to increment view count", e);
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadNews();
    }
}
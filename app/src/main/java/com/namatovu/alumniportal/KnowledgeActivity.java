package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.namatovu.alumniportal.adapters.ArticleAdapter;
import com.namatovu.alumniportal.models.Article;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Knowledge Activity - Interactive articles and resources section
 * Features: Search, filtering by category, add articles, bookmark/share functionality
 */
public class KnowledgeActivity extends AppCompatActivity implements ArticleAdapter.OnArticleClickListener {

    private static final String TAG = "KnowledgeActivity";
    
    private RecyclerView recyclerViewArticles;
    private ArticleAdapter articleAdapter;
    private SearchView searchView;
    private ChipGroup chipGroupCategories;
    private FloatingActionButton fabAddArticle;
    private View emptyStateLayout;
    
    private List<Article> articles;
    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchView();
        setupCategoryFilters();
        setupFab();
        
        updateEmptyState(); // Show empty state initially
    }

    private void initializeViews() {
        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);
        searchView = findViewById(R.id.searchView);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        fabAddArticle = findViewById(R.id.fabAddArticle);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        articles = new ArrayList<>();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Knowledge Hub 📚");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        articleAdapter = new ArticleAdapter(this, articles);
        articleAdapter.setOnArticleClickListener(this); // Set the click listener
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        
        recyclerViewArticles.setLayoutManager(layoutManager);
        recyclerViewArticles.setAdapter(articleAdapter);
        
        // Debug logging
        android.util.Log.d("KnowledgeActivity", "RecyclerView setup completed");
        android.util.Log.d("KnowledgeActivity", "Layout manager set: " + (recyclerViewArticles.getLayoutManager() != null));
        android.util.Log.d("KnowledgeActivity", "Adapter set: " + (recyclerViewArticles.getAdapter() != null));
        android.util.Log.d("KnowledgeActivity", "RecyclerView visibility: " + recyclerViewArticles.getVisibility());
        
        updateEmptyState();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                articleAdapter.filter(query);
                updateEmptyState();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                articleAdapter.filter(newText);
                updateEmptyState();
                return false;
            }
        });
    }

    private void setupCategoryFilters() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "All";
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                String chipText = selectedChip.getText().toString();
                
                // Extract category name from chip text (remove emoji)
                if (chipText.equals("All")) {
                    currentCategory = "All";
                } else if (chipText.contains("Networking")) {
                    currentCategory = "Networking";
                } else if (chipText.contains("Skills")) {
                    currentCategory = "Skills";
                } else if (chipText.contains("Mentorship")) {
                    currentCategory = "Mentorship";
                } else if (chipText.contains("Career")) {
                    currentCategory = "Career Growth";
                } else if (chipText.contains("Leadership")) {
                    currentCategory = "Leadership";
                }
            }
            
            articleAdapter.filterByCategory(currentCategory);
            updateEmptyState();
        });
    }

    private void setupFab() {
        fabAddArticle.setOnClickListener(v -> {
            Intent intent = new Intent(KnowledgeActivity.this, AddArticleActivity.class);
            startActivityForResult(intent, 100);
        });
    }

    private void updateEmptyState() {
        int itemCount = articleAdapter.getItemCount();
        android.util.Log.d("KnowledgeActivity", "updateEmptyState - Item count: " + itemCount);
        
        if (itemCount == 0) {
            android.util.Log.d("KnowledgeActivity", "Showing empty state");
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewArticles.setVisibility(View.GONE);
        } else {
            android.util.Log.d("KnowledgeActivity", "Showing RecyclerView with " + itemCount + " items");
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewArticles.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            // Get the new article from AddArticleActivity
            String title = data.getStringExtra("title");
            String description = data.getStringExtra("description");
            String content = data.getStringExtra("content");
            String category = data.getStringExtra("category");

            android.util.Log.d("KnowledgeActivity", "Received new article: " + title + ", category: " + category);

            if (title != null && description != null && content != null && category != null) {
                Article newArticle = new Article(title, description, content, category);
                newArticle.setId("article_" + System.currentTimeMillis());
                newArticle.setAuthorName("You");
                newArticle.setDateCreated(new Date()); // Current time - will be newest

                android.util.Log.d("KnowledgeActivity", "Articles before adding: " + articles.size());
                
                // Use addArticle method which handles both lists
                articleAdapter.addArticle(newArticle);
                
                // Also add to our local list for consistency  
                articles.add(0, newArticle);
                
                android.util.Log.d("KnowledgeActivity", "Articles after adding: " + articles.size());
                
                updateEmptyState();
                
                android.util.Log.d("KnowledgeActivity", "Used addArticle method on adapter");
                
                // Scroll to the top to show the new article
                recyclerViewArticles.scrollToPosition(0);

                Toast.makeText(this, "Article added successfully! 📚", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ArticleAdapter.OnArticleClickListener implementation
    @Override
    public void onArticleClick(Article article) {
        // Open detailed article view
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        intent.putExtra("article_title", article.getTitle());
        intent.putExtra("article_content", article.getContent());
        intent.putExtra("article_category", article.getCategory());
        intent.putExtra("article_author", article.getAuthorName());
        intent.putExtra("article_date", article.getFormattedDate());
        intent.putExtra("article_like_count", article.getLikeCount());
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(Article article) {
        article.setBookmarked(!article.isBookmarked());
        articleAdapter.notifyDataSetChanged();
        
        String message = article.isBookmarked() ? 
            "Article saved! 📖" : "Article removed from saved items";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareClick(Article article) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            article.getTitle() + "\n\n" + 
            article.getDescription() + "\n\n" +
            "Shared from Alumni Portal Knowledge Hub 📚");
        
        startActivity(Intent.createChooser(shareIntent, "Share Article"));
    }

    @Override
    public void onLikeClick(Article article) {
        // Simulate user ID (in real app, get from authentication)
        String userId = "current_user_123";
        
        article.toggleLike(userId);
        articleAdapter.notifyDataSetChanged();
        
        String message = article.isLiked() ? 
            "Article liked! ❤️" : "Like removed";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh articles when returning from Add Article activity
        // In a real app, you would reload from database/server
        articleAdapter.notifyDataSetChanged();
    }
}
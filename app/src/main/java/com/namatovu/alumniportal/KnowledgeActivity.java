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
        loadSampleData();
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
            getSupportActionBar().setTitle("Knowledge 📚");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        articleAdapter = new ArticleAdapter(this, articles);
        articleAdapter.setOnArticleClickListener(this);
        
        recyclerViewArticles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArticles.setAdapter(articleAdapter);
        recyclerViewArticles.setNestedScrollingEnabled(false);
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

    private void loadSampleData() {
        // Sample articles with realistic networking and mentorship content
        List<Article> sampleArticles = Arrays.asList(
            new Article(
                "Build Meaningful Professional Relationships",
                "Learn proven strategies to connect with professionals in your field and maintain lasting relationships that benefit your career growth.",
                "Building professional relationships is one of the most valuable investments you can make in your career. Start by attending industry events, joining professional associations, and engaging authentically with colleagues. Remember to give before you receive - offer help, share knowledge, and be genuinely interested in others' success. Follow up consistently and maintain connections through regular check-ins, sharing relevant opportunities, and celebrating others' achievements.",
                "Networking"
            ),
            
            new Article(
                "How to Find the Right Mentor",
                "Discover effective approaches to identify, connect with, and maintain relationships with mentors who can guide your career journey.",
                "Finding the right mentor requires clarity about your goals and strategic networking. Start by identifying professionals whose career paths align with your aspirations. Research their background, values, and expertise. When reaching out, be specific about what you're seeking and what you can offer in return. Prepare thoughtful questions and be respectful of their time. A good mentor-mentee relationship is built on mutual respect, clear expectations, and consistent communication.",
                "Mentorship"
            ),
            
            new Article(
                "Essential Skills for Career Growth in 2025",
                "Master the key competencies that employers value most, from technical skills to emotional intelligence and adaptability.",
                "The modern workplace demands a diverse skill set. Technical proficiency remains important, but soft skills like emotional intelligence, critical thinking, and adaptability are equally crucial. Focus on developing data literacy, digital communication skills, and cross-cultural competency. Continuously learn through online courses, workshops, and hands-on projects. Stay updated with industry trends and emerging technologies in your field.",
                "Skills"
            ),
            
            new Article(
                "Effective Leadership in Remote Teams",
                "Master the art of leading and motivating teams in virtual work environments with proven strategies and tools.",
                "Leading remote teams requires intentional communication, trust-building, and clear expectations. Establish regular check-ins, use collaborative tools effectively, and create opportunities for team bonding. Focus on outcomes rather than micromanaging processes. Provide clear direction, celebrate achievements, and ensure team members feel connected to the larger mission. Develop your emotional intelligence to read virtual cues and support team members' well-being.",
                "Leadership"
            ),
            
            new Article(
                "Navigating Career Transitions Successfully",
                "Strategic guidance for making smooth career changes, whether switching industries, roles, or starting your own venture.",
                "Career transitions can be challenging but rewarding. Start by conducting a thorough self-assessment of your skills, values, and goals. Research your target industry or role extensively. Build bridges by networking with professionals in your desired field and seeking informational interviews. Consider taking courses or certifications to fill skill gaps. Create a transition plan with realistic timelines and milestones. Be patient with the process and open to unexpected opportunities.",
                "Career Growth"
            ),
            
            new Article(
                "Maximizing Alumni Network Benefits",
                "Leverage your educational connections to accelerate career growth, find opportunities, and build lasting professional relationships.",
                "Your alumni network is a powerful career resource that often goes underutilized. Start by updating your alumni directory profile and joining local alumni chapters. Attend alumni events, both virtual and in-person. Reach out to alumni in your target companies or industries for informational interviews. Offer to help current students or recent graduates - giving back strengthens your network. Use social media platforms like LinkedIn to connect with fellow alumni and stay updated on their career progress.",
                "Networking"
            ),
            
            new Article(
                "Developing Executive Presence",
                "Build the confidence, communication skills, and strategic thinking needed to advance to senior leadership positions.",
                "Executive presence combines confidence, clarity, and authenticity. Develop strong communication skills by practicing public speaking and active listening. Build strategic thinking capabilities by understanding business fundamentals and industry trends. Cultivate emotional intelligence to navigate complex interpersonal dynamics. Dress appropriately for your role and industry. Practice decisive decision-making and take calculated risks. Seek feedback regularly and work with a coach to refine your leadership style.",
                "Leadership"
            ),
            
            new Article(
                "Creating a Personal Brand Strategy",
                "Build and maintain a professional brand that opens doors and accelerates career opportunities.",
                "Your personal brand is how others perceive your professional value and expertise. Start by defining your unique value proposition and target audience. Create consistent messaging across all platforms - LinkedIn, resume, email signature, and networking conversations. Share valuable content in your area of expertise through articles, posts, or speaking engagements. Be authentic and align your brand with your values. Monitor your online presence and actively manage your professional reputation.",
                "Career Growth"
            )
        );

        // Set creation dates and author info
        String[] authorNames = {"Sarah Johnson", "Michael Chen", "Dr. Emily Rodriguez", "James Wilson", "Priya Patel", "David Thompson", "Lisa Marie", "Alex Kumar"};
        for (int i = 0; i < sampleArticles.size(); i++) {
            Article article = sampleArticles.get(i);
            article.setId("article_" + i);
            article.setAuthorName(authorNames[i % authorNames.length]);
            article.setDateCreated(new Date(System.currentTimeMillis() - (i * 86400000L))); // Stagger dates
            
            // Add some initial likes to make it more realistic
            article.setLikeCount(5 + (i * 3)); // Varying like counts
            article.setLiked(i % 3 == 0); // Some articles are pre-liked
        }

        articles.addAll(sampleArticles);
        
        // Sort articles by date (newest first)
        articles.sort((a1, a2) -> {
            if (a1.getDateCreated() == null && a2.getDateCreated() == null) return 0;
            if (a1.getDateCreated() == null) return 1;
            if (a2.getDateCreated() == null) return -1;
            return a2.getDateCreated().compareTo(a1.getDateCreated()); // Newest first
        });
        
        articleAdapter.updateArticles(articles);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (articleAdapter.getItemCount() == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewArticles.setVisibility(View.GONE);
        } else {
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

            if (title != null && description != null && content != null && category != null) {
                Article newArticle = new Article(title, description, content, category);
                newArticle.setId("article_" + System.currentTimeMillis());
                newArticle.setAuthorName("You");
                newArticle.setDateCreated(new Date()); // Current time - will be newest

                // Add to the beginning of the main list
                articles.add(0, newArticle);
                
                // Sort the entire list to ensure proper ordering
                articles.sort((a1, a2) -> {
                    if (a1.getDateCreated() == null && a2.getDateCreated() == null) return 0;
                    if (a1.getDateCreated() == null) return 1;
                    if (a2.getDateCreated() == null) return -1;
                    return a2.getDateCreated().compareTo(a1.getDateCreated()); // Newest first
                });
                
                // Update the adapter with the sorted list
                articleAdapter.updateArticles(articles);
                updateEmptyState();
                
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
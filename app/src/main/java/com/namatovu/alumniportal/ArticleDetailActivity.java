package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Article Detail Activity - Full view of an article with content and actions
 */
public class ArticleDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvAuthor, tvDate, tvContent;
    private MaterialButton btnBookmark, btnShare;
    private FloatingActionButton fabShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        initializeViews();
        setupToolbar();
        loadArticleData();
        setupActionButtons();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvDate = findViewById(R.id.tvDate);
        tvContent = findViewById(R.id.tvContent);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        fabShare = findViewById(R.id.fabShare);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Article Details");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadArticleData() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("article_title");
        String category = intent.getStringExtra("article_category");
        String author = intent.getStringExtra("article_author");
        String date = intent.getStringExtra("article_date");
        String content = intent.getStringExtra("article_content");

        tvTitle.setText(title);
        tvCategory.setText(getCategoryWithIcon(category));
        tvAuthor.setText("By " + author);
        tvDate.setText(date);
        tvContent.setText(content);
    }

    private void setupActionButtons() {
        btnBookmark.setOnClickListener(v -> {
            // Toggle bookmark state
            String currentText = btnBookmark.getText().toString();
            if (currentText.contains("Save")) {
                btnBookmark.setText("✅ Saved");
                Toast.makeText(this, "Article saved! 📖", Toast.LENGTH_SHORT).show();
            } else {
                btnBookmark.setText("📖 Save");
                Toast.makeText(this, "Article removed from saved items", Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> shareArticle());
        fabShare.setOnClickListener(v -> shareArticle());
    }

    private void shareArticle() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("article_title");
        String content = intent.getStringExtra("article_content");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            title + "\n\n" + 
            content + "\n\n" +
            "Shared from Alumni Portal Knowledge Hub 📚");
        
        startActivity(Intent.createChooser(shareIntent, "Share Article"));
    }

    private String getCategoryWithIcon(String category) {
        switch (category) {
            case "Networking": return "🤝 " + category;
            case "Skills": return "💡 " + category;
            case "Mentorship": return "🌟 " + category;
            case "Career Growth": return "🚀 " + category;
            case "Leadership": return "🏆 " + category;
            default: return "📚 " + category;
        }
    }
}
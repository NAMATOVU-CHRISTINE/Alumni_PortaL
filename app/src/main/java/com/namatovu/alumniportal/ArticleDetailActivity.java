package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.namatovu.alumniportal.adapters.CommentAdapter;
import com.namatovu.alumniportal.models.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Article Detail Activity - Full view of an article with content, comments and actions
 */
public class ArticleDetailActivity extends AppCompatActivity implements CommentAdapter.OnCommentInteractionListener {

    private TextView tvTitle, tvCategory, tvAuthor, tvDate, tvContent;
    private MaterialButton btnBookmark, btnShare, btnLike, btnSendComment;
    private FloatingActionButton fabShare;
    private RecyclerView recyclerViewComments;
    private EditText etComment;
    
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private boolean isArticleLiked = false;
    private int articleLikeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail_view);

        initializeViews();
        setupToolbar();
        loadArticleData();
        setupActionButtons();
        setupComments();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvDate = findViewById(R.id.tvDate);
        tvContent = findViewById(R.id.tvContent);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        btnLike = findViewById(R.id.btnLike);
        btnSendComment = findViewById(R.id.btnSendComment);
        fabShare = findViewById(R.id.fabShare);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        etComment = findViewById(R.id.etComment);
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
        
        // Initialize article like count from intent
        articleLikeCount = intent.getIntExtra("article_like_count", 0);
        updateLikeButton();
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

        btnLike.setOnClickListener(v -> {
            isArticleLiked = !isArticleLiked;
            if (isArticleLiked) {
                articleLikeCount++;
                Toast.makeText(this, "You liked this article! ❤️", Toast.LENGTH_SHORT).show();
            } else {
                articleLikeCount = Math.max(0, articleLikeCount - 1);
                Toast.makeText(this, "Like removed", Toast.LENGTH_SHORT).show();
            }
            updateLikeButton();
        });

        btnShare.setOnClickListener(v -> shareArticle());
        fabShare.setOnClickListener(v -> shareArticle());

        btnSendComment.setOnClickListener(v -> {
            String commentText = etComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                Comment newComment = new Comment("You", commentText);
                commentAdapter.addComment(newComment);
                etComment.setText("");
                Toast.makeText(this, "Comment added! 💬", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please write a comment first", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void setupComments() {
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments);
        commentAdapter.setOnCommentInteractionListener(this);
        
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void updateLikeButton() {
        String likeText = isArticleLiked ? "❤️ " + articleLikeCount : "🤍 " + articleLikeCount;
        btnLike.setText(likeText);
    }

    // CommentAdapter.OnCommentInteractionListener implementation
    @Override
    public void onLikeComment(Comment comment, int position) {
        comment.toggleLike();
        commentAdapter.updateComment(position, comment);
        
        String message = comment.isLiked() ? "Comment liked! ❤️" : "Like removed";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReplyToComment(Comment comment, int position) {
        etComment.setText("@" + comment.getAuthorName() + " ");
        etComment.setSelection(etComment.getText().length());
        etComment.requestFocus();
    }
}
package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Article;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying articles in the Knowledge section
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private Context context;
    private List<Article> articles;
    private List<Article> filteredArticles;
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
        void onBookmarkClick(Article article);
        void onShareClick(Article article);
        void onLikeClick(Article article);
    }

    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
        this.filteredArticles = new ArrayList<>(articles);
    }

    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article_card, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = filteredArticles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return filteredArticles.size();
    }

    // Filter methods
    public void filter(String query) {
        filteredArticles.clear();
        if (query.isEmpty()) {
            filteredArticles.addAll(articles);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Article article : articles) {
                if (article.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    article.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                    article.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    filteredArticles.add(article);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        filteredArticles.clear();
        if (category.equals("All")) {
            filteredArticles.addAll(articles);
        } else {
            for (Article article : articles) {
                if (article.getCategory().equals(category)) {
                    filteredArticles.add(article);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addArticle(Article article) {
        articles.add(0, article); // Add to beginning
        filteredArticles.add(0, article);
        notifyItemInserted(0);
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles.clear();
        this.articles.addAll(newArticles);
        this.filteredArticles.clear();
        this.filteredArticles.addAll(newArticles);
        notifyDataSetChanged();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvCategory, tvDate, tvTitle, tvDescription, tvAuthor;
        MaterialButton btnLike, btnBookmark, btnShare;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnBookmark = itemView.findViewById(R.id.btnBookmark);
            btnShare = itemView.findViewById(R.id.btnShare);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onArticleClick(filteredArticles.get(position));
                    }
                }
            });

            btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onLikeClick(filteredArticles.get(position));
                    }
                }
            });

            btnBookmark.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onBookmarkClick(filteredArticles.get(position));
                    }
                }
            });

            btnShare.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onShareClick(filteredArticles.get(position));
                    }
                }
            });
        }

        public void bind(Article article) {
            android.util.Log.d("ArticleAdapter", "Binding article: " + article.getTitle());
            
            tvCategoryIcon.setText(article.getCategoryIcon());
            tvCategory.setText(article.getCategory());
            tvDate.setText(article.getFormattedDate());
            tvTitle.setText(article.getTitle());
            tvDescription.setText(article.getDescription());
            
            // Set author
            if (article.getAuthorName() != null && !article.getAuthorName().isEmpty()) {
                tvAuthor.setText("By " + article.getAuthorName());
                tvAuthor.setVisibility(View.VISIBLE);
            } else {
                tvAuthor.setVisibility(View.GONE);
            }

            // Update like button state
            String likeText = article.isLiked() ? "❤️ " + article.getLikeCount() : "🤍 " + article.getLikeCount();
            btnLike.setText(likeText);

            // Update bookmark button state
            if (article.isBookmarked()) {
                btnBookmark.setText("✅");
                btnBookmark.setTextColor(context.getColor(R.color.must_green));
            } else {
                btnBookmark.setText("📖");
                btnBookmark.setTextColor(context.getColor(R.color.must_green));
            }
        }
    }
}
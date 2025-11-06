package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.NewsArticle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    
    private List<NewsArticle> newsArticles = new ArrayList<>();
    private OnNewsClickListener listener;
    
    public interface OnNewsClickListener {
        void onNewsClick(NewsArticle newsArticle);
    }
    
    public void setOnNewsClickListener(OnNewsClickListener listener) {
        this.listener = listener;
    }
    
    public void setNewsArticles(List<NewsArticle> newsArticles) {
        this.newsArticles = newsArticles != null ? newsArticles : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_article, parent, false);
        return new NewsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsArticle article = newsArticles.get(position);
        holder.bind(article);
    }
    
    @Override
    public int getItemCount() {
        return newsArticles.size();
    }
    
    class NewsViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageArticle;
        private TextView textTitle;
        private TextView textSummary;
        private TextView textAuthor;
        private TextView textDate;
        private TextView textCategory;
        
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageArticle = itemView.findViewById(R.id.imageArticle);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSummary = itemView.findViewById(R.id.textSummary);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textDate = itemView.findViewById(R.id.textDate);
            textCategory = itemView.findViewById(R.id.textCategory);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onNewsClick(newsArticles.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(NewsArticle article) {
            if (article == null) return;
            
            textTitle.setText(article.getTitle() != null ? article.getTitle() : "Untitled");
            textSummary.setText(article.getSummary() != null ? article.getSummary() : "No summary available");
            textAuthor.setText(article.getAuthor() != null ? article.getAuthor() : "Alumni Portal");
            textCategory.setText(article.getCategory() != null ? article.getCategory() : "News");
            
            // Format published date
            if (article.getPublishedDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                textDate.setText(sdf.format(article.getPublishedDate()));
            } else {
                textDate.setText("Recently");
            }
            
            // Load image if URL is available
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                // Here you would typically use Glide or similar to load the image
                // For now, we'll use a placeholder
                imageArticle.setImageResource(R.drawable.ic_news);
            } else {
                imageArticle.setImageResource(R.drawable.ic_news);
            }
        }
    }
}
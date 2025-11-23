package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.News;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {
    
    private Context context;
    private List<News> newsList;
    
    public NewsListAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }
    
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.bind(news);
    }
    
    @Override
    public int getItemCount() {
        return newsList.size();
    }
    
    class NewsViewHolder extends RecyclerView.ViewHolder {
        
        private Chip categoryChip;
        private ImageView newsImage;
        private TextView titleText;
        private TextView summaryText;
        private TextView authorText;
        private TextView timeText;
        
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            newsImage = itemView.findViewById(R.id.newsImage);
            titleText = itemView.findViewById(R.id.newsTitle);
            summaryText = itemView.findViewById(R.id.newsSummary);
            authorText = itemView.findViewById(R.id.newsAuthor);
            timeText = itemView.findViewById(R.id.newsTime);
        }
        
        public void bind(News news) {
            // Set category chip
            if (news.getCategory() != null) {
                categoryChip.setText(news.getCategory().getIcon() + " " + news.getCategory().getDisplayName());
            }
            
            // Set title and summary
            titleText.setText(news.getTitle());
            summaryText.setText(news.getSummary());
            
            // Set author
            authorText.setText(news.getAuthor() != null ? news.getAuthor() : "MUST Communications");
            
            // Set time ago
            timeText.setText(news.getTimeAgo());
            
            // Set news image (placeholder for now)
            newsImage.setImageResource(R.drawable.ic_news);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (news.getSourceUrl() != null && !news.getSourceUrl().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getSourceUrl()));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Reading: " + news.getTitle(), Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to news details page
                }
            });
        }
    }
}
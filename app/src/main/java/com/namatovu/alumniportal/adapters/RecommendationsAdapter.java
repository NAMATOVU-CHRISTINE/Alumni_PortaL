package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Recommendation;
import com.namatovu.alumniportal.ProfileActivity;
import com.namatovu.alumniportal.JobsActivity;
import com.namatovu.alumniportal.KnowledgeActivity;
import com.namatovu.alumniportal.MentorshipActivity;

import java.util.List;

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder> {
    
    private List<Recommendation> recommendations;
    private Context context;
    
    public RecommendationsAdapter(Context context, List<Recommendation> recommendations) {
        this.context = context;
        this.recommendations = recommendations;
    }
    
    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new RecommendationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);
        holder.bind(recommendation);
    }
    
    @Override
    public int getItemCount() {
        return recommendations.size();
    }
    
    public void updateRecommendations(List<Recommendation> newRecommendations) {
        // Filter out duplicates based on title and description
        List<Recommendation> uniqueRecommendations = new java.util.ArrayList<>();
        java.util.Set<String> seenKeys = new java.util.HashSet<>();
        
        if (newRecommendations != null) {
            for (Recommendation rec : newRecommendations) {
                // Create a unique key based on title and description
                String key = rec.getTitle() + "|" + rec.getDescription();
                if (!seenKeys.contains(key)) {
                    seenKeys.add(key);
                    uniqueRecommendations.add(rec);
                }
            }
        }
        
        this.recommendations = uniqueRecommendations;
        notifyDataSetChanged();
    }
    
    class RecommendationViewHolder extends RecyclerView.ViewHolder {
        
        private TextView iconView;
        private TextView titleView;
        private TextView descriptionView;
        private TextView arrowView;
        
        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.recommendationIcon);
            titleView = itemView.findViewById(R.id.recommendationTitle);
            descriptionView = itemView.findViewById(R.id.recommendationDescription);
            arrowView = itemView.findViewById(R.id.recommendationArrow);
        }
        
        public void bind(Recommendation recommendation) {
            iconView.setText(recommendation.getIcon());
            titleView.setText(recommendation.getTitle());
            descriptionView.setText(recommendation.getDescription());
            
            // Set click listener to navigate based on recommendation type
            itemView.setOnClickListener(v -> {
                Intent intent = getIntentForRecommendation(recommendation);
                if (intent != null) {
                    context.startActivity(intent);
                }
            });
        }
        
        private Intent getIntentForRecommendation(Recommendation recommendation) {
            switch (recommendation.getType()) {
                case PROFILE_COMPLETION:
                    return new Intent(context, ProfileActivity.class);
                case JOB_OPPORTUNITY:
                    return new Intent(context, JobsActivity.class);
                case SKILL_DEVELOPMENT:
                    return new Intent(context, KnowledgeActivity.class);
                case NETWORKING:
                case MENTORSHIP:
                    return new Intent(context, MentorshipActivity.class);
                default:
                    return new Intent(context, JobsActivity.class);
            }
        }
    }
}
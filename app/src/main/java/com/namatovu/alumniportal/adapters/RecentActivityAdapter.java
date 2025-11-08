package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.RecentActivity;
import java.util.List;

/**
 * RecentActivityAdapter - RecyclerView adapter for recent activities
 */
public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {

    private Context context;
    private List<RecentActivity> activities;

    public RecentActivityAdapter(Context context, List<RecentActivity> activities) {
        this.context = context;
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        RecentActivity activity = activities.get(position);
        
        holder.tvIcon.setText(activity.getIcon());
        holder.tvTitle.setText(activity.getTitle());
        holder.tvDescription.setText(activity.getDescription());
        holder.tvTimeStamp.setText(activity.getTimeStamp());
        
        // Set card background based on read status
        if (activity.isRead()) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.text_secondary));
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.light_green));
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.text_primary));
        }
        
        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            activity.setRead(true);
            notifyItemChanged(position);
            // Handle activity click based on type
            handleActivityClick(activity);
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    private void handleActivityClick(RecentActivity activity) {
        // Handle different types of activities
        switch (activity.getType()) {
            case OPPORTUNITY:
                // Navigate to jobs/opportunities
                break;
            case MESSAGE:
                // Navigate to messages
                break;
            case KNOWLEDGE:
                // Navigate to knowledge base
                break;
            case CONNECTION:
                // Navigate to connections/network
                break;
            case PROFILE:
                // Navigate to profile
                break;
            case ACHIEVEMENT:
                // Show achievement details
                break;
        }
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvIcon, tvTitle, tvDescription, tvTimeStamp;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardActivity);
            tvIcon = itemView.findViewById(R.id.tvActivityIcon);
            tvTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvDescription = itemView.findViewById(R.id.tvActivityDescription);
            tvTimeStamp = itemView.findViewById(R.id.tvActivityTime);
        }
    }
}
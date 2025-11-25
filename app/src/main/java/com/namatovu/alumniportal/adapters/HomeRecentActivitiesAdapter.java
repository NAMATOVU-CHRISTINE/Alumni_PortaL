package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.RecentActivity;

import java.util.List;

public class HomeRecentActivitiesAdapter extends RecyclerView.Adapter<HomeRecentActivitiesAdapter.ActivityViewHolder> {
    
    private List<RecentActivity> activities;
    private Context context;
    
    public HomeRecentActivitiesAdapter(Context context, List<RecentActivity> activities) {
        this.context = context;
        this.activities = activities;
    }
    
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        RecentActivity activity = activities.get(position);
        holder.bind(activity);
    }
    
    @Override
    public int getItemCount() {
        return Math.min(activities.size(), 3); // Limit to 3 items for home page
    }
    
    public void updateActivities(List<RecentActivity> newActivities) {
        // Filter out duplicates based on title and timestamp
        List<RecentActivity> uniqueActivities = new java.util.ArrayList<>();
        java.util.Set<String> seenKeys = new java.util.HashSet<>();
        
        if (newActivities != null) {
            for (RecentActivity activity : newActivities) {
                // Create a unique key based on title and timestamp
                String key = activity.getTitle() + "|" + activity.getTimeStamp();
                if (!seenKeys.contains(key)) {
                    seenKeys.add(key);
                    uniqueActivities.add(activity);
                }
            }
        }
        
        this.activities = uniqueActivities;
        notifyDataSetChanged();
    }
    
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        
        private TextView iconView;
        private TextView titleView;
        private TextView timeView;
        private View unreadIndicator;
        
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.activityIcon);
            titleView = itemView.findViewById(R.id.activityTitle);
            timeView = itemView.findViewById(R.id.activityTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
        
        public void bind(RecentActivity activity) {
            titleView.setText(activity.getTitle());
            timeView.setText(activity.getTimeStamp()); // Use the timestamp string directly
            
            // Set icon based on activity type
            String icon;
            switch (activity.getType()) {
                case OPPORTUNITY:
                    icon = "🎯";
                    break;
                case MESSAGE:
                    icon = "💬";
                    break;
                case CONNECTION:
                    icon = "🤝";
                    break;
                case KNOWLEDGE:
                    icon = "📚";
                    break;
                case PROFILE:
                    icon = "👤";
                    break;
                case ACHIEVEMENT:
                    icon = "🏆";
                    break;
                default:
                    icon = "📈";
                    break;
            }
            iconView.setText(icon);
            
            // Show/hide unread indicator
            unreadIndicator.setVisibility(activity.isRead() ? View.GONE : View.VISIBLE);
        }
    }
}
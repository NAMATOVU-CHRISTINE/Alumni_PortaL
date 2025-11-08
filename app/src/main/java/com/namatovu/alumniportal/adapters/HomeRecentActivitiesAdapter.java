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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        this.activities = newActivities;
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
            timeView.setText(getTimeAgo(activity.getTimestamp()));
            
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
        
        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 60000) { // Less than 1 minute
                return "Just now";
            } else if (diff < 3600000) { // Less than 1 hour
                int minutes = (int) (diff / 60000);
                return minutes + " min ago";
            } else if (diff < 86400000) { // Less than 1 day
                int hours = (int) (diff / 3600000);
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.databinding.ItemNotificationBinding;
import com.namatovu.alumniportal.models.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {
    
    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;
    
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }
    
    public NotificationsAdapter(Context context, List<Notification> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(context), parent, false
        );
        return new NotificationViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }
    
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    
    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ItemNotificationBinding binding;
        
        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(Notification notification) {
            binding.notificationTitle.setText(notification.getTitle());
            binding.notificationMessage.setText(notification.getMessage());
            
            // Format timestamp
            if (notification.getTimestamp() > 0) {
                String timeAgo = getTimeAgo(notification.getTimestamp());
                binding.notificationTime.setText(timeAgo);
            }
            
            // Set read/unread styling
            if (notification.isRead()) {
                binding.getRoot().setAlpha(0.6f);
            } else {
                binding.getRoot().setAlpha(1.0f);
            }
            
            // Handle click
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
        
        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days < 7) {
                return days + "d ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}

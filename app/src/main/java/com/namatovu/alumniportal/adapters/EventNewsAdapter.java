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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventNewsAdapter extends RecyclerView.Adapter<EventNewsAdapter.EventViewHolder> {
    
    private Context context;
    private List<Event> events;
    
    public EventNewsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    class EventViewHolder extends RecyclerView.ViewHolder {
        
        private Chip categoryChip;
        private TextView dateText;
        private ImageView eventImage;
        private TextView titleText;
        private TextView summaryText;
        private TextView locationText;
        private TextView timeText;
        private TextView participantsText;
        private TextView organizerText;
        private MaterialButton actionButton;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryChip = itemView.findViewById(R.id.eventCategoryChip);
            dateText = itemView.findViewById(R.id.eventDate);
            eventImage = itemView.findViewById(R.id.eventImage);
            titleText = itemView.findViewById(R.id.eventTitle);
            summaryText = itemView.findViewById(R.id.eventSummary);
            locationText = itemView.findViewById(R.id.eventLocation);
            timeText = itemView.findViewById(R.id.eventTime);
            participantsText = itemView.findViewById(R.id.participantsInfo);
            organizerText = itemView.findViewById(R.id.organizerInfo);
            actionButton = itemView.findViewById(R.id.eventActionBtn);
        }
        
        public void bind(Event event) {
            // Set category chip
            if (event.getCategory() != null) {
                categoryChip.setText(event.getCategory().getIcon() + " " + event.getCategory().getDisplayName());
            }
            
            // Set formatted date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateText.setText(dateFormat.format(new Date(event.getDateTime())));
            
            // Set title and summary
            titleText.setText(event.getTitle());
            summaryText.setText(event.getSummary());
            
            // Set location
            String location = event.getLocation();
            if (event.isOnline()) {
                location = "🌐 Online Event";
            }
            locationText.setText(location);
            
            // Set time
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            timeText.setText(timeFormat.format(new Date(event.getDateTime())));
            
            // Set participants info
            participantsText.setText(event.getCurrentParticipants() + "/" + event.getMaxParticipants() + " participants");
            
            // Set organizer
            organizerText.setText("by " + (event.getOrganizerName() != null ? event.getOrganizerName() : "MUST Alumni"));
            
            // Set action button based on event status
            setupActionButton(event);
            
            // Set click listener for entire item
            itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Navigate to event details page
            });
        }
        
        private void setupActionButton(Event event) {
            if (event.isExpired()) {
                actionButton.setText("Event Completed");
                actionButton.setEnabled(false);
                actionButton.setAlpha(0.6f);
            } else if (event.isAvailableForRegistration()) {
                actionButton.setText("Register Now");
                actionButton.setEnabled(true);
                actionButton.setAlpha(1.0f);
                actionButton.setOnClickListener(v -> {
                    if (event.getRegistrationUrl() != null && !event.getRegistrationUrl().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getRegistrationUrl()));
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Registration for " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                actionButton.setText("Event Full");
                actionButton.setEnabled(false);
                actionButton.setAlpha(0.6f);
            }
        }
    }
}
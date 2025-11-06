package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.AlumniEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    
    private List<AlumniEvent> events = new ArrayList<>();
    private OnEventClickListener listener;
    
    public interface OnEventClickListener {
        void onEventClick(AlumniEvent event);
    }
    
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }
    
    public void setEvents(List<AlumniEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alumni_event, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        AlumniEvent event = events.get(position);
        holder.bind(event);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textDescription;
        private TextView textDate;
        private TextView textLocation;
        private TextView textCategory;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            // Use generic text views since we don't have the exact layout
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textDate = itemView.findViewById(R.id.textDate);
            textLocation = itemView.findViewById(R.id.textLocation);
            textCategory = itemView.findViewById(R.id.textCategory);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEventClick(events.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(AlumniEvent event) {
            if (event == null) return;
            
            if (textTitle != null) {
                textTitle.setText(event.getTitle() != null ? event.getTitle() : "Untitled Event");
            }
            
            if (textDescription != null) {
                textDescription.setText(event.getDescription() != null ? event.getDescription() : "No description available");
            }
            
            if (textLocation != null) {
                textLocation.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");
            }
            
            if (textCategory != null) {
                textCategory.setText(event.getCategory() != null ? event.getCategory() : "General");
            }
            
            // Format event date
            if (textDate != null && event.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                textDate.setText(sdf.format(event.getDate()));
            } else if (textDate != null) {
                textDate.setText("Date TBD");
            }
        }
    }
}
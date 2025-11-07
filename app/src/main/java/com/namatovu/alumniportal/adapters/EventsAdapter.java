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
    private OnEventActionListener listener;
    private String currentUserId;
    
    // Default constructor
    public EventsAdapter() {
    }
    
    // Constructor with parameters
    public EventsAdapter(List<AlumniEvent> events, String currentUserId, OnEventActionListener listener) {
        this.events = events != null ? events : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.listener = listener;
    }
    
    public interface OnEventActionListener {
        void onEventClick(AlumniEvent event);
        void onRegisterClick(AlumniEvent event);
        void onShareClick(AlumniEvent event);
        void onAddToCalendarClick(AlumniEvent event);
    }
    
    public void setOnEventActionListener(OnEventActionListener listener) {
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
        private View buttonRegister;
        private View buttonShare;
        private View buttonAddToCalendar;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textDate = itemView.findViewById(R.id.textDate);
            textLocation = itemView.findViewById(R.id.textLocation);
            textCategory = itemView.findViewById(R.id.textCategory);
            buttonRegister = itemView.findViewById(R.id.buttonRegister);
            buttonShare = itemView.findViewById(R.id.buttonShare);
            buttonAddToCalendar = itemView.findViewById(R.id.buttonAddToCalendar);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEventClick(events.get(getAdapterPosition()));
                }
            });

            if (buttonRegister != null) {
                buttonRegister.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onRegisterClick(events.get(getAdapterPosition()));
                    }
                });
            }

            if (buttonShare != null) {
                buttonShare.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onShareClick(events.get(getAdapterPosition()));
                    }
                });
            }

            if (buttonAddToCalendar != null) {
                buttonAddToCalendar.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onAddToCalendarClick(events.get(getAdapterPosition()));
                    }
                });
            }
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
                textLocation.setText(event.getVenueDisplayText());
            }
            
            if (textCategory != null) {
                textCategory.setText(event.getEventTypeDisplayText());
            }
            
            // Format event date
            if (textDate != null && event.getStartDateTime() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                textDate.setText(sdf.format(event.getStartDateTime()));
            } else if (textDate != null) {
                textDate.setText("Date TBD");
            }
        }
    }
}
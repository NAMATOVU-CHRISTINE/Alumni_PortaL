package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            checkRegistrationStatus(event);
            
            // Set click listener for entire item
            itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Navigate to event details page
            });
        }
        
        private void checkRegistrationStatus(Event event) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Get current user email from Firebase Auth
            com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                setupActionButton(event, false);
                return;
            }
            
            String userEmail = auth.getCurrentUser().getEmail();
            
            // Check if user already registered
            db.collection("event_registrations")
                .whereEqualTo("eventId", event.getId())
                .whereEqualTo("participantEmail", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean isRegistered = !querySnapshot.isEmpty();
                    setupActionButton(event, isRegistered);
                })
                .addOnFailureListener(e -> {
                    setupActionButton(event, false);
                });
        }
        
        private void setupActionButton(Event event, boolean isRegistered) {
            if (isRegistered) {
                actionButton.setText("✓ Already Registered");
                actionButton.setEnabled(false);
                actionButton.setAlpha(0.8f);
            } else if (event.isExpired()) {
                actionButton.setText("Event Completed");
                actionButton.setEnabled(false);
                actionButton.setAlpha(0.6f);
            } else if (event.isAvailableForRegistration()) {
                actionButton.setText("Register Now");
                actionButton.setEnabled(true);
                actionButton.setAlpha(1.0f);
                actionButton.setOnClickListener(v -> showRegistrationForm(event));
            } else {
                actionButton.setText("Event Full");
                actionButton.setEnabled(false);
                actionButton.setAlpha(0.6f);
            }
        }
        
        private void showRegistrationForm(Event event) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Register for " + event.getTitle());
            
            // Create form layout
            View formView = LayoutInflater.from(context).inflate(R.layout.dialog_event_registration, null);
            
            TextInputEditText nameInput = formView.findViewById(R.id.registrationName);
            TextInputEditText emailInput = formView.findViewById(R.id.registrationEmail);
            TextInputEditText phoneInput = formView.findViewById(R.id.registrationPhone);
            
            builder.setView(formView);
            builder.setPositiveButton("Register", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                
                if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                saveRegistration(event, name, email, phone);
            });
            
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            
            builder.show();
        }
        
        private void saveRegistration(Event event, String name, String email, String phone) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            Map<String, Object> registration = new HashMap<>();
            registration.put("eventId", event.getId());
            registration.put("eventTitle", event.getTitle());
            registration.put("participantName", name);
            registration.put("participantEmail", email);
            registration.put("participantPhone", phone);
            registration.put("registeredAt", System.currentTimeMillis());
            registration.put("eventDateTime", event.getDateTime());
            
            db.collection("event_registrations")
                .add(registration)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(context, "Successfully registered for " + event.getTitle(), Toast.LENGTH_LONG).show();
                    // Update participant count
                    event.setCurrentParticipants(event.getCurrentParticipants() + 1);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }
}
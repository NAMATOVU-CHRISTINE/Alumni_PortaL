package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.databinding.ActivityEventDetailsBinding;
import com.namatovu.alumniportal.models.AlumniEvent;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {
    private ActivityEventDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String eventId;
    private AlumniEvent event;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        loadEventDetails();
        setupButtons();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Event Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadEventDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        event = documentSnapshot.toObject(AlumniEvent.class);
                        if (event != null) {
                            event.setEventId(documentSnapshot.getId());
                            displayEventDetails();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentLayout.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    finish();
                });
    }

    private void displayEventDetails() {
        binding.textTitle.setText(event.getTitle());
        binding.textDescription.setText(event.getDescription());
        binding.textDate.setText(event.getFormattedDate());
        binding.textTime.setText(event.getFormattedTime());
        binding.textVenue.setText(event.getVenueDisplayText());
        binding.textEventType.setText(event.getEventTypeDisplayText());
        binding.textOrganizer.setText(event.getOrganizerName() != null ? event.getOrganizerName() : "MUST");
        binding.textAttendees.setText(event.getAttendanceText());

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            binding.imageEvent.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.ic_event)
                    .error(R.drawable.ic_event)
                    .into(binding.imageEvent);
        } else {
            binding.imageEvent.setVisibility(View.GONE);
        }

        updateRegisterButton();
    }

    private void setupButtons() {
        binding.buttonRegister.setOnClickListener(v -> handleRegistration());
        binding.buttonShare.setOnClickListener(v -> shareEvent());
        binding.buttonAddToCalendar.setOnClickListener(v -> addToCalendar());
    }

    private void updateRegisterButton() {
        if (event.isUserAttending(currentUserId)) {
            binding.buttonRegister.setText("Registered");
            binding.buttonRegister.setEnabled(false);
        } else if (!event.hasSpaceAvailable()) {
            binding.buttonRegister.setText("Event Full");
            binding.buttonRegister.setEnabled(false);
        } else {
            binding.buttonRegister.setText("Register");
            binding.buttonRegister.setEnabled(true);
        }
    }

    private void handleRegistration() {
        if (event.getRegistrationUrl() != null && !event.getRegistrationUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(event.getRegistrationUrl()));
            startActivity(intent);
        } else {
            registerForEvent();
        }
    }

    private void registerForEvent() {
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Please login to register", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> attendeeIds = event.getAttendeeIds();
        if (attendeeIds == null) {
            attendeeIds = new ArrayList<>();
        }
        attendeeIds.add(currentUserId);
        final List<String> finalAttendeeIds = attendeeIds;

        db.collection("events").document(eventId)
                .update("attendeeIds", finalAttendeeIds, "currentAttendees", event.getCurrentAttendees() + 1)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                    event.setAttendeeIds(finalAttendeeIds);
                    event.incrementAttendees();
                    updateRegisterButton();
                    binding.textAttendees.setText(event.getAttendanceText());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void shareEvent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this event: " + event.getTitle());
        
        String shareText = event.getTitle() + "\n" +
                          event.getFormattedDateTime() + "\n" +
                          event.getVenueDisplayText() + "\n\n" +
                          event.getDescription();
        
        if (event.getRegistrationUrl() != null && !event.getRegistrationUrl().isEmpty()) {
            shareText += "\n\nMore info: " + event.getRegistrationUrl();
        }
        
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Event"));
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(android.provider.CalendarContract.Events.CONTENT_URI);
        intent.putExtra(android.provider.CalendarContract.Events.TITLE, event.getTitle());
        intent.putExtra(android.provider.CalendarContract.Events.DESCRIPTION, event.getDescription());
        intent.putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, event.getVenueDisplayText());
        intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartDateTime());
        intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, event.getEndDateTime());
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No calendar app found", Toast.LENGTH_SHORT).show();
        }
    }
}

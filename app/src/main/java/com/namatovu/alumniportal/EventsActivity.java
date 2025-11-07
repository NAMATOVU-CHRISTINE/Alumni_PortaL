package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.adapters.EventsAdapter;
import com.namatovu.alumniportal.databinding.ActivityEventsBinding;
import com.namatovu.alumniportal.models.AlumniEvent;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventsActivity extends AppCompatActivity {
    private static final String TAG = "EventsActivity";
    
    private ActivityEventsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EventsAdapter adapter;
    private List<AlumniEvent> allEvents;
    private List<AlumniEvent> filteredEvents;
    private String currentTab = "upcoming"; // "upcoming", "past", "my_events"
    private String searchQuery = "";
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("EventsActivity", "HomeActivity");

        // Initialize lists
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();

        setupToolbar();
        setupTabs();
        setupSearch();
        setupRecyclerView();
        setupFAB();
        loadEvents();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alumni Events");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTabs() {
        // TODO: Add TabLayout to activity_events.xml layout if needed
        // For now, default to showing upcoming events
        currentTab = "upcoming";
    }

    private void setupSearch() {
        // TODO: Add search functionality when search UI is added to layout
        searchQuery = "";
    }

    private void setupRecyclerView() {
        adapter = new EventsAdapter(filteredEvents, currentUserId, new EventsAdapter.OnEventActionListener() {
            @Override
            public void onEventClick(AlumniEvent event) {
                Intent intent = new Intent(EventsActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onRegisterClick(AlumniEvent event) {
                handleEventRegistration(event);
            }

            @Override
            public void onShareClick(AlumniEvent event) {
                shareEvent(event);
            }

            @Override
            public void onAddToCalendarClick(AlumniEvent event) {
                addEventToCalendar(event);
            }
        });
        
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.eventsRecyclerView.setAdapter(adapter);
    }

    private void setupFAB() {
        // TODO: Add FAB to layout if needed for creating events
        // For now, events can be created through other UI flows
    }

    private void loadEvents() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyTextView.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("isPublic", true)
                .orderBy("startDateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AlumniEvent event = document.toObject(AlumniEvent.class);
                        event.setEventId(document.getId());
                        allEvents.add(event);
                    }
                    
                    binding.progressBar.setVisibility(View.GONE);
                    filterEvents();
                    
                    Log.d(TAG, "Loaded " + allEvents.size() + " events");
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.emptyTextView.setVisibility(View.VISIBLE);
                    binding.emptyTextView.setText("Failed to load events");
                    
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading events", e);
                    
                    AnalyticsHelper.logError("events_load_failed", e.getMessage(), "EventsActivity");
                });
    }

    private void filterEvents() {
        filteredEvents.clear();
        long now = System.currentTimeMillis();
        
        for (AlumniEvent event : allEvents) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                    (event.getTitle() != null && event.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (event.getVenue() != null && event.getVenue().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (event.getEventType() != null && event.getEventType().toLowerCase().contains(searchQuery.toLowerCase()));
            
            boolean matchesTab = false;
            switch (currentTab) {
                case "upcoming":
                    matchesTab = event.getStartDateTime() > now;
                    break;
                case "past":
                    matchesTab = event.getEndDateTime() < now;
                    break;
                case "my_events":
                    matchesTab = currentUserId.equals(event.getOrganizer()) || 
                               (event.getAttendeeIds() != null && event.getAttendeeIds().contains(currentUserId));
                    break;
            }
            
            if (matchesSearch && matchesTab) {
                filteredEvents.add(event);
            }
        }
        
        // Sort by date
        filteredEvents.sort((e1, e2) -> {
            if ("past".equals(currentTab)) {
                return Long.compare(e2.getStartDateTime(), e1.getStartDateTime()); // Newest first for past events
            } else {
                return Long.compare(e1.getStartDateTime(), e2.getStartDateTime()); // Earliest first for upcoming
            }
        });
        
        adapter.notifyDataSetChanged();
        
        // Show/hide no events message
        if (filteredEvents.isEmpty()) {
            binding.emptyTextView.setVisibility(View.VISIBLE);
            binding.emptyTextView.setText(getEmptyMessage());
        } else {
            binding.emptyTextView.setVisibility(View.GONE);
        }
        
        // TODO: Add event count display to layout if needed
        // Currently showing count: filteredEvents.size() + " events"
    }

    private String getEmptyMessage() {
        switch (currentTab) {
            case "upcoming":
                return "No upcoming events. Check back later!";
            case "past":
                return "No past events to show.";
            case "my_events":
                return "You haven't created or registered for any events yet.";
            default:
                return "No events found.";
        }
    }

    private void handleEventRegistration(AlumniEvent event) {
        if (event.isRequiresRegistration() && event.getRegistrationUrl() != null) {
            // Open external registration URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(event.getRegistrationUrl()));
            startActivity(intent);
        } else {
            // Handle internal registration
            registerForEvent(event);
        }
    }

    private void registerForEvent(AlumniEvent event) {
        if (event.getEventId() == null || currentUserId.isEmpty()) return;
        
        if (event.isUserAttending(currentUserId)) {
            Toast.makeText(this, "You're already registered for this event", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!event.hasSpaceAvailable()) {
            Toast.makeText(this, "This event is full", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Add user to attendees list
        List<String> attendeeIds = event.getAttendeeIds();
        if (attendeeIds == null) {
            attendeeIds = new ArrayList<>();
        }
        attendeeIds.add(currentUserId);
        
        db.collection("events").document(event.getEventId())
                .update("attendeeIds", attendeeIds, "currentAttendees", event.getCurrentAttendees() + 1)
                .addOnSuccessListener(aVoid -> {
                    event.setAttendeeIds(attendeeIds);
                    event.incrementAttendees();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Successfully registered for event!", Toast.LENGTH_SHORT).show();
                    
                    // Log analytics
                    AnalyticsHelper.logNavigation("event_registration", event.getTitle());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to register for event", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to register for event", e);
                });
    }

    private void shareEvent(AlumniEvent event) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this event: " + event.getTitle());
        
        String shareText = event.getTitle() + "\n" +
                          event.getFormattedDateTime() + "\n" +
                          event.getVenueDisplayText() + "\n\n" +
                          event.getDescription();
        
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Event"));
    }

    private void addEventToCalendar(AlumniEvent event) {
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadEvents();
    }
}
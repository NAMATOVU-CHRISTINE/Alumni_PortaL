package com.namatovu.alumniportal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.adapters.NotificationsAdapter;
import com.namatovu.alumniportal.databinding.ActivityNotificationsBinding;
import com.namatovu.alumniportal.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    
    private static final String TAG = "NotificationsActivity";
    private ActivityNotificationsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NotificationsAdapter adapter;
    private List<Notification> notificationsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        notificationsList = new ArrayList<>();
        
        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(this, notificationsList, notification -> {
            handleNotificationClick(notification);
        });
        binding.notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.notificationsRecyclerView.setAdapter(adapter);
    }
    
    private void loadNotifications() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error loading notifications", error);
                    Toast.makeText(NotificationsActivity.this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (querySnapshot != null) {
                    notificationsList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notification.setId(doc.getId());
                                notificationsList.add(notification);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing notification", e);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            });
    }
    
    private void updateEmptyState() {
        if (notificationsList.isEmpty()) {
            binding.emptyStateText.setVisibility(View.VISIBLE);
            binding.notificationsRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyStateText.setVisibility(View.GONE);
            binding.notificationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void handleNotificationClick(Notification notification) {
        // Mark as read
        if (mAuth.getCurrentUser() != null && notification.getId() != null) {
            db.collection("notifications").document(notification.getId())
                .update("read", true)
                .addOnFailureListener(e -> Log.e(TAG, "Error marking notification as read", e));
        }
        
        // Navigate based on notification type
        String type = notification.getType();
        String referenceId = notification.getReferenceId();
        
        Intent intent = null;
        
        if ("message".equalsIgnoreCase(type) || "chat".equalsIgnoreCase(type)) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatId", referenceId);
            intent.putExtra("otherUserId", referenceId);
        } else if ("mentorship_request".equalsIgnoreCase(type)) {
            intent = new Intent(this, com.namatovu.alumniportal.MentorshipActivity.class);
            intent.putExtra("mentorship_id", referenceId);
        } else if ("event".equalsIgnoreCase(type)) {
            intent = new Intent(this, com.namatovu.alumniportal.EventDetailsActivity.class);
            intent.putExtra("eventId", referenceId);
        } else if ("job".equalsIgnoreCase(type)) {
            intent = new Intent(this, com.namatovu.alumniportal.JobDetailActivity.class);
            intent.putExtra("jobId", referenceId);
        }
        
        if (intent != null) {
            startActivity(intent);
        }
    }
}

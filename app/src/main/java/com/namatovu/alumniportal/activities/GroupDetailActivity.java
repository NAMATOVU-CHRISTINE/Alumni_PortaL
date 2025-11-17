package com.namatovu.alumniportal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.AlumniGroup;

import java.util.HashMap;
import java.util.Map;

public class GroupDetailActivity extends AppCompatActivity {
    
    private ImageView imageViewGroup;
    private TextView textViewGroupName;
    private TextView textViewDescription;
    private TextView textViewMemberCount;
    private TextView textViewGroupType;
    private TextView textViewCreator;
    private Button buttonJoinLeave;
    private Button buttonViewMembers;
    private Button buttonGroupChat;
    private ProgressBar progressBar;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String groupId;
    private String currentUserId;
    private AlumniGroup currentGroup;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            finish();
            return;
        }
        
        initViews();
        loadGroupDetails();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Group Details");
        }
    }
    
    private void initViews() {
        imageViewGroup = findViewById(R.id.imageViewGroup);
        textViewGroupName = findViewById(R.id.textViewGroupName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewMemberCount = findViewById(R.id.textViewMemberCount);
        textViewGroupType = findViewById(R.id.textViewGroupType);
        textViewCreator = findViewById(R.id.textViewCreator);
        buttonJoinLeave = findViewById(R.id.buttonJoinLeave);
        buttonViewMembers = findViewById(R.id.buttonViewMembers);
        buttonGroupChat = findViewById(R.id.buttonGroupChat);
        progressBar = findViewById(R.id.progressBar);
        
        buttonJoinLeave.setOnClickListener(v -> toggleMembership());
        buttonViewMembers.setOnClickListener(v -> viewMembers());
        buttonGroupChat.setOnClickListener(v -> openGroupChat());
    }
    
    private void loadGroupDetails() {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentGroup = doc.toObject(AlumniGroup.class);
                        if (currentGroup != null) {
                            currentGroup.setGroupId(groupId);
                            displayGroupInfo();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading group", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void displayGroupInfo() {
        textViewGroupName.setText(currentGroup.getGroupName());
        textViewDescription.setText(currentGroup.getDescription());
        textViewMemberCount.setText(currentGroup.getMemberCount() + " members");
        textViewGroupType.setText(currentGroup.getGroupType().toUpperCase());
        textViewCreator.setText("Created by " + currentGroup.getCreatorName());
        
        // Load group image
        if (currentGroup.getImageUrl() != null && !currentGroup.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentGroup.getImageUrl())
                    .placeholder(R.drawable.ic_group)
                    .into(imageViewGroup);
        }
        
        // Update join/leave button
        if (currentGroup.isMember(currentUserId)) {
            buttonJoinLeave.setText("Leave Group");
            buttonJoinLeave.setBackgroundColor(getColor(android.R.color.holo_red_light));
            buttonGroupChat.setVisibility(View.VISIBLE);
        } else {
            buttonJoinLeave.setText("Join Group");
            buttonJoinLeave.setBackgroundColor(getColor(R.color.colorPrimary));
            buttonGroupChat.setVisibility(View.GONE);
        }
    }
    
    private void toggleMembership() {
        if (currentGroup.isMember(currentUserId)) {
            leaveGroup();
        } else {
            joinGroup();
        }
    }
    
    private void joinGroup() {
        progressBar.setVisibility(View.VISIBLE);
        buttonJoinLeave.setEnabled(false);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("memberIds", FieldValue.arrayUnion(currentUserId));
        updates.put("memberCount", FieldValue.increment(1));
        updates.put("lastActivityAt", System.currentTimeMillis());
        
        db.collection("groups").document(groupId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Joined group successfully!", Toast.LENGTH_SHORT).show();
                    loadGroupDetails();
                    buttonJoinLeave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonJoinLeave.setEnabled(true);
                    Toast.makeText(this, "Error joining group", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void leaveGroup() {
        progressBar.setVisibility(View.VISIBLE);
        buttonJoinLeave.setEnabled(false);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("memberIds", FieldValue.arrayRemove(currentUserId));
        updates.put("memberCount", FieldValue.increment(-1));
        
        db.collection("groups").document(groupId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Left group", Toast.LENGTH_SHORT).show();
                    loadGroupDetails();
                    buttonJoinLeave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonJoinLeave.setEnabled(true);
                    Toast.makeText(this, "Error leaving group", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void viewMembers() {
        // TODO: Implement view members activity
        Toast.makeText(this, "View members coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openGroupChat() {
        Intent intent = new Intent(this, com.namatovu.alumniportal.activities.ChatActivity.class);
        intent.putExtra("chatId", "group_" + groupId);
        intent.putExtra("chatType", "group");
        intent.putExtra("chatName", currentGroup.getGroupName());
        startActivity(intent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

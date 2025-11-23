package com.namatovu.alumniportal;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.adapters.ChatAdapter;
import com.namatovu.alumniportal.databinding.ActivityChatBinding;
import com.namatovu.alumniportal.models.ChatMessage;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.services.NotificationService;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    
    private ActivityChatBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference chatRef;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private NotificationService notificationService;
    
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    private String connectionId;
    private String chatRoomId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            binding = ActivityChatBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            
            Log.d(TAG, "ChatActivity onCreate started");
            
            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            notificationService = new NotificationService(this);
            
            // Get current user ID
            currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
            Log.d(TAG, "Current user ID: " + currentUserId);
            
            // Get data from intent
            connectionId = getIntent().getStringExtra("connectionId");
            otherUserId = getIntent().getStringExtra("otherUserId");
            otherUserName = getIntent().getStringExtra("otherUserName");
            
            Log.d(TAG, "Intent data - connectionId: " + connectionId + ", otherUserId: " + otherUserId + ", otherUserName: " + otherUserName);
            
            if (TextUtils.isEmpty(connectionId) || TextUtils.isEmpty(otherUserId)) {
                Log.e(TAG, "Invalid chat parameters - connectionId or otherUserId is empty");
                Toast.makeText(this, "Invalid chat parameters", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            if (TextUtils.isEmpty(currentUserId)) {
                Log.e(TAG, "Current user not authenticated");
                Toast.makeText(this, "Please log in to use chat", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Create chat room ID (consistent ordering of user IDs)
            chatRoomId = createChatRoomId(currentUserId, otherUserId);
            Log.d(TAG, "Chat room ID created: " + chatRoomId);
            
            setupUI();
            setupChat();
            loadCurrentUserInfo();
            
            // Log analytics
            AnalyticsHelper.logMentorConnection("chat_opened", otherUserId);
            
            Log.d(TAG, "ChatActivity onCreate completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in ChatActivity onCreate", e);
            Toast.makeText(this, "Error opening chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void setupUI() {
        try {
            Log.d(TAG, "Setting up UI");
            
            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            
            // Set chat header with other user's name
            binding.textViewChatName.setText(otherUserName != null ? otherUserName : "User");
            binding.textViewOnlineStatus.setText("Active now");
            
            // Setup RecyclerView
            adapter = new ChatAdapter(messages, currentUserId);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerView.setAdapter(adapter);
            
            Log.d(TAG, "RecyclerView setup complete");
            
            // Setup send button
            binding.buttonSend.setOnClickListener(v -> sendMessage());
            
            // Setup input field
            binding.editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            });
            
            Log.d(TAG, "UI setup complete");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in setupUI", e);
            Toast.makeText(this, "Error setting up chat UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupChat() {
        try {
            // Initialize Firebase Realtime Database reference
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            
            // Enable offline persistence for better reliability
            try {
                database.setPersistenceEnabled(true);
            } catch (Exception e) {
                Log.w(TAG, "Persistence already enabled or not available", e);
            }
            
            chatRef = database.getReference("chats").child(chatRoomId).child("messages");
            
            Log.d(TAG, "Setting up chat for room: " + chatRoomId);
            
            // Listen for new messages
            chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Messages received: " + dataSnapshot.getChildrenCount());
                    messages.clear();
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        try {
                            ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                            if (message != null) {
                                messages.add(message);
                                Log.d(TAG, "Message loaded: " + message.getMessageText());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing message", e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
                    }
                    updateEmptyState();
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Failed to load messages: " + databaseError.getMessage(), databaseError.toException());
                    Toast.makeText(ChatActivity.this, "Failed to load messages: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up chat", e);
            Toast.makeText(this, "Error initializing chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadCurrentUserInfo() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            adapter.setCurrentUserName(currentUser.getFullName());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load current user info", e);
                });
    }
    
    private void sendMessage() {
        String messageText = binding.editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (chatRef == null) {
            Toast.makeText(this, "Chat not initialized. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot send message - chatRef is null");
            return;
        }
        
        // Create new message
        ChatMessage message = new ChatMessage();
        message.setSenderId(currentUserId);
        message.setReceiverId(otherUserId);
        message.setMessageText(messageText);
        message.setTimestamp(System.currentTimeMillis());
        message.setChatId(chatRoomId);
        
        Log.d(TAG, "Sending message: " + messageText + " to room: " + chatRoomId);
        
        // Send to Firebase Realtime Database
        chatRef.push().setValue(message.toMap())
                .addOnSuccessListener(aVoid -> {
                    binding.editTextMessage.setText("");
                    Log.d(TAG, "Message sent successfully");
                    
                    // Send notification to confirm message was sent
                    notificationService.sendMessageSentNotification(messageText, otherUserName);
                    
                    // Update last message in chat room info
                    updateChatRoomInfo(message);
                    
                    // Log analytics
                    AnalyticsHelper.logMentorConnection("message_sent", otherUserId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to send message: " + e.getMessage(), e);
                });
    }
    
    private void updateChatRoomInfo(ChatMessage lastMessage) {
        try {
            Map<String, Object> chatRoomInfo = new HashMap<>();
            chatRoomInfo.put("connectionId", connectionId);
            chatRoomInfo.put("user1Id", currentUserId);
            chatRoomInfo.put("user2Id", otherUserId);
            chatRoomInfo.put("lastMessage", lastMessage.getMessageText());
            chatRoomInfo.put("lastMessageTime", lastMessage.getTimestamp());
            chatRoomInfo.put("lastSenderId", currentUserId);
            
            Log.d(TAG, "Updating chat room info for: " + chatRoomId);
            
            FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatRoomId)
                    .child("info")
                    .setValue(chatRoomInfo)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat room info updated"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update chat room info", e));
        } catch (Exception e) {
            Log.e(TAG, "Error updating chat room info", e);
        }
    }
    
    private void updateEmptyState() {
        if (messages.isEmpty()) {
            // TODO: Add empty state TextView to layout
            // binding.textEmptyState.setVisibility(View.VISIBLE);
            // binding.textEmptyState.setText("Start your mentorship conversation here!");
        } else {
            // binding.textEmptyState.setVisibility(View.GONE);
        }
    }
    
    private String createChatRoomId(String userId1, String userId2) {
        // Create consistent chat room ID regardless of parameter order
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRef != null) {
            chatRef.removeEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }
}
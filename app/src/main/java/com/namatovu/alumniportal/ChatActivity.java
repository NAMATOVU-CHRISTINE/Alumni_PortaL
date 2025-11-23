package com.namatovu.alumniportal;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
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
            updateCurrentUserActivity();
            
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
            
            // Load other user's profile picture and online status
            loadOtherUserProfile();
            updateOnlineStatus();
            
            // Setup RecyclerView
            adapter = new ChatAdapter(messages, currentUserId);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerView.setAdapter(adapter);
            
            Log.d(TAG, "RecyclerView setup complete");
            
            // Setup send button
            binding.buttonSend.setOnClickListener(v -> sendMessage());
            
            // Setup input field with text watcher to enable/disable send button
            binding.editTextMessage.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    binding.buttonSend.setEnabled(s.toString().trim().length() > 0);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
            
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
            Log.d(TAG, "Setting up chat for room: " + chatRoomId);
            
            // Listen for new messages from Firestore
            db.collection("chats").document(chatRoomId).collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Failed to load messages: " + error.getMessage(), error);
                        Toast.makeText(ChatActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        Log.d(TAG, "Messages received: " + querySnapshot.size());
                        messages.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot messageDoc : querySnapshot.getDocuments()) {
                            try {
                                ChatMessage message = messageDoc.toObject(ChatMessage.class);
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
    
    private void loadCurrentUserNameForNotification(String userId, UserNameCallback callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getFullName() != null) {
                            callback.onNameLoaded(user.getFullName());
                        } else {
                            callback.onNameLoaded("User");
                        }
                    } else {
                        callback.onNameLoaded("User");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user name for notification", e);
                    callback.onNameLoaded("User");
                });
    }
    
    private interface UserNameCallback {
        void onNameLoaded(String name);
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
                    
                    // Update user activity
                    updateCurrentUserActivity();
                    
                    // Send notification to confirm message was sent to sender
                    notificationService.sendMessageSentNotification(messageText, otherUserName);
                    
                    // Send notification to recipient about incoming message
                    loadCurrentUserNameForNotification(currentUserId, senderName -> {
                        notificationService.sendIncomingMessageNotification(otherUserId, senderName, messageText);
                    });
                    
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
    
    private void loadOtherUserProfile() {
        if (otherUserId == null) return;

        // Use real-time listener to get profile image updates
        db.collection("users").document(otherUserId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                        binding.imageViewProfile.setImageResource(R.drawable.ic_person);
                        Log.d(TAG, "Profile image not found, using default");
                        return;
                    }
                    
                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(binding.imageViewProfile);
                        Log.d(TAG, "Profile image loaded: " + profileImageUrl);
                    } else {
                        binding.imageViewProfile.setImageResource(R.drawable.ic_person);
                    }
                });
    }
    
    private void updateOnlineStatus() {
        if (otherUserId == null) {
            binding.textViewOnlineStatus.setText("Offline");
            return;
        }

        // Check user's online status with real-time listener
        db.collection("users").document(otherUserId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                        binding.textViewOnlineStatus.setText("Offline");
                        return;
                    }

                    Long lastActive = documentSnapshot.getLong("lastActive");

                    if (lastActive != null) {
                        long diff = System.currentTimeMillis() - lastActive;
                        long minutes = diff / (60 * 1000);
                        long hours = diff / (60 * 60 * 1000);
                        long days = diff / (24 * 60 * 60 * 1000);
                        
                        // If active within last 5 minutes, show as active
                        if (minutes < 5) {
                            binding.textViewOnlineStatus.setText("Active now");
                        } else if (minutes < 60) {
                            binding.textViewOnlineStatus.setText("Active " + minutes + "m ago");
                        } else if (hours < 24) {
                            binding.textViewOnlineStatus.setText("Active " + hours + "h ago");
                        } else if (days < 7) {
                            binding.textViewOnlineStatus.setText("Active " + days + "d ago");
                        } else {
                            binding.textViewOnlineStatus.setText("Offline");
                        }
                    } else {
                        binding.textViewOnlineStatus.setText("Offline");
                    }
                });
    }
    
    private void updateCurrentUserActivity() {
        if (currentUserId == null || currentUserId.isEmpty()) return;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastActive", System.currentTimeMillis());
        
        db.collection("users").document(currentUserId)
                .update(updates)
                .addOnFailureListener(e -> Log.w(TAG, "Failed to update user activity", e));
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
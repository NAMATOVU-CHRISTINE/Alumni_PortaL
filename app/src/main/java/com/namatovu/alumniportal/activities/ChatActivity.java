package com.namatovu.alumniportal.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.ProfileActivity;
import com.namatovu.alumniportal.adapters.ChatMessageAdapter;
import com.namatovu.alumniportal.models.Chat;
import com.namatovu.alumniportal.models.ChatMessage;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import com.namatovu.alumniportal.utils.SecurityHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements ChatMessageAdapter.OnMessageClickListener {
    private static final String TAG = "ChatActivity";
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    
    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonAttach;
    private TextView textViewTyping;
    private ImageView imageViewProfile;
    private TextView textViewChatName;
    private TextView textViewOnlineStatus;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private String currentUserId;
    private String currentUserName;
    private String chatId;
    private String chatType;
    private String otherUserId;
    private String otherUserName;
    private String chatName;
    
    private List<ChatMessage> messages = new ArrayList<>();
    private ListenerRegistration messagesListener;
    private ListenerRegistration chatListener;
    private Chat currentChat;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            finish();
            return;
        }
        
        // Load current user's name
        loadCurrentUserName();
        
        // Get intent data
        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");
        chatType = intent.getStringExtra("chatType");
        otherUserId = intent.getStringExtra("otherUserId");
        otherUserName = intent.getStringExtra("otherUserName");
        chatName = intent.getStringExtra("chatName");
        
        // Initialize views
        initViews();
        setupRecyclerView();
        setupListeners();
        setupActivityResultLaunchers();
        
        // Handle chat creation or loading
        if (chatId != null) {
            loadExistingChat();
        } else if (otherUserId != null) {
            createDirectChat();
        } else {
            finish();
            return;
        }
        
        // Track screen view
        AnalyticsHelper.logScreenView(this, "chat_conversation");
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonAttach = findViewById(R.id.buttonAttach);
        textViewTyping = findViewById(R.id.textViewTyping);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewChatName = findViewById(R.id.textViewChatName);
        textViewOnlineStatus = findViewById(R.id.textViewOnlineStatus);
        
        // Initially disable send button
        buttonSend.setEnabled(false);
    }
    
    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(this, messages, currentUserId);
        adapter.setOnMessageClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setEnabled(s.toString().trim().length() > 0);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        buttonSend.setOnClickListener(v -> sendTextMessage());
        buttonAttach.setOnClickListener(v -> showAttachmentOptions());
        
        imageViewProfile.setOnClickListener(v -> {
            if (otherUserId != null) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("userId", otherUserId);
                startActivity(intent);
            }
        });
    }
    
    private void setupActivityResultLaunchers() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadAndSendImage(imageUri);
                    }
                }
            }
        );
        
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadAndSendFile(fileUri);
                    }
                }
            }
        );
        
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Handle camera result
                    // Implementation depends on camera activity setup
                }
            }
        );
    }
    
    private void loadExistingChat() {
        // Load chat metadata
        chatListener = db.collection("chats").document(chatId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading chat", error);
                        return;
                    }
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentChat = documentSnapshot.toObject(Chat.class);
                        if (currentChat != null) {
                            currentChat.setChatId(chatId);
                            updateChatUI();
                            markChatAsRead();
                        }
                    }
                });
        
        // Load messages
        loadMessages();
    }
    
    private void createDirectChat() {
        // Check if chat already exists
        String generatedChatId = Chat.generateChatId(currentUserId, otherUserId);
        
        db.collection("chats").document(generatedChatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Chat exists, load it
                        chatId = generatedChatId;
                        loadExistingChat();
                    } else {
                        // Create new chat
                        createNewDirectChat(generatedChatId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking existing chat", e);
                    Toast.makeText(this, "Error loading chat", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void createNewDirectChat(String newChatId) {
        // Get current user's name
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(currentUserDoc -> {
                    currentUserName = currentUserDoc.getString("fullName");
                    if (currentUserName == null) currentUserName = "Unknown User";
                    
                    // Create new chat
                    Chat newChat = new Chat(currentUserId, currentUserName, otherUserId, otherUserName);
                    
                    db.collection("chats").document(newChatId)
                            .set(newChat.toMap())
                            .addOnSuccessListener(aVoid -> {
                                chatId = newChatId;
                                currentChat = newChat;
                                currentChat.setChatId(chatId);
                                updateChatUI();
                                loadMessages();
                                
                                // Track chat creation
                                AnalyticsHelper.logEvent("chat_created", "chat_type", "direct");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating chat", e);
                                Toast.makeText(this, "Error creating chat", Toast.LENGTH_SHORT).show();
                            });
                });
    }
    
    private void updateChatUI() {
        if (currentChat == null) return;
        
        String displayName = currentChat.getDisplayName(currentUserId);
        String displayImage = currentChat.getDisplayImage(currentUserId);
        
        textViewChatName.setText(displayName);
        
        if (displayImage != null && !displayImage.isEmpty()) {
            Glide.with(this)
                    .load(displayImage)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(imageViewProfile);
        } else {
            imageViewProfile.setImageResource(R.drawable.ic_person);
        }
        
        // Update online status for direct chats
        if (currentChat.isDirectChat() && otherUserId != null) {
            updateOnlineStatus();
        } else {
            textViewOnlineStatus.setText("");
        }
    }
    
    private void updateOnlineStatus() {
        // Check user's online status
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isOnline = documentSnapshot.getBoolean("isOnline");
                        Long lastSeen = documentSnapshot.getLong("lastSeen");
                        
                        if (isOnline != null && isOnline) {
                            textViewOnlineStatus.setText("Online");
                            textViewOnlineStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                        } else if (lastSeen != null) {
                            String lastSeenText = getLastSeenText(lastSeen);
                            textViewOnlineStatus.setText(lastSeenText);
                            textViewOnlineStatus.setTextColor(getColor(android.R.color.darker_gray));
                        } else {
                            textViewOnlineStatus.setText("");
                        }
                    }
                });
    }
    
    private String getLastSeenText(long lastSeen) {
        long diff = System.currentTimeMillis() - lastSeen;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (minutes < 5) return "Last seen recently";
        if (minutes < 60) return "Last seen " + minutes + " minutes ago";
        if (hours < 24) return "Last seen " + hours + " hours ago";
        if (days < 7) return "Last seen " + days + " days ago";
        return "Last seen a while ago";
    }
    
    private void loadMessages() {
        if (chatId == null) return;
        
        messagesListener = db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading messages", error);
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        messages.clear();
                        
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            try {
                                ChatMessage message = document.toObject(ChatMessage.class);
                                if (message != null) {
                                    message.setMessageId(document.getId());
                                    
                                    // Validate message data
                                    if (SecurityHelper.isValidMessageData(message)) {
                                        messages.add(message);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing message document: " + document.getId(), e);
                            }
                        }
                        
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }
                        
                        // Mark messages as read
                        markMessagesAsRead();
                    }
                });
    }
    
    private void sendTextMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty() || chatId == null) return;
        
        // Validate message content
        if (!SecurityHelper.isValidMessageContent(messageText)) {
            Toast.makeText(this, "Message contains invalid content", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ChatMessage message = new ChatMessage(
                chatId,
                currentUserId,
                currentUserName != null ? currentUserName : "Unknown User",
                otherUserId,
                messageText
        );
        
        sendMessage(message);
        editTextMessage.setText("");
    }
    
    private void sendMessage(ChatMessage message) {
        if (chatId == null) return;
        
        // Add message to subcollection
        db.collection("chats").document(chatId)
                .collection("messages")
                .add(message.toMap())
                .addOnSuccessListener(documentReference -> {
                    // Update chat's last message info
                    updateChatLastMessage(message);
                    
                    // Track message sent
                    AnalyticsHelper.logEvent("message_sent", "message_type", message.getMessageType());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateChatLastMessage(ChatMessage message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessageText", message.getDisplayText());
        updates.put("lastMessageSenderId", message.getSenderId());
        updates.put("lastMessageType", message.getMessageType());
        updates.put("lastMessageTimestamp", message.getTimestamp());
        updates.put("updatedAt", FieldValue.serverTimestamp());
        
        // Update unread counts for other participants
        if (currentChat != null && currentChat.getParticipantIds() != null) {
            Map<String, Object> unreadCounts = new HashMap<>();
            for (String participantId : currentChat.getParticipantIds()) {
                if (!participantId.equals(currentUserId)) {
                    String unreadCountField = "unreadCounts." + participantId;
                    updates.put(unreadCountField, FieldValue.increment(1));
                }
            }
        }
        
        db.collection("chats").document(chatId)
                .update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating chat last message", e));
    }
    
    private void markChatAsRead() {
        if (chatId == null || currentUserId == null) return;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("unreadCounts." + currentUserId, 0);
        updates.put("lastSeenTimestamps." + currentUserId, FieldValue.serverTimestamp());
        
        db.collection("chats").document(chatId)
                .update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Error marking chat as read", e));
    }
    
    private void markMessagesAsRead() {
        // Mark unread messages as read
        // This could be optimized to only update recent unread messages
        for (ChatMessage message : messages) {
            if (!message.getSenderId().equals(currentUserId) && 
                !message.isRead()) {
                
                db.collection("chats").document(chatId)
                        .collection("messages").document(message.getMessageId())
                        .update("readStatus", "read", "readTimestamp", FieldValue.serverTimestamp())
                        .addOnFailureListener(e -> Log.e(TAG, "Error marking message as read", e));
            }
        }
    }
    
    private void showAttachmentOptions() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_attachment_options, null);
        bottomSheet.setContentView(view);
        
        view.findViewById(R.id.optionCamera).setOnClickListener(v -> {
            bottomSheet.dismiss();
            openCamera();
        });
        
        view.findViewById(R.id.optionGallery).setOnClickListener(v -> {
            bottomSheet.dismiss();
            openImagePicker();
        });
        
        view.findViewById(R.id.optionFile).setOnClickListener(v -> {
            bottomSheet.dismiss();
            openFilePicker();
        });
        
        view.findViewById(R.id.optionLocation).setOnClickListener(v -> {
            bottomSheet.dismiss();
            shareLocation();
        });
        
        // Add voice note option if it exists in layout
        View voiceOption = view.findViewById(R.id.optionVoice);
        if (voiceOption != null) {
            voiceOption.setOnClickListener(v -> {
                bottomSheet.dismiss();
                startVoiceRecording();
            });
        }
        
        bottomSheet.show();
    }
    
    private void startVoiceRecording() {
        // Check audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.RECORD_AUDIO}, 102);
            return;
        }
        
        Toast.makeText(this, "Voice recording coming soon!", Toast.LENGTH_SHORT).show();
        // TODO: Implement voice recording
    }
    
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Open camera
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(intent);
            }
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }
    
    private void shareLocation() {
        // TODO: Implement location sharing
        Toast.makeText(this, "Location sharing coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void uploadAndSendImage(Uri imageUri) {
        // TODO: Implement image upload to Firebase Storage
        Toast.makeText(this, "Image sending coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void uploadAndSendFile(Uri fileUri) {
        // TODO: Implement file upload to Firebase Storage
        Toast.makeText(this, "File sending coming soon", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onMessageClick(ChatMessage message) {
        // Handle message click (show details, copy text, etc.)
    }
    
    @Override
    public void onMessageLongClick(ChatMessage message) {
        // Show message options (reply, forward, delete, etc.)
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_call) {
            // TODO: Implement voice call
            Toast.makeText(this, "Voice call coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_video_call) {
            // TODO: Implement video call
            Toast.makeText(this, "Video call coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_chat_info) {
            // TODO: Show chat info
            Toast.makeText(this, "Chat info coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void loadCurrentUserName() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        currentUserName = userDoc.getString("fullName");
                        if (currentUserName == null) currentUserName = "Unknown User";
                    } else {
                        currentUserName = "Unknown User";
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading current user name", e);
                    currentUserName = "Unknown User";
                });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
        if (chatListener != null) {
            chatListener.remove();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
package com.namatovu.alumniportal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.adapters.ChatListAdapter;
import com.namatovu.alumniportal.models.Chat;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import com.namatovu.alumniportal.utils.SecurityHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements ChatListAdapter.OnChatClickListener {
    private static final String TAG = "ChatListActivity";
    
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private FloatingActionButton fabNewChat;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private ListenerRegistration chatsListener;
    
    private List<Chat> allChats = new ArrayList<>();
    private List<Chat> filteredChats = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            finish();
            return;
        }
        
        // Initialize views
        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Load chats
        loadChats();
        
        // Track screen view
        AnalyticsHelper.logScreenView(this, "chat_list");
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Messages");
        }
        
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);
        fabNewChat = findViewById(R.id.fabNewChat);
    }
    
    private void setupRecyclerView() {
        adapter = new ChatListAdapter(this, filteredChats, currentUserId);
        adapter.setOnChatClickListener(this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshChats);
        
        fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlumniDirectoryActivity.class);
            intent.putExtra("selectForChat", true);
            startActivity(intent);
        });
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterChats(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                filterChats(newText);
                return true;
            }
        });
    }
    
    private void loadChats() {
        if (currentUserId == null) return;
        
        swipeRefreshLayout.setRefreshing(true);
        
        // Listen for real-time updates to user's chats
        chatsListener = db.collection("chats")
                .whereArrayContains("participantIds", currentUserId)
                .whereEqualTo("isActive", true)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    swipeRefreshLayout.setRefreshing(false);
                    
                    if (error != null) {
                        Log.e(TAG, "Error loading chats", error);
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        allChats.clear();
                        
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            try {
                                Chat chat = document.toObject(Chat.class);
                                if (chat != null) {
                                    chat.setChatId(document.getId());
                                    
                                    // Validate chat data
                                    if (SecurityHelper.isValidChatData(chat, currentUserId)) {
                                        allChats.add(chat);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing chat document: " + document.getId(), e);
                            }
                        }
                        
                        // Apply current filter
                        String currentQuery = searchView.getQuery().toString();
                        filterChats(currentQuery);
                        
                        Log.d(TAG, "Loaded " + allChats.size() + " chats");
                    }
                });
    }
    
    private void refreshChats() {
        // Refresh is handled by the real-time listener
        // Just show loading state briefly
        swipeRefreshLayout.postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }
    
    private void filterChats(String query) {
        if (query == null) query = "";
        query = query.toLowerCase().trim();
        
        filteredChats.clear();
        
        if (query.isEmpty()) {
            filteredChats.addAll(allChats);
        } else {
            for (Chat chat : allChats) {
                String displayName = chat.getDisplayName(currentUserId);
                String lastMessage = chat.getLastMessageDisplayText();
                
                if ((displayName != null && displayName.toLowerCase().contains(query)) ||
                    (lastMessage != null && lastMessage.toLowerCase().contains(query))) {
                    filteredChats.add(chat);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Track search if query is not empty
        if (!query.isEmpty()) {
            AnalyticsHelper.logSearch(query, "chat_search", filteredChats.size());
        }
    }
    
    @Override
    public void onChatClick(Chat chat) {
        if (chat == null || chat.getChatId() == null) return;
        
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chat.getChatId());
        intent.putExtra("chatType", chat.getChatType());
        
        if (chat.isDirectChat()) {
            String otherUserId = chat.getOtherParticipantId(currentUserId);
            String otherUserName = chat.getOtherParticipantName(currentUserId);
            intent.putExtra("otherUserId", otherUserId);
            intent.putExtra("otherUserName", otherUserName);
        } else {
            intent.putExtra("chatName", chat.getChatName());
        }
        
        startActivity(intent);
        
        // Track chat open
        AnalyticsHelper.logEvent("chat_opened", "chat_type", chat.getChatType());
    }
    
    @Override
    public void onChatLongClick(Chat chat) {
        // Show chat options menu (mute, delete, etc.)
        // TODO: Implement chat options
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatsListener != null) {
            chatsListener.remove();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
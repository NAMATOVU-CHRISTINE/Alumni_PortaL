package com.namatovu.alumniportal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.adapters.AlumniGroupAdapter;
import com.namatovu.alumniportal.models.AlumniGroup;

import java.util.ArrayList;
import java.util.List;

public class AlumniGroupsActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private AlumniGroupAdapter adapter;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private FloatingActionButton fabCreateGroup;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    
    private List<AlumniGroup> allGroups = new ArrayList<>();
    private List<AlumniGroup> myGroups = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_groups);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            finish();
            return;
        }
        
        initViews();
        setupRecyclerView();
        setupTabLayout();
        loadGroups();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);
        fabCreateGroup = findViewById(R.id.fabCreateGroup);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alumni Groups");
        }
        
        fabCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGroupActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupRecyclerView() {
        adapter = new AlumniGroupAdapter(this, allGroups, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnGroupClickListener(group -> {
            Intent intent = new Intent(this, GroupDetailActivity.class);
            intent.putExtra("groupId", group.getGroupId());
            startActivity(intent);
        });
    }
    
    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("All Groups"));
        tabLayout.addTab(tabLayout.newTab().setText("My Groups"));
        tabLayout.addTab(tabLayout.newTab().setText("Class Groups"));
        tabLayout.addTab(tabLayout.newTab().setText("Department"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterGroups(tab.getPosition());
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void loadGroups() {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("groups")
                .orderBy("lastActivityAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allGroups.clear();
                    myGroups.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AlumniGroup group = document.toObject(AlumniGroup.class);
                        group.setGroupId(document.getId());
                        allGroups.add(group);
                        
                        if (group.isMember(currentUserId)) {
                            myGroups.add(group);
                        }
                    }
                    
                    adapter.updateGroups(allGroups);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading groups", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void filterGroups(int tabPosition) {
        List<AlumniGroup> filteredGroups = new ArrayList<>();
        
        switch (tabPosition) {
            case 0: // All Groups
                filteredGroups.addAll(allGroups);
                break;
            case 1: // My Groups
                filteredGroups.addAll(myGroups);
                break;
            case 2: // Class Groups
                for (AlumniGroup group : allGroups) {
                    if ("class".equals(group.getGroupType())) {
                        filteredGroups.add(group);
                    }
                }
                break;
            case 3: // Department
                for (AlumniGroup group : allGroups) {
                    if ("department".equals(group.getGroupType())) {
                        filteredGroups.add(group);
                    }
                }
                break;
        }
        
        adapter.updateGroups(filteredGroups);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

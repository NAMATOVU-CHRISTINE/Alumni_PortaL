package com.namatovu.alumniportal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.AlumniGroup;

public class CreateGroupActivity extends AppCompatActivity {
    
    private EditText editTextGroupName;
    private EditText editTextDescription;
    private Spinner spinnerGroupType;
    private EditText editTextGraduationYear;
    private EditText editTextDepartment;
    private Button buttonCreate;
    private ProgressBar progressBar;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        initViews();
        setupSpinner();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Group");
        }
    }
    
    private void initViews() {
        editTextGroupName = findViewById(R.id.editTextGroupName);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerGroupType = findViewById(R.id.spinnerGroupType);
        editTextGraduationYear = findViewById(R.id.editTextGraduationYear);
        editTextDepartment = findViewById(R.id.editTextDepartment);
        buttonCreate = findViewById(R.id.buttonCreate);
        progressBar = findViewById(R.id.progressBar);
        
        buttonCreate.setOnClickListener(v -> createGroup());
    }
    
    private void setupSpinner() {
        String[] groupTypes = {"Class Group", "Department", "Interest", "Location"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, groupTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroupType.setAdapter(adapter);
    }
    
    private void createGroup() {
        String groupName = editTextGroupName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String groupTypeDisplay = spinnerGroupType.getSelectedItem().toString();
        String graduationYear = editTextGraduationYear.getText().toString().trim();
        String department = editTextDepartment.getText().toString().trim();
        
        if (groupName.isEmpty()) {
            editTextGroupName.setError("Group name is required");
            return;
        }
        
        if (description.isEmpty()) {
            editTextDescription.setError("Description is required");
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        buttonCreate.setEnabled(false);
        
        // Convert display type to internal type
        String groupType = groupTypeDisplay.toLowerCase().replace(" ", "_");
        if (groupType.equals("class_group")) {
            groupType = "class";
        }
        
        // Get current user info
        String currentUserId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String currentUserName = doc.getString("fullName");
                    
                    // Create group
                    AlumniGroup group = new AlumniGroup(groupName, groupType, currentUserId, currentUserName);
                    group.setDescription(description);
                    
                    if (!graduationYear.isEmpty()) {
                        group.setGraduationYear(graduationYear);
                    }
                    
                    if (!department.isEmpty()) {
                        group.setDepartment(department);
                    }
                    
                    // Save to Firestore
                    db.collection("groups")
                            .add(group.toMap())
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                buttonCreate.setEnabled(true);
                                Toast.makeText(this, "Error creating group: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonCreate.setEnabled(true);
                    Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show();
                });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

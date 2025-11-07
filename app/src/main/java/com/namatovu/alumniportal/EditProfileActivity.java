package com.namatovu.alumniportal;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.namatovu.alumniportal.utils.PermissionHelper;
import com.namatovu.alumniportal.utils.AnalyticsHelper;
import com.namatovu.alumniportal.utils.SecurityHelper;
import com.namatovu.alumniportal.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.namatovu.alumniportal.databinding.ActivityEditProfileBinding;

import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri selectedImageUri;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        setSupportActionBar(binding.toolbar);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                Glide.with(EditProfileActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.profileImage);
            }
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Storage permission required to select images", Toast.LENGTH_SHORT).show();
            }
        });

        binding.changeProfilePhotoText.setOnClickListener(v -> ensurePermissionAndPickImage());

        // skill add button (end icon on text input) is handled via clicking the label area
        binding.skillTextInputLayout.setEndIconOnClickListener(v -> addSkillFromInput());

        binding.saveButton.setOnClickListener(v -> saveProfile());
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("EditProfileActivity", "HomeActivity");
        
        // Initialize Security Helper
        SecurityHelper.initialize(this);

        loadCurrentProfile();
    }

    private void ensurePermissionAndPickImage() {
        if (PermissionHelper.hasStoragePermission(this)) {
            openImagePicker();
        } else {
            PermissionHelper.requestStoragePermission(this);
        }
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    private void addSkillFromInput() {
        String skill = binding.skillEditText.getText() != null ? binding.skillEditText.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(skill)) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> binding.skillsChipGroup.removeView(chip));
            binding.skillsChipGroup.addView(chip);
            binding.skillEditText.setText("");
        }
    }

    private void loadCurrentProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    try {
                        User u = doc.toObject(User.class);
                        if (u != null) populateFields(u);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to deserialize user", e);
                    }
                }
            }
        });
    }

    private void populateFields(@NonNull User u) {
        binding.nameEditText.setText(u.getFullName());
        binding.bioEditText.setText(u.getBio());
        binding.careerEditText.setText(u.getCurrentJob());

        binding.skillsChipGroup.removeAllViews();
        for (String s : u.getSkills()) {
            Chip chip = new Chip(this);
            chip.setText(s);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> binding.skillsChipGroup.removeView(chip));
            binding.skillsChipGroup.addView(chip);
        }

        String url = u.getProfileImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).placeholder(R.drawable.ic_person).into(binding.profileImage);
        }
    }

    private void saveProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String name = binding.nameEditText.getText() != null ? binding.nameEditText.getText().toString().trim() : "";
        String bio = binding.bioEditText.getText() != null ? binding.bioEditText.getText().toString().trim() : "";
        String career = binding.careerEditText.getText() != null ? binding.careerEditText.getText().toString().trim() : "";

        // Validate and sanitize input
        final String finalName = SecurityHelper.sanitizeInput(name);
        final String finalBio = SecurityHelper.sanitizeInput(bio);
        final String finalCareer = SecurityHelper.sanitizeInput(career);

        // Validate profile data
        if (!SecurityHelper.isValidProfileData(finalName, finalBio, null)) {
            Toast.makeText(this, "Please check your input. Some fields contain invalid data.", Toast.LENGTH_LONG).show();
            return;
        }

        final List<String> skills = new ArrayList<>();
        for (int i = 0; i < binding.skillsChipGroup.getChildCount(); i++) {
            View v = binding.skillsChipGroup.getChildAt(i);
            if (v instanceof Chip) {
                String skill = SecurityHelper.sanitizeInput(((Chip) v).getText().toString());
                if (!skill.isEmpty()) {
                    skills.add(skill);
                }
            }
        }

        binding.saveButton.setEnabled(false);

        // If image selected, upload first
        if (selectedImageUri != null) {
            StorageReference ref = storageRef.child(user.getUid() + ".jpg");
            UploadTask uploadTask = ref.putFile(selectedImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                saveProfileDocument(user.getUid(), name, bio, career, skills, imageUrl);
            }).addOnFailureListener(e -> {
                binding.saveButton.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                binding.saveButton.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
            });
        } else {
            // No image change
            saveProfileDocument(user.getUid(), name, bio, career, skills, null);
        }
    }

    private void saveProfileDocument(String uid, String name, String bio, String career, List<String> skills, String imageUrl) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("fullName", name);
        updates.put("bio", bio);
        updates.put("currentJob", career);
        updates.put("skills", skills);
        if (imageUrl != null) updates.put("profileImageUrl", imageUrl);

        db.collection("users").document(uid).update(updates).addOnCompleteListener(task -> {
            binding.saveButton.setEnabled(true);
            if (task.isSuccessful()) {
                // Log analytics event for profile update
                String editType = selectedImageUri != null ? "with_photo" : "without_photo";
                AnalyticsHelper.logProfileEdit(editType);
                
                Toast.makeText(EditProfileActivity.this, "Profile updated.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                AnalyticsHelper.logError("profile_update_failed", 
                    task.getException() != null ? task.getException().getMessage() : "Unknown error", 
                    "EditProfileActivity");
            }
        });
    }
}

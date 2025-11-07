package com.namatovu.alumniportal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;

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
        
        // Initialize Firebase Storage with explicit bucket URL
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://alumniportal-198ec.firebasestorage.app");
            // Use root reference first, then create profile_images folder
            storageRef = storage.getReference().child("profile_images");
            Log.d(TAG, "Firebase Storage initialized with explicit bucket: " + storageRef.toString());
            Log.d(TAG, "Storage bucket: " + storage.getApp().getOptions().getStorageBucket());
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Storage with explicit bucket, using default", e);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference().child("profile_images");
            Log.d(TAG, "Firebase Storage reference created with default: " + storageRef.toString());
            Log.d(TAG, "Storage bucket: " + storage.getApp().getOptions().getStorageBucket());
        }

        setSupportActionBar(binding.toolbar);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                Log.d(TAG, "Image selected: " + uri.toString());
                Glide.with(EditProfileActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.profileImage);
                Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "No image selected");
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                Log.d(TAG, "Permission granted, opening image picker");
                openImagePicker();
            } else {
                Log.w(TAG, "Storage permission denied");
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
        
        // Test Firebase connection
        testFirebaseConnection();
    }

    private void testFirebaseConnection() {
        Log.d(TAG, "Testing Firebase connection...");
        
        // Test Firestore
        db.collection("users").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firestore connection successful");
            } else {
                Log.e(TAG, "Firestore connection failed", task.getException());
            }
        });
        
        // Test Storage
        storageRef.child("test").getMetadata().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase Storage connection successful");
            } else {
                Log.d(TAG, "Firebase Storage connection test (expected to fail for non-existent file): " + 
                      (task.getException() != null ? task.getException().getMessage() : "unknown"));
            }
        });
    }

    private void ensurePermissionAndPickImage() {
        // For Android 13+ (API 33+), we need READ_MEDIA_IMAGES permission
        // For older versions, we need READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Media images permission already granted");
                openImagePicker();
            } else {
                Log.d(TAG, "Requesting media images permission");
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "External storage permission already granted");
                openImagePicker();
            } else {
                Log.d(TAG, "Requesting external storage permission");
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Log.d(TAG, "Opening image picker...");
        try {
            pickImageLauncher.launch("image/*");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open image picker", e);
            Toast.makeText(this, "Failed to open image picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addSkillFromInput() {
        String skill = binding.skillEditText.getText() != null ? binding.skillEditText.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(skill)) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> binding.skillsChipGroup.removeView(chip));
            // Apply custom styling
            chip.setChipBackgroundColorResource(R.color.light_gray);
            chip.setTextColor(getColor(R.color.black));
            chip.setChipStrokeColorResource(R.color.must_green);
            chip.setChipStrokeWidth(2.0f);
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
            // Apply custom styling
            chip.setChipBackgroundColorResource(R.color.light_gray);
            chip.setTextColor(getColor(R.color.black));
            chip.setChipStrokeColorResource(R.color.must_green);
            chip.setChipStrokeWidth(2.0f);
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
            Log.d(TAG, "Starting image upload...");
            
            // Use simpler file naming and path
            String fileName = "profile_" + user.getUid() + ".jpg";
            
            // Try uploading with explicit bucket configuration
            try {
                FirebaseStorage storage = FirebaseStorage.getInstance("gs://alumniportal-198ec.firebasestorage.app");
                StorageReference rootRef = storage.getReference();
                StorageReference imageRef = rootRef.child("profile_images").child(fileName);
                
                Log.d(TAG, "Upload path: " + imageRef.getPath());
                Log.d(TAG, "Selected image URI: " + selectedImageUri.toString());
                Log.d(TAG, "Storage bucket: " + storage.getApp().getOptions().getStorageBucket());
                
                UploadTask uploadTask = imageRef.putFile(selectedImageUri);
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload progress: " + progress + "%");
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image upload successful, getting download URL...");
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Log.d(TAG, "Download URL obtained: " + imageUrl);
                    saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, skills, imageUrl);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL", e);
                    binding.saveButton.setEnabled(true);
                    
                    // If getting download URL fails, try alternative approach
                    if (e.getMessage() != null && e.getMessage().contains("object")) {
                        Toast.makeText(EditProfileActivity.this, "Upload completed but URL retrieval failed. Saving profile without image.", Toast.LENGTH_LONG).show();
                        saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, skills, null);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed", e);
                binding.saveButton.setEnabled(true);
                
                // Handle specific "object doesn't exist" error
                if (e.getMessage() != null && e.getMessage().contains("object")) {
                    Log.d(TAG, "Trying upload to root directory as fallback...");
                    uploadToRootDirectory(user, finalName, finalBio, finalCareer, skills);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Firebase Storage for upload", e);
                binding.saveButton.setEnabled(true);
                uploadToRootDirectory(user, finalName, finalBio, finalCareer, skills);
            }
        } else {
            Log.d(TAG, "No image selected, saving profile without image update");
            // No image change
            saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, skills, null);
        }
    }

    private void uploadToRootDirectory(FirebaseUser user, String finalName, String finalBio, String finalCareer, List<String> skills) {
        Log.d(TAG, "Attempting upload to root directory...");
        
        String fileName = "profile_" + user.getUid() + ".jpg";
        
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://alumniportal-198ec.firebasestorage.app");
            StorageReference rootRef = storage.getReference().child(fileName);
            
            Log.d(TAG, "Root upload path: " + rootRef.getPath());
            Log.d(TAG, "Root storage bucket: " + storage.getApp().getOptions().getStorageBucket());
            
            UploadTask uploadTask = rootRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Root directory upload successful");
            rootRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                Log.d(TAG, "Root upload download URL: " + imageUrl);
                saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, skills, imageUrl);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get root upload URL", e);
                binding.saveButton.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Image uploaded but failed to get URL. Saving profile without image.", Toast.LENGTH_LONG).show();
                saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, skills, null);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Root directory upload also failed", e);
            binding.saveButton.setEnabled(true);
            Toast.makeText(EditProfileActivity.this, "Image upload failed completely. Saving profile without image.", Toast.LENGTH_LONG).show();
            saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, skills, null);
        });
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Storage for root upload", e);
            binding.saveButton.setEnabled(true);
            Toast.makeText(EditProfileActivity.this, "Storage initialization failed. Saving profile without image.", Toast.LENGTH_LONG).show();
            saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, skills, null);
        }
    }

    private void saveProfileDocument(String uid, String name, String bio, String career, List<String> skills, String imageUrl) {
        Log.d(TAG, "Saving profile document for user: " + uid);
        
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("fullName", name);  // Use "fullName" to match models.User
        updates.put("bio", bio);
        updates.put("currentJob", career);  // Use "currentJob" to match models.User
        updates.put("skills", skills);
        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
            Log.d(TAG, "Including profile image URL in update");
        }

        db.collection("users").document(uid).update(updates).addOnCompleteListener(task -> {
            binding.saveButton.setEnabled(true);
            if (task.isSuccessful()) {
                Log.d(TAG, "Profile updated successfully");
                // Log analytics event for profile update
                String editType = selectedImageUri != null ? "with_photo" : "without_photo";
                AnalyticsHelper.logProfileEdit(editType);
                
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Failed to update profile", task.getException());
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + errorMessage, Toast.LENGTH_LONG).show();
                AnalyticsHelper.logError("profile_update_failed", errorMessage, "EditProfileActivity");
            }
        });
    }
}

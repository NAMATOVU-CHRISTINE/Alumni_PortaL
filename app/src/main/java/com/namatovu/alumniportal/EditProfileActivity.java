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
import com.namatovu.alumniportal.utils.CloudinaryHelper;
import com.namatovu.alumniportal.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.databinding.ActivityEditProfileBinding;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        
        // Initialize Cloudinary
        CloudinaryHelper.initialize(this);

        setSupportActionBar(binding.toolbar);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                Glide.with(EditProfileActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.profileImage);
                Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
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

        setupDropdowns();

        binding.saveButton.setOnClickListener(v -> saveProfile());
        
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logNavigation("EditProfileActivity", "HomeActivity");
        
        SecurityHelper.initialize(this);

        loadCurrentProfile();
    }

    private void ensurePermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        try {
            pickImageLauncher.launch("image/*");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open image picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupDropdowns() {
        // User Type dropdown
        // Options:
        // - "Student" = Current students
        // - "Alumni" = Graduated students
        // - "Staff" = Faculty/staff members
        String[] userTypes = {"Student", "Alumni", "Staff"};
        android.widget.ArrayAdapter<String> statusAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, userTypes);
        binding.statusDropdown.setAdapter(statusAdapter);
        
        // Work Status dropdown
        String[] workStatuses = {"Employed", "Self-employed", "Not working", "Looking for a job", "Student"};
        android.widget.ArrayAdapter<String> workStatusAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, workStatuses);
        binding.workStatusDropdown.setAdapter(workStatusAdapter);
        
        // Skills dropdown
        String[] skills = {
            "Java", "Python", "JavaScript", "C++", "C#", "PHP", "Ruby", "Swift", "Kotlin",
            "React", "Angular", "Vue.js", "Node.js", "Django", "Flask", "Spring Boot",
            "Machine Learning", "Data Science", "AI", "Cloud Computing", "AWS", "Azure", "GCP",
            "DevOps", "Docker", "Kubernetes", "CI/CD", "Git", "Agile", "Scrum",
            "Project Management", "Leadership", "Communication", "Problem Solving",
            "UI/UX Design", "Graphic Design", "Digital Marketing", "SEO", "Content Writing",
            "Sales", "Business Analysis", "Financial Analysis", "Accounting", "HR Management"
        };
        android.widget.ArrayAdapter<String> skillsAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, skills);
        binding.skillEditText.setAdapter(skillsAdapter);
        binding.skillEditText.setOnItemClickListener((parent, view, position, id) -> {
            String skill = (String) parent.getItemAtPosition(position);
            addSkillChip(skill);
            binding.skillEditText.setText("");
        });

        // Industry dropdown
        String[] industries = {
            "Technology", "Finance", "Healthcare", "Education", "Manufacturing",
            "Retail", "Hospitality", "Real Estate", "Construction", "Transportation",
            "Media & Entertainment", "Telecommunications", "Energy", "Agriculture",
            "Government", "Non-Profit", "Consulting", "Legal", "Marketing & Advertising"
        };
        android.widget.ArrayAdapter<String> industryAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, industries);
        binding.industryDropdown.setAdapter(industryAdapter);

        // Currency dropdown
        String[] currencies = {
            "USD - US Dollar", "EUR - Euro", "GBP - British Pound", "JPY - Japanese Yen",
            "AUD - Australian Dollar", "CAD - Canadian Dollar", "CHF - Swiss Franc",
            "CNY - Chinese Yuan", "INR - Indian Rupee", "UGX - Ugandan Shilling",
            "KES - Kenyan Shilling", "TZS - Tanzanian Shilling", "ZAR - South African Rand"
        };
        android.widget.ArrayAdapter<String> currencyAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, currencies);
        binding.currencyDropdown.setAdapter(currencyAdapter);
    }

    private void addSkillChip(String skill) {
        if (!TextUtils.isEmpty(skill)) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> binding.skillsChipGroup.removeView(chip));
            chip.setChipBackgroundColorResource(R.color.light_gray);
            chip.setTextColor(getColor(R.color.black));
            chip.setChipStrokeColorResource(R.color.must_green);
            chip.setChipStrokeWidth(2.0f);
            binding.skillsChipGroup.addView(chip);
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
        binding.graduationYearEditText.setText(u.getGraduationYear());
        
        // Set user type based on userType field
        String userType = u.getUserType();
        if ("alumni".equalsIgnoreCase(userType)) {
            binding.statusDropdown.setText("Alumni", false);
        } else if ("staff".equalsIgnoreCase(userType)) {
            binding.statusDropdown.setText("Staff", false);
        } else {
            binding.statusDropdown.setText("Student", false);
        }
        
        // Set work status
        String workStatus = u.getWorkStatus();
        if (workStatus != null && !workStatus.isEmpty()) {
            binding.workStatusDropdown.setText(workStatus, false);
        }

        binding.skillsChipGroup.removeAllViews();
        for (String s : u.getSkills()) {
            addSkillChip(s);
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
        String graduationYear = binding.graduationYearEditText.getText() != null ? binding.graduationYearEditText.getText().toString().trim() : "";
        String status = binding.statusDropdown.getText() != null ? binding.statusDropdown.getText().toString().trim() : "Student";
        String workStatus = binding.workStatusDropdown.getText() != null ? binding.workStatusDropdown.getText().toString().trim() : "";
        String industry = binding.industryDropdown.getText() != null ? binding.industryDropdown.getText().toString().trim() : "";
        String currency = binding.currencyDropdown.getText() != null ? binding.currencyDropdown.getText().toString().trim() : "";

        final String finalName = SecurityHelper.sanitizeInput(name);
        final String finalBio = SecurityHelper.sanitizeInput(bio);
        final String finalCareer = SecurityHelper.sanitizeInput(career);
        final String finalGraduationYear = SecurityHelper.sanitizeInput(graduationYear);
        final String finalIndustry = SecurityHelper.sanitizeInput(industry);
        final String finalCurrency = SecurityHelper.sanitizeInput(currency);

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

        // Upload image to Cloudinary if selected
        if (selectedImageUri != null) {
            Log.d(TAG, "Starting image upload to Cloudinary");
            binding.changeProfilePhotoText.setText("Uploading...");
            
            CloudinaryHelper.uploadImage(selectedImageUri, "profiles", new CloudinaryHelper.CloudinaryUploadCallback() {
                @Override
                public void onUploadStart() {
                    Log.d(TAG, "Upload started");
                    runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onUploadProgress(int progress) {
                    Log.d(TAG, "Upload progress: " + progress + "%");
                    runOnUiThread(() -> binding.changeProfilePhotoText.setText("Uploading " + progress + "%"));
                }

                @Override
                public void onUploadSuccess(String imageUrl, String publicId) {
                    Log.d(TAG, "Upload successful! URL: " + imageUrl);
                    runOnUiThread(() -> {
                        binding.changeProfilePhotoText.setText("Change Profile Photo");
                        saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, finalGraduationYear, skills, imageUrl, publicId);
                    });
                }

                @Override
                public void onUploadError(String error) {
                    Log.e(TAG, "Upload failed: " + error);
                    runOnUiThread(() -> {
                        binding.changeProfilePhotoText.setText("Change Profile Photo");
                        binding.saveButton.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } else {
            Log.d(TAG, "No image selected, saving profile without image");
            // No image selected, save profile without image
            saveProfileDocument(user.getUid(), finalName, finalBio, finalCareer, finalGraduationYear, skills, null, null);
        }
    }

    private void saveProfileDocument(String uid, String name, String bio, String career, String graduationYear, List<String> skills, String imageUrl, String publicId) {
        String userType = binding.statusDropdown.getText() != null ? binding.statusDropdown.getText().toString().trim() : "Student";
        String workStatus = binding.workStatusDropdown.getText() != null ? binding.workStatusDropdown.getText().toString().trim() : "";
        String industry = binding.industryDropdown.getText() != null ? binding.industryDropdown.getText().toString().trim() : "";
        String currency = binding.currencyDropdown.getText() != null ? binding.currencyDropdown.getText().toString().trim() : "";
        
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("fullName", name);
        updates.put("bio", bio);
        updates.put("currentJob", career);
        updates.put("graduationYear", graduationYear);
        updates.put("workStatus", workStatus);
        updates.put("userType", userType.toLowerCase());
        updates.put("isAlumni", "alumni".equalsIgnoreCase(userType));
        updates.put("industry", industry);
        updates.put("currency", currency);
        updates.put("skills", skills);
        updates.put("updatedAt", System.currentTimeMillis());
        
        if (imageUrl != null) {
            Log.d(TAG, "Saving image URL to Firestore: " + imageUrl);
            updates.put("profileImageUrl", imageUrl);
            updates.put("profileImagePublicId", publicId);
        }

        Log.d(TAG, "Updating Firestore document for user: " + uid);
        db.collection("users").document(uid).update(updates).addOnCompleteListener(task -> {
            binding.saveButton.setEnabled(true);
            if (task.isSuccessful()) {
                String editType = selectedImageUri != null ? "with_photo" : "without_photo";
                AnalyticsHelper.logProfileEdit(editType);
                
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                
                // Send result back to trigger refresh
                Intent resultIntent = new Intent();
                resultIntent.putExtra("profile_updated", true);
                setResult(RESULT_OK, resultIntent);
                
                finish();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + errorMessage, Toast.LENGTH_LONG).show();
                AnalyticsHelper.logError("profile_update_failed", errorMessage, "EditProfileActivity");
            }
        });
    }
}
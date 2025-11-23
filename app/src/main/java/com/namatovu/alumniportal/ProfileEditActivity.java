package com.namatovu.alumniportal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.utils.CloudinaryHelper;
import com.namatovu.alumniportal.utils.ImageUploadHelper;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private Button btnChangePhoto;
    private ProgressBar uploadProgress;
    private TextView uploadStatus;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize Cloudinary
        CloudinaryHelper.initialize(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImage = findViewById(R.id.profileImage);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        uploadProgress = findViewById(R.id.uploadProgress);
        uploadStatus = findViewById(R.id.uploadStatus);

        loadCurrentProfileImage();

        btnChangePhoto.setOnClickListener(v -> showImageSourceDialog());
    }

    private void loadCurrentProfileImage() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists() && doc.contains("profileImageUrl")) {
                    String imageUrl = doc.getString("profileImageUrl");
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .into(profileImage);
                }
            });
    }

    private void showImageSourceDialog() {
        String[] options = {"Choose from Gallery", "Take Photo"};
        
        new AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    ImageUploadHelper.pickImageFromGallery(this);
                } else {
                    ImageUploadHelper.captureImageFromCamera(this);
                }
            })
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageUploadHelper.REQUEST_IMAGE_PICK && data != null) {
                selectedImageUri = data.getData();
                uploadImageToCloudinary();
            } else if (requestCode == ImageUploadHelper.REQUEST_IMAGE_CAPTURE && data != null) {
                selectedImageUri = data.getData();
                uploadImageToCloudinary();
            }
        }
    }

    private void uploadImageToCloudinary() {
        if (selectedImageUri == null) return;

        CloudinaryHelper.uploadImage(selectedImageUri, "profiles", 
            new CloudinaryHelper.CloudinaryUploadCallback() {
                @Override
                public void onUploadStart() {
                    uploadProgress.setVisibility(View.VISIBLE);
                    uploadStatus.setVisibility(View.VISIBLE);
                    uploadStatus.setText("Uploading...");
                    btnChangePhoto.setEnabled(false);
                }

                @Override
                public void onUploadProgress(int progress) {
                    uploadProgress.setProgress(progress);
                    uploadStatus.setText("Uploading... " + progress + "%");
                }

                @Override
                public void onUploadSuccess(String imageUrl, String publicId) {
                    // Save to Firestore
                    saveImageToFirestore(imageUrl, publicId);
                    
                    // Display the image
                    Glide.with(ProfileEditActivity.this)
                        .load(imageUrl)
                        .into(profileImage);
                    
                    uploadProgress.setVisibility(View.GONE);
                    uploadStatus.setVisibility(View.GONE);
                    btnChangePhoto.setEnabled(true);
                }

                @Override
                public void onUploadError(String error) {
                    uploadProgress.setVisibility(View.GONE);
                    uploadStatus.setVisibility(View.GONE);
                    uploadStatus.setText("Upload failed");
                    btnChangePhoto.setEnabled(true);
                    Toast.makeText(ProfileEditActivity.this, 
                        "Upload failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void saveImageToFirestore(String imageUrl, String publicId) {
        String userId = auth.getCurrentUser().getUid();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", imageUrl);
        updates.put("profileImagePublicId", publicId);
        updates.put("updatedAt", System.currentTimeMillis());
        
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}

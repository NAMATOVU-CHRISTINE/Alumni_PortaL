package com.namatovu.alumniportal.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

public class ImageUploadHelper {
    
    public static final int REQUEST_IMAGE_PICK = 1001;
    public static final int REQUEST_IMAGE_CAPTURE = 1002;

    /**
     * Open image picker
     */
    public static void pickImageFromGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    /**
     * Open camera to take photo
     */
    public static void captureImageFromCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(activity, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Upload image to Cloudinary with progress
     */
    public static void uploadToCloudinary(Activity activity, Uri imageUri, String folder, 
                                         CloudinaryHelper.CloudinaryUploadCallback callback) {
        if (imageUri == null) {
            Toast.makeText(activity, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        CloudinaryHelper.uploadImage(imageUri, folder, new CloudinaryHelper.CloudinaryUploadCallback() {
            @Override
            public void onUploadStart() {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Uploading image...", Toast.LENGTH_SHORT).show();
                    callback.onUploadStart();
                });
            }

            @Override
            public void onUploadProgress(int progress) {
                activity.runOnUiThread(() -> callback.onUploadProgress(progress));
            }

            @Override
            public void onUploadSuccess(String imageUrl, String publicId) {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    callback.onUploadSuccess(imageUrl, publicId);
                });
            }

            @Override
            public void onUploadError(String error) {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    callback.onUploadError(error);
                });
            }
        });
    }
}

package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static boolean isInitialized = false;

    /**
     * Initialize Cloudinary with your credentials
     * Get these from: https://console.cloudinary.com/
     */
    public static void initialize(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dekdqvxwi");
            config.put("api_key", "789253959439482");
            config.put("api_secret", "_-C04hSLVxKa1bRbxMwcc-1OW88");
            
            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "Cloudinary initialized");
        }
    }

    /**
     * Upload an image to Cloudinary
     * @param imageUri The local URI of the image
     * @param folder The folder in Cloudinary (e.g., "profiles", "events", "posts")
     * @param callback Callback to handle success/failure
     */
    public static void uploadImage(Uri imageUri, String folder, CloudinaryUploadCallback callback) {
        Map<String, Object> options = new HashMap<>();
        options.put("folder", "alumni_portal/" + folder);
        options.put("resource_type", "image");
        
        MediaManager.get().upload(imageUri)
                .option("folder", "alumni_portal/" + folder)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                        callback.onUploadStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        callback.onUploadProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        String publicId = (String) resultData.get("public_id");
                        Log.d(TAG, "Upload successful: " + imageUrl);
                        callback.onUploadSuccess(imageUrl, publicId);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload failed: " + error.getDescription());
                        callback.onUploadError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    /**
     * Get optimized image URL with transformations
     * @param publicId The public ID from Cloudinary
     * @param width Desired width
     * @param height Desired height
     * @return Optimized image URL
     */
    public static String getOptimizedImageUrl(String publicId, int width, int height) {
        if (publicId == null || publicId.isEmpty()) {
            return null;
        }
        
        // Format: https://res.cloudinary.com/{cloud_name}/image/upload/w_{width},h_{height},c_fill,q_auto,f_auto/{public_id}
        return String.format(
            "https://res.cloudinary.com/dekdqvxwi/image/upload/w_%d,h_%d,c_fill,q_auto,f_auto/%s",
            width, height, publicId
        );
    }

    /**
     * Get thumbnail URL
     */
    public static String getThumbnailUrl(String publicId) {
        return getOptimizedImageUrl(publicId, 200, 200);
    }

    /**
     * Get profile image URL
     */
    public static String getProfileImageUrl(String publicId) {
        return getOptimizedImageUrl(publicId, 400, 400);
    }

    /**
     * Delete an image from Cloudinary
     * Note: This requires server-side implementation for security
     */
    public static void deleteImage(String publicId) {
        // Deletion should be done server-side (Firebase Functions)
        // to keep API secret secure
        Log.w(TAG, "Image deletion should be handled server-side");
    }

    public interface CloudinaryUploadCallback {
        void onUploadStart();
        void onUploadProgress(int progress);
        void onUploadSuccess(String imageUrl, String publicId);
        void onUploadError(String error);
    }
}

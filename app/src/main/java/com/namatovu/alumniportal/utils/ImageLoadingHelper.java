package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * Image loading utility with optimized caching and performance configurations
 */
public class ImageLoadingHelper {
    private static final String TAG = "ImageLoadingHelper";
    
    // Default placeholder and error images
    private static final int DEFAULT_PLACEHOLDER = android.R.drawable.ic_menu_gallery;
    private static final int DEFAULT_ERROR = android.R.drawable.ic_menu_close_clear_cancel;
    
    /**
     * Load image with default options
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, DEFAULT_PLACEHOLDER, DEFAULT_ERROR);
    }
    
    /**
     * Load image with custom placeholder and error images
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, 
                               int placeholderResId, int errorResId) {
        if (context == null || imageView == null) return;
        
        RequestOptions options = new RequestOptions()
            .placeholder(placeholderResId)
            .error(errorResId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .timeout(10000); // 10 second timeout
            
        Glide.with(context)
            .load(imageUrl)
            .apply(options)
            .listener(new LoadingListener(imageUrl))
            .into(imageView);
    }
    
    /**
     * Load circular profile image
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        RequestOptions options = new RequestOptions()
            .placeholder(DEFAULT_PLACEHOLDER)
            .error(DEFAULT_ERROR)
            .transform(new CircleCrop())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .timeout(10000);
            
        Glide.with(context)
            .load(imageUrl)
            .apply(options)
            .listener(new LoadingListener(imageUrl))
            .into(imageView);
    }
    
    /**
     * Load image with rounded corners
     */
    public static void loadRoundedImage(Context context, String imageUrl, ImageView imageView, int cornerRadius) {
        if (context == null || imageView == null) return;
        
        RequestOptions options = new RequestOptions()
            .placeholder(DEFAULT_PLACEHOLDER)
            .error(DEFAULT_ERROR)
            .transform(new RoundedCorners(cornerRadius))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .timeout(10000);
            
        Glide.with(context)
            .load(imageUrl)
            .apply(options)
            .listener(new LoadingListener(imageUrl))
            .into(imageView);
    }
    
    /**
     * Load image from URI (for local files)
     */
    public static void loadImageFromUri(Context context, Uri imageUri, ImageView imageView) {
        if (context == null || imageView == null || imageUri == null) return;
        
        RequestOptions options = new RequestOptions()
            .placeholder(DEFAULT_PLACEHOLDER)
            .error(DEFAULT_ERROR)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Don't cache local files
            .timeout(5000);
            
        Glide.with(context)
            .load(imageUri)
            .apply(options)
            .listener(new LoadingListener(imageUri.toString()))
            .into(imageView);
    }
    
    /**
     * Load thumbnail image with reduced quality for better performance
     */
    public static void loadThumbnail(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        RequestOptions options = new RequestOptions()
            .placeholder(DEFAULT_PLACEHOLDER)
            .error(DEFAULT_ERROR)
            .override(200, 200) // Resize to thumbnail size
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .timeout(5000);
            
        Glide.with(context)
            .load(imageUrl)
            .apply(options)
            .listener(new LoadingListener(imageUrl))
            .into(imageView);
    }
    
    /**
     * Load image with high quality for detail views
     */
    public static void loadHighQualityImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        RequestOptions options = new RequestOptions()
            .placeholder(DEFAULT_PLACEHOLDER)
            .error(DEFAULT_ERROR)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .timeout(15000) // Longer timeout for high quality
            .dontTransform(); // Keep original quality
            
        Glide.with(context)
            .load(imageUrl)
            .apply(options)
            .listener(new LoadingListener(imageUrl))
            .into(imageView);
    }
    
    /**
     * Load image as bitmap (for manipulation)
     */
    public static void loadImageAsBitmap(Context context, String imageUrl, BitmapLoadCallback callback) {
        if (context == null || callback == null) return;
        
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(new BitmapTarget(callback));
    }
    
    /**
     * Preload image into cache
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (context == null || imageUrl == null) return;
        
        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .preload();
    }
    
    /**
     * Clear image cache
     */
    public static void clearCache(Context context) {
        if (context == null) return;
        
        // Clear memory cache
        Glide.get(context).clearMemory();
        
        // Clear disk cache (must be called on background thread)
        new Thread(() -> {
            try {
                Glide.get(context).clearDiskCache();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing disk cache", e);
            }
        }).start();
    }
    
    /**
     * Get cache size
     */
    public static long getCacheSize(Context context) {
        if (context == null) return 0;
        
        try {
            return Glide.getPhotoCacheDir(context).length();
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache size", e);
            return 0;
        }
    }
    
    /**
     * Pause image loading (for scrolling performance)
     */
    public static void pauseRequests(Context context) {
        if (context != null) {
            Glide.with(context).pauseRequests();
        }
    }
    
    /**
     * Resume image loading
     */
    public static void resumeRequests(Context context) {
        if (context != null) {
            Glide.with(context).resumeRequests();
        }
    }
    
    // Request listener for logging and error handling
    private static class LoadingListener implements RequestListener<Drawable> {
        private final String imageUrl;
        
        LoadingListener(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            Log.w(TAG, "Failed to load image: " + imageUrl, e);
            AnalyticsHelper.logError("image_load_failed", e != null ? e.getMessage() : "Unknown error", imageUrl);
            return false; // Allow error placeholder to be shown
        }
        
        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            Log.d(TAG, "Successfully loaded image: " + imageUrl + " from " + dataSource);
            return false; // Allow image to be displayed
        }
    }
    
    // Bitmap callback interface
    public interface BitmapLoadCallback {
        void onBitmapLoaded(Bitmap bitmap);
        void onLoadFailed(Exception e);
    }
    
    // Custom bitmap target
    private static class BitmapTarget extends com.bumptech.glide.request.target.CustomTarget<Bitmap> {
        private final BitmapLoadCallback callback;
        
        BitmapTarget(BitmapLoadCallback callback) {
            this.callback = callback;
        }
        
        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
            callback.onBitmapLoaded(resource);
        }
        
        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            // Handle cleanup if needed
        }
        
        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            callback.onLoadFailed(new Exception("Failed to load bitmap"));
        }
    }
}
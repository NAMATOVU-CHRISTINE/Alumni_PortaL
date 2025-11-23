package com.namatovu.alumniportal.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

import com.google.android.material.card.MaterialCardView;
import com.namatovu.alumniportal.R;

/**
 * Helper for showing in-app notifications/toasts
 */
public class InAppNotificationHelper {
    
    public static void showNotification(Activity activity, String title, String message, String type) {
        if (activity == null) return;
        
        activity.runOnUiThread(() -> {
            // Create notification view
            FrameLayout rootView = activity.findViewById(android.R.id.content);
            if (rootView == null) return;
            
            View notificationView = LayoutInflater.from(activity).inflate(R.layout.layout_in_app_notification, null);
            
            MaterialCardView cardView = notificationView.findViewById(R.id.notificationCard);
            TextView titleText = notificationView.findViewById(R.id.notificationTitle);
            TextView messageText = notificationView.findViewById(R.id.notificationMessage);
            
            // Set content
            titleText.setText(title);
            messageText.setText(message);
            
            // Set color based on type
            int backgroundColor = getBackgroundColor(activity, type);
            cardView.setCardBackgroundColor(backgroundColor);
            
            // Add to root view
            rootView.addView(notificationView);
            
            // Animate in
            animateIn(notificationView);
            
            // Auto-dismiss after 5 seconds
            notificationView.postDelayed(() -> {
                animateOut(notificationView, () -> rootView.removeView(notificationView));
            }, 5000);
            
            // Click to dismiss
            notificationView.setOnClickListener(v -> {
                animateOut(notificationView, () -> rootView.removeView(notificationView));
            });
        });
    }
    
    private static int getBackgroundColor(Activity activity, String type) {
        switch (type) {
            case "mentorship":
            case "mentorship_request":
                return activity.getResources().getColor(R.color.light_green, null);
            case "event":
                return activity.getResources().getColor(R.color.light_blue, null);
            case "message":
                return activity.getResources().getColor(R.color.light_green, null);
            case "job":
                return activity.getResources().getColor(R.color.light_orange, null);
            default:
                return activity.getResources().getColor(R.color.light_green, null);
        }
    }
    
    private static void animateIn(View view) {
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", -500f, 0f);
        translateY.setDuration(300);
        
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alpha.setDuration(300);
        
        AnimatorSet set = new AnimatorSet();
        set.playTogether(translateY, alpha);
        set.start();
    }
    
    private static void animateOut(View view, Runnable onComplete) {
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", 0f, -500f);
        translateY.setDuration(300);
        
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        alpha.setDuration(300);
        
        AnimatorSet set = new AnimatorSet();
        set.playTogether(translateY, alpha);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (onComplete != null) onComplete.run();
            }
        });
        set.start();
    }
}

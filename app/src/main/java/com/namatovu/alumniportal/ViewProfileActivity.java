package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.databinding.ActivityViewProfileBinding;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.model.MentorshipRequest;
import com.namatovu.alumniportal.services.NotificationService;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;
import java.util.HashMap;
import java.util.Map;

public class ViewProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewProfileActivity";
    private ActivityViewProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private User viewedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get userId BEFORE inflating layout
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "Loading profile for userId: " + userId);
        
        // Now inflate layout
        binding = ActivityViewProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupToolbar();
        
        // Immediately clear all UI to prevent showing cached data
        clearProfileUI();
        
        loadUserProfile();
        setupClickListeners();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Reload profile data when activity comes to foreground
        // This ensures fresh data is always displayed
        if (userId != null && !userId.isEmpty()) {
            Log.d(TAG, "onStart - reloading profile for userId: " + userId);
            clearProfileUI();
            loadUserProfile();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        binding.btnRequestMentorship.setOnClickListener(v -> showMentorshipRequestDialog());
        binding.btnShareProfile.setOnClickListener(v -> shareProfile());
        binding.btnCopyLink.setOnClickListener(v -> copyProfileLink());
        binding.tvViewMoreBio.setOnClickListener(v -> toggleBioExpanded());
    }
    
    private void copyProfileLink() {
        String profileLink = "https://alumni-portal.app/profile/" + userId;
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Profile Link", profileLink);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Profile link copied!", Toast.LENGTH_SHORT).show();
    }
    
    private boolean bioExpanded = false;
    private void toggleBioExpanded() {
        if (bioExpanded) {
            binding.tvBio.setMaxLines(3);
            binding.tvViewMoreBio.setText("View More");
            bioExpanded = false;
        } else {
            binding.tvBio.setMaxLines(Integer.MAX_VALUE);
            binding.tvViewMoreBio.setText("View Less");
            bioExpanded = true;
        }
    }
    
    private void shareProfile() {
        if (viewedUser == null) {
            Toast.makeText(this, "Profile not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create share content
        String shareText = "Check out " + viewedUser.getFullName() + "'s profile on Alumni Portal!\n\n";
        
        if (viewedUser.getCurrentJob() != null && !viewedUser.getCurrentJob().isEmpty()) {
            shareText += "Position: " + viewedUser.getCurrentJob();
            if (viewedUser.getCompany() != null && !viewedUser.getCompany().isEmpty()) {
                shareText += " at " + viewedUser.getCompany();
            }
            shareText += "\n";
        }
        
        if (viewedUser.getMajor() != null && !viewedUser.getMajor().isEmpty()) {
            shareText += "Major: " + viewedUser.getMajor();
            if (viewedUser.getGraduationYear() != null && !viewedUser.getGraduationYear().isEmpty()) {
                shareText += " (Class of " + viewedUser.getGraduationYear() + ")";
            }
            shareText += "\n";
        }
        
        shareText += "\nConnect with fellow MUST alumni!";
        
        // Create implicit intent for sharing
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Alumni Profile - " + viewedUser.getFullName());
        
        // Create chooser to let user pick sharing app
        Intent chooser = Intent.createChooser(shareIntent, "Share profile via");
        
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, "No apps available for sharing", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        Log.d(TAG, "loadUserProfile called for userId: " + userId);
        
        // Clear previous user data and UI immediately
        viewedUser = null;
        clearProfileUI();
        
        // Show loading state
        binding.tvFullName.setText("Loading...");
        binding.tvFullName.setVisibility(View.VISIBLE);
        
        // Try cache first, then server
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                Log.d(TAG, "Document fetched for userId: " + userId);
                if (document.exists()) {
                    try {
                        // Clear UI again right before displaying new data
                        clearProfileUI();
                        
                        viewedUser = document.toObject(User.class);
                        if (viewedUser != null) {
                            String fullName = viewedUser.getFullName();
                            Log.d(TAG, "Displaying profile for: " + fullName);
                            
                            // If fullName is empty, show a default message
                            if (fullName == null || fullName.trim().isEmpty()) {
                                Log.w(TAG, "User fullName is empty for userId: " + userId);
                                binding.tvFullName.setText("Profile Incomplete");
                            } else {
                                displayUserProfile(viewedUser);
                            }
                        } else {
                            Log.w(TAG, "User object is null");
                            binding.tvFullName.setText("Unable to load profile");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user data", e);
                        Toast.makeText(ViewProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Document does not exist for userId: " + userId);
                    Toast.makeText(ViewProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading profile for userId: " + userId, e);
                Toast.makeText(ViewProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void clearProfileUI() {
        try {
            // AGGRESSIVELY clear all UI elements to prevent showing stale data
            
            // Header - set to empty/default
            binding.tvFullName.setText("");
            binding.tvFullName.setVisibility(View.GONE);
            binding.profileImage.setImageResource(R.drawable.ic_person);
            binding.verificationBadge.setVisibility(View.GONE);
            binding.verificationBadgeSmall.setVisibility(View.GONE);
            binding.statusBadge.setVisibility(View.GONE);
            binding.statusBadge.setText("");
            
            // Profile info
            binding.tvCurrentJob.setText("");
            binding.tvCurrentJob.setVisibility(View.GONE);
            binding.locationContainer.setVisibility(View.GONE);
            binding.tvLocation.setText("");
            
            // Cards - hide all
            binding.bioCard.setVisibility(View.GONE);
            binding.tvBio.setText("");
            binding.majorContainer.setVisibility(View.GONE);
            binding.tvMajor.setText("");
            binding.graduationContainer.setVisibility(View.GONE);
            binding.tvGraduationYear.setText("");
            binding.companyContainer.setVisibility(View.GONE);
            binding.tvCompany.setText("");
            binding.skillsCard.setVisibility(View.GONE);
            binding.contactCard.setVisibility(View.GONE);
            binding.tvEmail.setText("");
            binding.tvPhone.setText("");
            
            // Progress
            binding.tvProfileCompletion.setText("");
            binding.tvProfileCompletion.setVisibility(View.GONE);
            binding.profileCompletionBar.setVisibility(View.GONE);
            binding.profileCompletionBar.setProgress(0);
            
            // Clear collections
            binding.skillsChipGroup.removeAllViews();
            
            // View more bio
            binding.tvViewMoreBio.setVisibility(View.GONE);
            binding.tvViewMoreBio.setText("");
            
            Log.d(TAG, "Profile UI cleared completely");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing profile UI", e);
        }
    }

    private void displayUserProfile(User user) {
        // Check if viewing own profile or someone else's
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        boolean isOwnProfile = currentUserId != null && currentUserId.equals(userId);
        
        // Profile image - always visible
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            ImageLoadingHelper.loadProfileImage(this, user.getProfileImageUrl(), binding.profileImage);
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_person);
        }

        // Basic info - always visible
        binding.tvFullName.setText(user.getFullName() != null ? user.getFullName() : "Alumni User");
        
        // Show verification badge if email is verified
        if (user.isEmailVerified()) {
            binding.verificationBadge.setVisibility(View.VISIBLE);
            binding.verificationBadgeSmall.setVisibility(View.VISIBLE);
        }
        
        // Show user type status badge
        // User Types:
        // - "student" = Current students
        // - "alumni" = Graduated students
        // - "staff" = Faculty/staff members
        String userType = user.getUserType();
        if ("alumni".equalsIgnoreCase(userType)) {
            binding.statusBadge.setText("Alumni");
            binding.statusBadge.setChipBackgroundColorResource(R.color.must_green);
            binding.statusBadge.setTextColor(getResources().getColor(R.color.white, null));
        } else if ("staff".equalsIgnoreCase(userType)) {
            binding.statusBadge.setText("Staff");
            binding.statusBadge.setChipBackgroundColorResource(R.color.must_gold);
            binding.statusBadge.setTextColor(getResources().getColor(R.color.black, null));
        } else {
            binding.statusBadge.setText("Student");
            binding.statusBadge.setChipBackgroundColorResource(R.color.light_gray);
            binding.statusBadge.setTextColor(getResources().getColor(R.color.black, null));
        }
        binding.statusBadge.setVisibility(View.VISIBLE);
        
        // Last active status removed - was showing incorrect data
        // displayLastActiveStatus(user);
        
        // Calculate and display profile completion - ONLY for own profile
        if (isOwnProfile) {
            calculateProfileCompletion(user);
            binding.tvProfileCompletion.setVisibility(View.VISIBLE);
            binding.profileCompletionBar.setVisibility(View.VISIBLE);
        } else {
            binding.tvProfileCompletion.setVisibility(View.GONE);
            binding.profileCompletionBar.setVisibility(View.GONE);
        }
        
        // Hide Share and Copy buttons for other users - ONLY show Request Mentorship
        if (isOwnProfile) {
            binding.btnShareProfile.setVisibility(View.VISIBLE);
            binding.btnCopyLink.setVisibility(View.VISIBLE);
            binding.btnRequestMentorship.setVisibility(View.GONE);
        } else {
            binding.btnShareProfile.setVisibility(View.GONE);
            binding.btnCopyLink.setVisibility(View.GONE);
            // Show mentorship button only if user allows requests
            if (user.getPrivacySetting("allowMentorRequests")) {
                binding.btnRequestMentorship.setVisibility(View.VISIBLE);
            } else {
                binding.btnRequestMentorship.setVisibility(View.GONE);
            }
        }
        
        // Current job
        if (user.getCurrentJob() != null && !user.getCurrentJob().isEmpty()) {
            String jobText = user.getCurrentJob();
            if (user.getCompany() != null && !user.getCompany().isEmpty()) {
                jobText += " at " + user.getCompany();
            }
            binding.tvCurrentJob.setText(jobText);
            binding.tvCurrentJob.setVisibility(View.VISIBLE);
        } else {
            binding.tvCurrentJob.setVisibility(View.GONE);
        }

        // Location - respect privacy
        if (user.getPrivacySetting("showLocation") && user.getLocation() != null && !user.getLocation().isEmpty()) {
            binding.tvLocation.setText(user.getLocation());
            binding.locationContainer.setVisibility(View.VISIBLE);
        } else {
            binding.locationContainer.setVisibility(View.GONE);
        }

        // Bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            binding.tvBio.setText(user.getBio());
            binding.bioCard.setVisibility(View.VISIBLE);
        } else {
            binding.bioCard.setVisibility(View.GONE);
        }

        // Education info
        boolean hasEducationInfo = false;
        
        if (user.getMajor() != null && !user.getMajor().isEmpty()) {
            binding.tvMajor.setText(user.getMajor());
            binding.majorContainer.setVisibility(View.VISIBLE);
            hasEducationInfo = true;
        } else {
            binding.majorContainer.setVisibility(View.GONE);
        }

        if (user.getGraduationYear() != null && !user.getGraduationYear().isEmpty()) {
            binding.tvGraduationYear.setText(user.getGraduationYear());
            binding.graduationContainer.setVisibility(View.VISIBLE);
            hasEducationInfo = true;
        } else {
            binding.graduationContainer.setVisibility(View.GONE);
        }

        if (user.getCompany() != null && !user.getCompany().isEmpty()) {
            binding.tvCompany.setText(user.getCompany());
            binding.companyContainer.setVisibility(View.VISIBLE);
            hasEducationInfo = true;
        } else {
            binding.companyContainer.setVisibility(View.GONE);
        }

        // Skills
        if (user.getSkills() != null && !user.getSkills().isEmpty()) {
            binding.skillsChipGroup.removeAllViews();
            for (String skill : user.getSkills()) {
                Chip chip = new Chip(this);
                chip.setText(skill);
                chip.setChipBackgroundColorResource(R.color.light_green);
                chip.setTextColor(getResources().getColor(R.color.must_green, null));
                binding.skillsChipGroup.addView(chip);
            }
            binding.skillsCard.setVisibility(View.VISIBLE);
        } else {
            binding.skillsCard.setVisibility(View.GONE);
        }

        // Contact info - respect privacy
        boolean hasContactInfo = false;
        
        if (user.getPrivacySetting("showEmail") && user.getEmail() != null && !user.getEmail().isEmpty()) {
            binding.tvEmail.setText(user.getEmail());
            binding.emailContainer.setVisibility(View.VISIBLE);
            hasContactInfo = true;
        } else {
            binding.emailContainer.setVisibility(View.GONE);
        }

        if (user.getPrivacySetting("showPhone") && user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            binding.tvPhone.setText(user.getPhoneNumber());
            binding.phoneContainer.setVisibility(View.VISIBLE);
            hasContactInfo = true;
        } else {
            binding.phoneContainer.setVisibility(View.GONE);
        }

        binding.contactCard.setVisibility(hasContactInfo ? View.VISIBLE : View.GONE);
    }

    private void showMentorshipRequestDialog() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to request mentorship", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mentorship_request, null);
        TextInputEditText etTopic = dialogView.findViewById(R.id.etTopic);
        TextInputEditText etMessage = dialogView.findViewById(R.id.etMessage);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btnSend).setOnClickListener(v -> {
            String topic = etTopic.getText() != null ? etTopic.getText().toString().trim() : "";
            String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";

            if (topic.isEmpty()) {
                etTopic.setError("Please enter a topic");
                return;
            }

            if (message.isEmpty()) {
                etMessage.setError("Please enter a message");
                return;
            }

            sendMentorshipRequest(topic, message);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void sendMentorshipRequest(String topic, String message) {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        final String mentorId = userId;
        
        // Get current user's name first
        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener(currentUserDoc -> {
                final String currentUserName = currentUserDoc.getString("fullName") != null ? 
                    currentUserDoc.getString("fullName") : "Unknown User";
                
                // Create mentorship connection (not just request)
                Map<String, Object> connectionData = new HashMap<>();
                connectionData.put("menteeId", currentUserId);
                connectionData.put("menteeName", currentUserName);
                connectionData.put("mentorId", mentorId);
                connectionData.put("mentorName", viewedUser != null ? viewedUser.getFullName() : "Mentor");
                connectionData.put("mentorTitle", viewedUser != null ? viewedUser.getCurrentJob() : "");
                connectionData.put("mentorCompany", viewedUser != null ? viewedUser.getCompany() : "");
                connectionData.put("mentorImageUrl", viewedUser != null ? viewedUser.getProfileImageUrl() : "");
                connectionData.put("topic", topic);
                connectionData.put("message", message);
                connectionData.put("status", "pending");
                connectionData.put("createdAt", new java.util.Date());
                connectionData.put("updatedAt", new java.util.Date());

                db.collection("mentorships")
                    .add(connectionData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Mentorship request sent successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Show in-app notification
                        com.namatovu.alumniportal.utils.InAppNotificationHelper.showNotification(
                            ViewProfileActivity.this,
                            "Mentorship Request Sent",
                            "Your request has been sent to " + (viewedUser != null ? viewedUser.getFullName() : "the mentor"),
                            "mentorship"
                        );
                        
                        // Send both email and push notification to mentor
                        sendMentorshipNotifications(currentUserId, currentUserName, mentorId, documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error sending mentorship request", e);
                        Toast.makeText(this, "Failed to send request. Please try again.", Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting current user info", e);
                Toast.makeText(this, "Failed to send request. Please try again.", Toast.LENGTH_SHORT).show();
            });
    }

    private void sendMentorshipNotifications(String fromUserId, String fromUserName, String toUserId, String connectionId) {
        // Get mentor's email and name
        db.collection("users").document(toUserId).get()
            .addOnSuccessListener(mentorDoc -> {
                if (mentorDoc.exists()) {
                    String mentorEmail = mentorDoc.getString("email");
                    String mentorName = mentorDoc.getString("fullName");
                    
                    if (mentorEmail != null && mentorName != null) {
                        // Use NotificationService to send both push and email
                        NotificationService notificationService = new NotificationService(this);
                        notificationService.sendMentorshipRequestNotification(
                            toUserId,
                            mentorEmail,
                            mentorName,
                            fromUserId,
                            fromUserName,
                            connectionId
                        );
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to get mentor info for notifications", e));
    }
    
    private void calculateProfileCompletion(User user) {
        int completionScore = 0;
        int totalFields = 10;
        
        // Check each field
        if (user.getFullName() != null && !user.getFullName().isEmpty()) completionScore++;
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) completionScore++;
        if (user.getBio() != null && !user.getBio().isEmpty()) completionScore++;
        if (user.getMajor() != null && !user.getMajor().isEmpty()) completionScore++;
        if (user.getGraduationYear() != null && !user.getGraduationYear().isEmpty()) completionScore++;
        if (user.getCurrentJob() != null && !user.getCurrentJob().isEmpty()) completionScore++;
        if (user.getCompany() != null && !user.getCompany().isEmpty()) completionScore++;
        if (user.getLocation() != null && !user.getLocation().isEmpty()) completionScore++;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) completionScore++;
        if (user.getSkills() != null && !user.getSkills().isEmpty()) completionScore++;
        
        int percentage = (completionScore * 100) / totalFields;
        binding.tvProfileCompletion.setText(percentage + "%");
        binding.profileCompletionBar.setProgress(percentage);
    }
    
    private void displayLastActiveStatus(User user) {
        long lastActiveTime = user.getLastActive();
        if (lastActiveTime > 0) {
            long currentTime = System.currentTimeMillis();
            long diffMillis = currentTime - lastActiveTime;
            
            String statusText;
            if (diffMillis < 300000) { // Less than 5 minutes - show as online
                statusText = "🟢 Active now";
            } else if (diffMillis < 3600000) { // Less than 1 hour
                long minutes = diffMillis / 60000;
                statusText = "Active " + minutes + "m ago";
            } else if (diffMillis < 86400000) { // Less than 1 day
                long hours = diffMillis / 3600000;
                statusText = "Active " + hours + "h ago";
            } else {
                long days = diffMillis / 86400000;
                statusText = "Active " + days + "d ago";
            }
            binding.tvLastActive.setText(statusText);
        } else {
            binding.tvLastActive.setText("Offline");
        }
    }

}
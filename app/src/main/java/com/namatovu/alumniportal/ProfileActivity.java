package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.databinding.ActivityProfileBinding;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;

import java.util.List;

/**
 * Profile view activity — shows profile and navigates to edit screen.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);

        binding.editProfileFab.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setLoadingState(true);
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            setLoadingState(false);
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    try {
                        User u = doc.toObject(User.class);
                        if (u != null) updateUi(u);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to deserialize user", e);
                    }
                }
            } else {
                Log.e(TAG, "Failed to load user profile", task.getException());
            }
        });
    }

    private void updateUi(@NonNull User user) {
        binding.nameText.setText(user.getName());
        binding.emailText.setText(user.getEmail());
        binding.bioText.setText(user.getBio());
        binding.careerText.setText(user.getCareer());

        // profile image
        String url = user.getProfileImageUrl();
        if (url != null && !url.isEmpty()) {
            ImageLoadingHelper.loadProfileImage(this, url, binding.profileImage);
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_person);
        }

        // skills
        binding.skillsChipGroup.removeAllViews();
        List<String> skills = user.getSkills();
        for (String s : skills) {
            Chip c = new Chip(this);
            c.setText(s);
            c.setClickable(false);
            c.setCheckable(false);
            binding.skillsChipGroup.addView(c);
        }
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            binding.nameText.setText("Loading...");
        }
    }
}

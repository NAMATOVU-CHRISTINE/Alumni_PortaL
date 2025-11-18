package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.databinding.ItemAlumniBinding;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;
import com.namatovu.alumniportal.utils.PerformanceHelper;

import java.util.List;

/**
 * Adapter for displaying alumni in the directory RecyclerView
 */
public class AlumniAdapter extends RecyclerView.Adapter<AlumniAdapter.AlumniViewHolder> {
    
    private List<User> users;
    private OnUserClickListener listener;
    
    public interface OnUserClickListener {
        void onUserClick(User user);
        void onEmailClick(User user);
    }
    
    public AlumniAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AlumniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlumniBinding binding = ItemAlumniBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AlumniViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AlumniViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    class AlumniViewHolder extends RecyclerView.ViewHolder {
        private ItemAlumniBinding binding;
        
        public AlumniViewHolder(ItemAlumniBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(users.get(position));
                }
            });
            
            // Email button click listener
            binding.btnEmail.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEmailClick(users.get(position));
                }
            });
        }
        
        public void bind(User user) {
            // Start performance timing for bind operation
            PerformanceHelper.getInstance().startTiming("alumni_adapter_bind");
            
            // Basic info
            binding.nameText.setText(user.getFullName());
            binding.majorText.setText(user.getMajor() != null ? user.getMajor() : "Major not specified");
            binding.yearText.setText(user.getGraduationYear() != null ? 
                    "Class of " + user.getGraduationYear() : "Graduation year not specified");
            
            // Current position (if user allows it to be shown)
            if (user.getPrivacySetting("showCurrentJob") && user.getCurrentJob() != null) {
                String jobInfo = user.getCurrentJob();
                if (user.getCompany() != null) {
                    jobInfo += " at " + user.getCompany();
                }
                binding.currentJobText.setText(jobInfo);
                binding.currentJobText.setVisibility(View.VISIBLE);
            } else {
                binding.currentJobText.setVisibility(View.GONE);
            }
            
            // Location (if user allows it to be shown)
            if (user.getPrivacySetting("showLocation") && user.getLocation() != null) {
                binding.locationText.setText(user.getLocation());
                binding.locationText.setVisibility(View.VISIBLE);
            } else {
                binding.locationText.setVisibility(View.GONE);
            }
            
            // Bio (truncated)
            if (user.getBio() != null && !user.getBio().isEmpty()) {
                String bio = user.getBio();
                if (bio.length() > 100) {
                    bio = bio.substring(0, 97) + "...";
                }
                binding.bioText.setText(bio);
                binding.bioText.setVisibility(View.VISIBLE);
            } else {
                binding.bioText.setVisibility(View.GONE);
            }
            
            // Skills (first 3 only)
            if (user.getSkills() != null && !user.getSkills().isEmpty()) {
                StringBuilder skillsText = new StringBuilder();
                int count = Math.min(3, user.getSkills().size());
                for (int i = 0; i < count; i++) {
                    if (i > 0) skillsText.append(" • ");
                    skillsText.append(user.getSkills().get(i));
                }
                if (user.getSkills().size() > 3) {
                    skillsText.append(" +").append(user.getSkills().size() - 3).append(" more");
                }
                binding.skillsText.setText(skillsText.toString());
                binding.skillsText.setVisibility(View.VISIBLE);
            } else {
                binding.skillsText.setVisibility(View.GONE);
            }
            
            // Profile image using optimized image loading
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                ImageLoadingHelper.loadProfileImage(
                    binding.getRoot().getContext(),
                    user.getProfileImageUrl(),
                    binding.profileImage
                );
            } else {
                binding.profileImage.setImageResource(R.drawable.ic_person);
            }
            
            // Verified badge
            if (user.isVerified()) {
                binding.verifiedBadge.setVisibility(View.VISIBLE);
            } else {
                binding.verifiedBadge.setVisibility(View.GONE);
            }
            
            // Mentor availability indicator
            if (user.getPrivacySetting("allowMentorRequests")) {
                binding.mentorAvailableText.setVisibility(View.VISIBLE);
                binding.mentorAvailableText.setText("Available for mentoring");
            } else {
                binding.mentorAvailableText.setVisibility(View.GONE);
            }
            
            // Email button visibility (show if user allows email to be shown)
            if (user.getPrivacySetting("showEmail") && user.getEmail() != null && !user.getEmail().isEmpty()) {
                binding.btnEmail.setVisibility(View.VISIBLE);
            } else {
                binding.btnEmail.setVisibility(View.GONE);
            }
            
            // End performance timing
            PerformanceHelper.getInstance().endTiming("alumni_adapter_bind");
        }
    }
}
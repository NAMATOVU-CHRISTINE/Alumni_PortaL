package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.databinding.ItemAlumniBinding;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;
import com.namatovu.alumniportal.utils.PerformanceHelper;

import java.util.List;

/**
 * Adapter for displaying almater (students and staff) in the directory RecyclerView
 * Similar to AlumniAdapter but without mentoring options since these are students
 */
public class AlmaterAdapter extends RecyclerView.Adapter<AlmaterAdapter.AlmaterViewHolder> {
    
    private List<User> users;
    private OnUserClickListener listener;
    
    public interface OnUserClickListener {
        void onUserClick(User user);
        void onEmailClick(User user);
    }
    
    public AlmaterAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AlmaterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlumniBinding binding = ItemAlumniBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AlmaterViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AlmaterViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    class AlmaterViewHolder extends RecyclerView.ViewHolder {
        private ItemAlumniBinding binding;
        
        public AlmaterViewHolder(ItemAlumniBinding binding) {
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
            PerformanceHelper.getInstance().startTiming("almater_adapter_bind");
            
            // Basic info
            binding.nameText.setText(user.getFullName());
            binding.majorText.setText(user.getMajor() != null ? user.getMajor() : "Major not specified");
            binding.yearText.setText(user.getGraduationYear() != null ? 
                    "Class of " + user.getGraduationYear() : "Year not specified");
            
            // Current position (if user allows it to be shown)
            boolean showJob = getPrivacySetting(user, "showCurrentJob", true);
            if (showJob && user.getCurrentJob() != null) {
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
            boolean showLocation = getPrivacySetting(user, "showLocation", true);
            if (showLocation && user.getLocation() != null) {
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
            
            // Connection status only - NO mentoring options for students
            if (user.isConnected()) {
                binding.mentorAvailableText.setVisibility(View.VISIBLE);
                binding.mentorAvailableText.setText("✓ Connected");
            } else {
                // Hide mentoring text for almater (students/staff)
                binding.mentorAvailableText.setVisibility(View.GONE);
            }
            
            // Email button visibility (show if user allows email to be shown)
            boolean showEmail = getPrivacySetting(user, "showEmail", false);
            if (showEmail && user.getEmail() != null && !user.getEmail().isEmpty()) {
                binding.btnEmail.setVisibility(View.VISIBLE);
            } else {
                binding.btnEmail.setVisibility(View.GONE);
            }
            
            // End performance timing
            PerformanceHelper.getInstance().endTiming("almater_adapter_bind");
        }
        
        private boolean getPrivacySetting(User user, String setting, boolean defaultValue) {
            if (user == null || user.getPrivacySettings() == null) {
                return defaultValue;
            }
            Object value = user.getPrivacySettings().get(setting);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return defaultValue;
        }
    }
}

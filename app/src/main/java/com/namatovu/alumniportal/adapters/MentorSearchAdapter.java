package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.utils.ImageLoadingHelper;

import java.util.List;

public class MentorSearchAdapter extends RecyclerView.Adapter<MentorSearchAdapter.MentorSearchViewHolder> {

    private List<User> mentors;
    private OnMentorActionListener listener;

    public interface OnMentorActionListener {
        void onViewProfile(User mentor);
        void onRequestMentorship(User mentor);
    }

    public MentorSearchAdapter(List<User> mentors, OnMentorActionListener listener) {
        this.mentors = mentors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MentorSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mentor_search, parent, false);
        return new MentorSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MentorSearchViewHolder holder, int position) {
        User mentor = mentors.get(position);
        holder.bind(mentor, listener);
    }

    @Override
    public int getItemCount() {
        return mentors.size();
    }

    static class MentorSearchViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private TextView nameText;
        private TextView positionText;
        private TextView bioText;
        private ChipGroup skillsChipGroup;
        private Button viewProfileButton;
        private Button requestMentorshipButton;

        public MentorSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            nameText = itemView.findViewById(R.id.nameText);
            positionText = itemView.findViewById(R.id.positionText);
            bioText = itemView.findViewById(R.id.bioText);
            skillsChipGroup = itemView.findViewById(R.id.skillsChipGroup);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
            requestMentorshipButton = itemView.findViewById(R.id.requestMentorshipButton);
        }

        public void bind(User mentor, OnMentorActionListener listener) {
            // Set name
            if (mentor.getFullName() != null && !mentor.getFullName().isEmpty()) {
                nameText.setText(mentor.getFullName());
            } else {
                nameText.setText("Unknown Name");
            }

            // Set position/career
            if (mentor.getCurrentJob() != null && !mentor.getCurrentJob().isEmpty()) {
                positionText.setText(mentor.getCurrentJob());
                positionText.setVisibility(View.VISIBLE);
            } else {
                positionText.setText("Alumni Member");
                positionText.setVisibility(View.VISIBLE);
            }

            // Set bio
            if (mentor.getBio() != null && !mentor.getBio().isEmpty()) {
                bioText.setText(mentor.getBio());
                bioText.setVisibility(View.VISIBLE);
            } else {
                bioText.setText("Passionate about helping fellow alumni grow");
                bioText.setVisibility(View.VISIBLE);
            }

            // Load profile image
            ImageLoadingHelper.loadProfileImage(
                itemView.getContext(),
                mentor.getProfileImageUrl(),
                profileImage
            );

            // Set up skills chips (limit to 3 for space)
            skillsChipGroup.removeAllViews();
            if (mentor.getSkills() != null && !mentor.getSkills().isEmpty()) {
                int chipCount = 0;
                for (String skill : mentor.getSkills()) {
                    if (chipCount >= 3) break; // Limit to 3 chips
                    
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(skill);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    chip.setTextSize(10);
                    chip.setChipBackgroundColorResource(R.color.light_green);
                    chip.setTextColor(itemView.getContext().getColor(R.color.must_green));
                    chip.setChipStrokeColorResource(R.color.must_green);
                    chip.setChipStrokeWidth(1.0f);
                    
                    skillsChipGroup.addView(chip);
                    chipCount++;
                }
                
                // Add "more" chip if there are additional skills
                if (mentor.getSkills().size() > 3) {
                    Chip moreChip = new Chip(itemView.getContext());
                    moreChip.setText("+" + (mentor.getSkills().size() - 3) + " more");
                    moreChip.setClickable(false);
                    moreChip.setCheckable(false);
                    moreChip.setTextSize(10);
                    moreChip.setChipBackgroundColorResource(R.color.light_gray);
                    moreChip.setTextColor(itemView.getContext().getColor(R.color.dark_gray));
                    
                    skillsChipGroup.addView(moreChip);
                }
            } else {
                // Add a default skill chip
                Chip defaultChip = new Chip(itemView.getContext());
                defaultChip.setText("Alumni");
                defaultChip.setClickable(false);
                defaultChip.setCheckable(false);
                defaultChip.setTextSize(10);
                defaultChip.setChipBackgroundColorResource(R.color.light_green);
                defaultChip.setTextColor(itemView.getContext().getColor(R.color.must_green));
                
                skillsChipGroup.addView(defaultChip);
            }

            // Set up click listeners
            viewProfileButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(mentor);
                }
            });

            requestMentorshipButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRequestMentorship(mentor);
                }
            });

            // Make the whole card clickable for viewing profile
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(mentor);
                }
            });
        }
    }
}
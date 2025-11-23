package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.database.entities.MentorEntity;

import java.util.List;

public class MentorAdapter extends RecyclerView.Adapter<MentorAdapter.MentorViewHolder> {
    
    private Context context;
    private List<MentorEntity> mentors;
    
    public MentorAdapter(Context context, List<MentorEntity> mentors) {
        this.context = context;
        this.mentors = mentors;
    }
    
    @NonNull
    @Override
    public MentorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mentor, parent, false);
        return new MentorViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MentorViewHolder holder, int position) {
        MentorEntity mentor = mentors.get(position);
        holder.bind(mentor);
    }
    
    @Override
    public int getItemCount() {
        return mentors.size();
    }
    
    class MentorViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProfile;
        private TextView textViewName;
        private TextView textViewJob;
        private TextView textViewExpertise;
        private TextView textViewCategory;
        private TextView textViewYear;
        private TextView textViewRating;
        private View viewAvailable;
        
        public MentorViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewJob = itemView.findViewById(R.id.textViewJob);
            textViewExpertise = itemView.findViewById(R.id.textViewExpertise);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewYear = itemView.findViewById(R.id.textViewYear);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            viewAvailable = itemView.findViewById(R.id.viewAvailable);
        }
        
        public void bind(MentorEntity mentor) {
            textViewName.setText(mentor.getFullName());
            textViewJob.setText(mentor.getCurrentJob() + " at " + mentor.getCompany());
            textViewExpertise.setText(mentor.getExpertise());
            textViewCategory.setText(mentor.getCategory());
            textViewYear.setText("Class of " + mentor.getGraduationYear());
            textViewRating.setText(String.format("%.1f ⭐", mentor.getRating()));
            
            // Show availability indicator
            viewAvailable.setVisibility(mentor.isAvailable() ? View.VISIBLE : View.GONE);
            
            // Load profile image
            if (mentor.getProfileImageUrl() != null && !mentor.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(mentor.getProfileImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(imageViewProfile);
            } else {
                imageViewProfile.setImageResource(R.drawable.ic_person);
            }
            
            // Click to view details
            itemView.setOnClickListener(v -> {
                // TODO: Open mentor detail activity
            });
        }
    }
}

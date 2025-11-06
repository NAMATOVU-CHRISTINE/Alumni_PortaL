package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.MentorshipConnection;

import java.util.ArrayList;
import java.util.List;

public class MentorshipAdapter extends RecyclerView.Adapter<MentorshipAdapter.MentorViewHolder> {
    
    private List<MentorshipConnection> mentorships = new ArrayList<>();
    private OnMentorClickListener listener;
    
    public interface OnMentorClickListener {
        void onMentorClick(MentorshipConnection mentorship);
        void onConnectClick(MentorshipConnection mentorship);
    }
    
    public void setOnMentorClickListener(OnMentorClickListener listener) {
        this.listener = listener;
    }
    
    public void setMentorships(List<MentorshipConnection> mentorships) {
        this.mentorships = mentorships != null ? mentorships : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MentorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mentor, parent, false);
        return new MentorViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MentorViewHolder holder, int position) {
        MentorshipConnection mentorship = mentorships.get(position);
        holder.bind(mentorship);
    }
    
    @Override
    public int getItemCount() {
        return mentorships.size();
    }
    
    class MentorViewHolder extends RecyclerView.ViewHolder {
        private TextView textMentorName;
        private TextView textPosition;
        private TextView textCompany;
        private TextView textSkills;
        private TextView textRating;
        
        public MentorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Use generic text views since the exact layout might vary
            textMentorName = itemView.findViewById(R.id.textMentorName);
            textPosition = itemView.findViewById(R.id.textPosition);
            textCompany = itemView.findViewById(R.id.textCompany);
            textSkills = itemView.findViewById(R.id.textSkills);
            textRating = itemView.findViewById(R.id.textRating);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onMentorClick(mentorships.get(getAdapterPosition()));
                }
            });
            
            View buttonConnect = itemView.findViewById(R.id.buttonConnect);
            if (buttonConnect != null) {
                buttonConnect.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onConnectClick(mentorships.get(getAdapterPosition()));
                    }
                });
            }
        }
        
        public void bind(MentorshipConnection mentorship) {
            if (mentorship == null) return;
            
            if (textMentorName != null) {
                textMentorName.setText(mentorship.getMentorName() != null ? mentorship.getMentorName() : "Unknown Mentor");
            }
            
            if (textPosition != null) {
                textPosition.setText(mentorship.getMentorTitle() != null ? mentorship.getMentorTitle() : "Professional");
            }
            
            if (textCompany != null) {
                textCompany.setText(mentorship.getMentorCompany() != null ? mentorship.getMentorCompany() : "");
            }
            
            if (textSkills != null) {
                textSkills.setText(mentorship.getSkills() != null ? mentorship.getSkills() : "Various Skills");
            }
            
            if (textRating != null) {
                // Default rating display
                textRating.setText("⭐ 4.5");
            }
        }
    }
}
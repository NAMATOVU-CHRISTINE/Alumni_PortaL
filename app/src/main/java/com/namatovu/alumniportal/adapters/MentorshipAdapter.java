package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.MentorshipConnection;
import com.namatovu.alumniportal.models.User;

import java.util.ArrayList;
import java.util.List;

public class MentorshipAdapter extends RecyclerView.Adapter<MentorshipAdapter.MentorViewHolder> {
    
    private List<User> mentors = new ArrayList<>();
    private OnMentorClickListener listener;
    private OnMentorshipActionListener actionListener;
    private String currentUserId;
    
    // Default constructor
    public MentorshipAdapter() {
    }
    
    // Constructor for mentorship connections
    public MentorshipAdapter(List<?> connections, String currentUserId, OnMentorshipActionListener actionListener) {
        this.currentUserId = currentUserId;
        this.actionListener = actionListener;
        // Note: connections parameter is not used in this simple implementation
    }
    
    public interface OnMentorClickListener {
        void onMentorClick(User mentor);
        void onConnectClick(User mentor);
    }
    
    public interface OnMentorshipActionListener {
        void onAccept(MentorshipConnection connection);
        void onReject(MentorshipConnection connection);
        void onViewProfile(String userId);
        void onStartSession(MentorshipConnection connection);
        void onCompleteConnection(MentorshipConnection connection);
    }
    
    public void setOnMentorClickListener(OnMentorClickListener listener) {
        this.listener = listener;
    }
    
    public void setMentors(List<User> mentors) {
        this.mentors = mentors != null ? mentors : new ArrayList<>();
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
        User mentor = mentors.get(position);
        holder.bind(mentor);
    }
    
    @Override
    public int getItemCount() {
        return mentors.size();
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
                        listener.onMentorClick(mentors.get(getAdapterPosition()));
                    }
                });
                
                View buttonConnect = itemView.findViewById(R.id.buttonConnect);
                if (buttonConnect != null) {
                    buttonConnect.setOnClickListener(v -> {
                        if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                            listener.onConnectClick(mentors.get(getAdapterPosition()));
                        }
                    });
                }
            }
            
            public void bind(User mentor) {
                if (mentor == null) return;
                
                if (textMentorName != null) {
                    textMentorName.setText(mentor.getFullName());
                }
                
                if (textPosition != null) {
                    textPosition.setText(mentor.getCurrentJob());
                }
                
                if (textCompany != null) {
                    textCompany.setText(mentor.getCompany());
                }
                
                if (textSkills != null) {
                    textSkills.setText(mentor.getSkillsAsString());
                }
                
                if (textRating != null) {
                    // Default rating display
                    textRating.setText("⭐ 4.5");
                }
            }
        }

    // Minimal placeholder model classes so this adapter compiles independently.
    // If your project already provides com.namatovu.alumniportal.models.User and
    // com.namatovu.alumniportal.models.MentorshipConnection, remove these placeholders.
    public static class User {
        private String fullName;
        private String currentJob;
        private String company;
        private String skills;

        public User() { }

        public String getFullName() { return fullName == null ? "" : fullName; }
        public String getCurrentJob() { return currentJob == null ? "" : currentJob; }
        public String getCompany() { return company == null ? "" : company; }
        public String getSkillsAsString() { return skills == null ? "" : skills; }

        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setCurrentJob(String currentJob) { this.currentJob = currentJob; }
        public void setCompany(String company) { this.company = company; }
        public void setSkills(String skills) { this.skills = skills; }
    }

    public static class MentorshipConnection {
        private String id;

        public MentorshipConnection() { }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}

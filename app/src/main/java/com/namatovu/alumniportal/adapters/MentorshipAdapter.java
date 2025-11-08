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
    
    private List<MentorshipConnection> connections = new ArrayList<>();
    private OnMentorClickListener listener;
    private OnMentorshipActionListener actionListener;
    private String currentUserId;
    
    // Default constructor
    public MentorshipAdapter() {
    }
    
    // Constructor for mentorship connections
    public MentorshipAdapter(List<MentorshipConnection> connections, String currentUserId, OnMentorshipActionListener actionListener) {
        this.connections = connections != null ? connections : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.actionListener = actionListener;
    }
    
    public interface OnMentorClickListener {
        void onMentorClick(MentorshipConnection connection);
        void onConnectClick(MentorshipConnection connection);
    }
    
    public interface OnMentorshipActionListener {
        void onAccept(MentorshipConnection connection);
        void onReject(MentorshipConnection connection);
        void onViewProfile(String userId);
        void onStartSession(MentorshipConnection connection);
        void onCompleteConnection(MentorshipConnection connection);
        void onRequestMentorship(MentorshipConnection connection);
    }
    
    public void setOnMentorClickListener(OnMentorClickListener listener) {
        this.listener = listener;
    }
    
    public void setMentors(List<User> mentors) {
        // Convert User list to MentorshipConnection list for compatibility
        this.connections.clear();
        if (mentors != null) {
            for (User mentor : mentors) {
                MentorshipConnection connection = new MentorshipConnection();
                connection.setMentorId(mentor.getUserId());
                connection.setMentorName(mentor.getFullName());
                connection.setMentorTitle(mentor.getCurrentJob());
                connection.setMentorCompany(mentor.getCompany());
                connection.setStatus("available");
                this.connections.add(connection);
            }
        }
        notifyDataSetChanged();
    }
    
    public void setConnections(List<MentorshipConnection> connections) {
        this.connections = connections != null ? connections : new ArrayList<>();
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
        MentorshipConnection connection = connections.get(position);
        holder.bind(connection);
    }
    
    @Override
    public int getItemCount() {
        return connections.size();
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
                        listener.onMentorClick(connections.get(getAdapterPosition()));
                    }
                });
                
                View buttonConnect = itemView.findViewById(R.id.buttonConnect);
                if (buttonConnect != null) {
                    buttonConnect.setOnClickListener(v -> {
                        if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                            listener.onConnectClick(connections.get(getAdapterPosition()));
                        }
                    });
                }
            }
            
            public void bind(MentorshipConnection connection) {
                if (connection == null) return;
                
                if (textMentorName != null) {
                    textMentorName.setText(connection.getMentorName() != null ? connection.getMentorName() : "Unknown");
                }
                
                if (textPosition != null) {
                    textPosition.setText(connection.getMentorTitle() != null ? connection.getMentorTitle() : "Alumni");
                }
                
                if (textCompany != null) {
                    textCompany.setText(connection.getMentorCompany() != null ? connection.getMentorCompany() : "");
                }
                
                if (textSkills != null) {
                    textSkills.setVisibility(View.GONE); // Hide skills for now
                }
                
                if (textRating != null) {
                    textRating.setVisibility(View.GONE); // Hide rating for now
                }
                
                // Update button text based on connection status
                View buttonConnect = itemView.findViewById(R.id.buttonConnect);
                if (buttonConnect instanceof TextView) {
                    TextView button = (TextView) buttonConnect;
                    if ("available".equals(connection.getStatus())) {
                        button.setText("Request Mentorship");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(true);
                    } else if ("pending".equals(connection.getStatus())) {
                        button.setText("Request Pending");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(false);
                    } else {
                        button.setText("Connected");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(false);
                    }
                }
            }
        }
}

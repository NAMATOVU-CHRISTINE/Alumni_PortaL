package com.namatovu.alumniportal.adapters;

import android.util.Log;
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

    public interface OnMentorshipActionListener {
        void onAccept(MentorshipConnection connection);
        void onReject(MentorshipConnection connection);
        void onViewProfile(String userId);
        void onStartSession(MentorshipConnection connection);
        void onCompleteConnection(MentorshipConnection connection);
        void onRequestMentorship(MentorshipConnection connection);
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
                textMentorName = itemView.findViewById(R.id.textViewName);
                textPosition = itemView.findViewById(R.id.textViewJob);
                textCompany = itemView.findViewById(R.id.textViewJob); // Assuming company and job are the same text view
                textSkills = itemView.findViewById(R.id.textViewExpertise);
                textRating = itemView.findViewById(R.id.textViewRating);
                
                itemView.setOnClickListener(v -> {
                    if (actionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        // For now, just log the click - we can add profile viewing later
                        Log.d("MentorshipAdapter", "Item clicked for connection: " + connections.get(getAdapterPosition()).getMentorName());
                    }
                });
                
                View buttonConnect = itemView.findViewById(R.id.buttonConnect);
                if (buttonConnect != null) {
                    buttonConnect.setOnClickListener(v -> {
                        if (actionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                            MentorshipConnection connection = connections.get(getAdapterPosition());
                            TextView button = (TextView) buttonConnect;
                            String buttonText = button.getText().toString();
                            
                            if ("Request Mentorship".equals(buttonText)) {
                                actionListener.onRequestMentorship(connection);
                            } else if ("Accept Request".equals(buttonText)) {
                                actionListener.onAccept(connection);
                            } else if ("Chat".equals(buttonText)) {
                                actionListener.onStartSession(connection);
                            }
                        }
                    });
                    
                    // Add long press listener for reject action on pending requests
                    buttonConnect.setOnLongClickListener(v -> {
                        if (actionListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                            MentorshipConnection connection = connections.get(getAdapterPosition());
                            TextView button = (TextView) buttonConnect;
                            String buttonText = button.getText().toString();
                            
                            if ("Accept Request".equals(buttonText)) {
                                actionListener.onReject(connection);
                                return true;
                            }
                        }
                        return false;
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
                
                // Update button text and behavior based on connection status and user role
                View buttonConnect = itemView.findViewById(R.id.buttonConnect);
                if (buttonConnect instanceof TextView) {
                    TextView button = (TextView) buttonConnect;
                    
                    // Check if current user is the mentor or mentee in this connection
                    boolean isCurrentUserMentor = currentUserId != null && currentUserId.equals(connection.getMentorId());
                    boolean isCurrentUserMentee = currentUserId != null && currentUserId.equals(connection.getMenteeId());
                    
                    if (isCurrentUserMentor && "pending".equals(connection.getStatus())) {
                        // Mentor viewing pending request - show accept/reject options
                        button.setText("Accept Request");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(true);
                        
                        // Show mentee name instead of mentor name for mentor's view
                        if (textMentorName != null) {
                            textMentorName.setText(connection.getMenteeName() != null ? connection.getMenteeName() : "Unknown User");
                        }
                        if (textPosition != null) {
                            textPosition.setText("Requesting Mentorship");
                        }
                        if (textCompany != null) {
                            textCompany.setText(connection.getMessage() != null ? connection.getMessage() : "No message provided");
                        }
                    } else if (isCurrentUserMentee && "pending".equals(connection.getStatus())) {
                        // Mentee viewing their own pending request
                        button.setText("Request Pending");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(false);
                    } else if ("available".equals(connection.getStatus())) {
                        // Available mentor for mentee to request
                        button.setText("Request Mentorship");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(true);
                    } else if ("accepted".equals(connection.getStatus()) || "active".equals(connection.getStatus())) {
                        button.setText("Chat");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(true);
                    } else {
                        button.setText("Connected");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(false);
                    }
                }
            }
        }
}

package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
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
            private ImageView imageViewProfile;
            private FirebaseFirestore db;
            
            public MentorViewHolder(@NonNull View itemView) {
                super(itemView);
                // Use generic text views since the exact layout might vary
                textMentorName = itemView.findViewById(R.id.textViewName);
                textPosition = itemView.findViewById(R.id.textViewJob);
                textCompany = itemView.findViewById(R.id.textViewJob); // Assuming company and job are the same text view
                textSkills = itemView.findViewById(R.id.textViewExpertise);
                textRating = itemView.findViewById(R.id.textViewRating);
                imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
                db = FirebaseFirestore.getInstance();
                
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
                if (connection == null) {
                    Log.w("MentorshipAdapter", "Connection is null");
                    return;
                }
                
                // Check if current user is the mentor or mentee in this connection
                boolean isCurrentUserMentor = currentUserId != null && currentUserId.equals(connection.getMentorId());
                boolean isCurrentUserMentee = currentUserId != null && currentUserId.equals(connection.getMenteeId());
                
                Log.d("MentorshipAdapter", "Binding connection - isCurrentUserMentor: " + isCurrentUserMentor + 
                      ", mentorName: " + connection.getMentorName() + ", menteeName: " + connection.getMenteeName());
                
                // Show the OTHER person's name (not your own)
                String displayName = "";
                String displayTitle = "";
                String displayCompany = "";
                String otherUserId = "";
                
                if (isCurrentUserMentor) {
                    // If I'm the mentor, show the mentee's name
                    displayName = connection.getMenteeName() != null && !connection.getMenteeName().isEmpty() 
                                  ? connection.getMenteeName() : "Mentee";
                    displayTitle = "Your Mentee";
                    otherUserId = connection.getMenteeId();
                    String msg = connection.getMessage();
                    if (msg != null && !msg.isEmpty()) {
                        displayCompany = msg.length() > 50 ? msg.substring(0, 47) + "..." : msg;
                    }
                } else {
                    // If I'm the mentee (or viewing available mentors), show the mentor's name
                    displayName = connection.getMentorName() != null && !connection.getMentorName().isEmpty() 
                                  ? connection.getMentorName() : "Mentor";
                    displayTitle = connection.getMentorTitle() != null && !connection.getMentorTitle().isEmpty() 
                                   ? connection.getMentorTitle() : "Alumni";
                    displayCompany = connection.getMentorCompany() != null ? connection.getMentorCompany() : "";
                    otherUserId = connection.getMentorId();
                }
                
                if (textMentorName != null) {
                    textMentorName.setText(displayName);
                }
                if (textPosition != null) {
                    textPosition.setText(displayTitle);
                }
                if (textCompany != null) {
                    textCompany.setText(displayCompany);
                }
                
                // Load profile picture and bio from Firestore
                if (!otherUserId.isEmpty()) {
                    loadUserProfileData(otherUserId);
                }
                
                if (textSkills != null) {
                    textSkills.setVisibility(View.GONE);
                }
                
                if (textRating != null) {
                    textRating.setVisibility(View.GONE);
                }
                
                // Update button text and behavior based on connection status and user role
                View buttonConnect = itemView.findViewById(R.id.buttonConnect);
                if (buttonConnect instanceof TextView) {
                    TextView button = (TextView) buttonConnect;
                    
                    if (isCurrentUserMentor && "pending".equals(connection.getStatus())) {
                        // Mentor viewing pending request
                        button.setText("Accept Request");
                        button.setVisibility(View.VISIBLE);
                        button.setEnabled(true);
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
            
            private void loadUserProfileData(String userId) {
                db.collection("users").document(userId)
                        .addSnapshotListener((documentSnapshot, error) -> {
                            if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                                imageViewProfile.setImageResource(R.drawable.ic_person);
                                return;
                            }
                            
                            try {
                                User user = documentSnapshot.toObject(User.class);
                                if (user != null) {
                                    // Load profile picture
                                    String profileImageUrl = user.getProfileImageUrl();
                                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                        Glide.with(itemView.getContext())
                                                .load(profileImageUrl)
                                                .circleCrop()
                                                .placeholder(R.drawable.ic_person)
                                                .error(R.drawable.ic_person)
                                                .into(imageViewProfile);
                                    } else {
                                        imageViewProfile.setImageResource(R.drawable.ic_person);
                                    }
                                    
                                    // Load bio/about info
                                    String bio = user.getBio();
                                    if (textSkills != null && bio != null && !bio.isEmpty()) {
                                        textSkills.setText(bio);
                                        textSkills.setVisibility(View.VISIBLE);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("MentorshipAdapter", "Error loading user profile", e);
                                imageViewProfile.setImageResource(R.drawable.ic_person);
                            }
                        });
            }
        }
}

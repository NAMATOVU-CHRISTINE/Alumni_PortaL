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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Opportunity;
import com.namatovu.alumniportal.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the main vertical list of opportunities
 */
public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.OpportunityViewHolder> {

    private Context context;
    private List<Opportunity> opportunities = new ArrayList<>();
    private OnOpportunityClickListener listener;

    public interface OnOpportunityClickListener {
        void onOpportunityClick(Opportunity opportunity);
        void onSaveClick(Opportunity opportunity);
        void onApplyClick(Opportunity opportunity);
    }

    public OpportunityAdapter(Context context, List<Opportunity> opportunities) {
        this.context = context;
        if (opportunities != null) this.opportunities = opportunities;
    }

    public void setOnOpportunityClickListener(OnOpportunityClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OpportunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_opportunity_card, parent, false);
        return new OpportunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpportunityViewHolder holder, int position) {
        Opportunity opportunity = opportunities.get(position);
        holder.bind(opportunity);
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    public void updateList(List<Opportunity> newList) {
        this.opportunities.clear();
        if (newList != null) this.opportunities.addAll(newList);
        notifyDataSetChanged();
    }

    // Compatibility method used by JobsActivity
    public void updateOpportunities(List<Opportunity> newList) {
        updateList(newList);
    }

    public Opportunity getItem(int position) {
        return opportunities.get(position);
    }

    class OpportunityViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogo, tvTitle, tvCompany, tvLocation, tvDeadline, tvShortDesc;
        TextView tvPosterName, tvPosterBio, tvDatePosted;
        ImageView imageViewPoster;
        Chip chipCategory;
        MaterialButton btnSave;
        FirebaseFirestore db;

        public OpportunityViewHolder(@NonNull View itemView) {
            super(itemView);
            // Card layout uses tvCategoryIcon as a small circle/icon
            tvLogo = itemView.findViewById(R.id.tvCategoryIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvShortDesc = itemView.findViewById(R.id.tvDescription);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            btnSave = itemView.findViewById(R.id.btnSave);
            tvPosterName = itemView.findViewById(R.id.tvPosterName);
            tvPosterBio = itemView.findViewById(R.id.tvPosterBio);
            imageViewPoster = itemView.findViewById(R.id.imageViewPoster);
            tvDatePosted = itemView.findViewById(R.id.tvDatePosted);
            db = FirebaseFirestore.getInstance();
            MaterialButton btnApply = itemView.findViewById(R.id.btnApply);
            MaterialButton btnDetails = itemView.findViewById(R.id.btnDetails);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onOpportunityClick(opportunities.get(pos));
                    }
                }
            });

            btnSave.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    Opportunity opp = opportunities.get(pos);
                    // Delegate save action to the listener (JobsActivity will toggle/persist)
                    listener.onSaveClick(opp);
                    notifyItemChanged(pos);
                }
            });

            btnApply.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    Opportunity opp = opportunities.get(pos);
                    listener.onApplyClick(opp);
                    notifyItemChanged(pos);
                }
            });

            btnDetails.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOpportunityClick(opportunities.get(pos));
                }
            });
        }

        public void bind(Opportunity opportunity) {
            try {
                if (opportunity == null) {
                    Log.e("OpportunityAdapter", "Opportunity is null");
                    return;
                }
                
                // Load poster profile data
                if (opportunity.getPostedBy() != null && !opportunity.getPostedBy().isEmpty()) {
                    loadPosterProfile(opportunity.getPostedBy());
                }
                
                // Logo or initial
                if (opportunity.getCompanyLogo() != null && !opportunity.getCompanyLogo().isEmpty()) {
                    tvLogo.setText(opportunity.getCompanyLogo());
                } else if (opportunity.getCompany() != null && !opportunity.getCompany().isEmpty()) {
                    tvLogo.setText(opportunity.getCompany().substring(0, 1).toUpperCase());
                } else {
                    tvLogo.setText("?");
                }

                tvTitle.setText(opportunity.getTitle() != null ? opportunity.getTitle() : "");
                tvCompany.setText(opportunity.getCompany() != null ? opportunity.getCompany() : "");
                
                // Use actual description from opportunity
                String description = opportunity.getDescription();
                if (description != null && !description.isEmpty()) {
                    tvShortDesc.setText(description);
                } else {
                    String shortDesc = opportunity.getShortDescription();
                    tvShortDesc.setText(shortDesc != null ? shortDesc : "");
                }

                if (opportunity.getLocation() != null && !opportunity.getLocation().isEmpty()) {
                    tvLocation.setText("📍 " + opportunity.getLocation());
                    tvLocation.setVisibility(View.VISIBLE);
                } else {
                    tvLocation.setVisibility(View.GONE);
                }

                String deadline = opportunity.getFormattedDeadline();
                tvDeadline.setText(deadline != null ? deadline : "");
                if (opportunity.isDeadlineApproaching()) {
                    tvDeadline.setTextColor(context.getResources().getColor(R.color.orange));
                } else {
                    tvDeadline.setTextColor(context.getResources().getColor(R.color.must_green));
                }

                // Set date posted
                if (tvDatePosted != null) {
                    String datePosted = opportunity.getFormattedDatePosted();
                    tvDatePosted.setText(datePosted != null ? datePosted : "");
                }

                String category = opportunity.getCategory() != null ? opportunity.getCategory() : "Job";
                String icon = opportunity.getCategoryIcon() != null ? opportunity.getCategoryIcon() : "💼";
                chipCategory.setText(icon + " " + category);
            } catch (Exception e) {
                Log.e("OpportunityAdapter", "Error binding opportunity", e);
            }

            // Save state as text (keeps resources simple and compatible)
            if (opportunity.isSaved()) {
                btnSave.setText("Saved");
                btnSave.setContentDescription("Unsave opportunity");
            } else {
                btnSave.setText("Save");
                btnSave.setContentDescription("Save opportunity");
            }
        }
        
        private void loadPosterProfile(String userId) {
            db.collection("users").document(userId)
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                            imageViewPoster.setImageResource(R.drawable.ic_person);
                            tvPosterName.setText("Unknown");
                            tvPosterBio.setText("");
                            return;
                        }
                        
                        try {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Load profile picture
                                String profileImageUrl = user.getProfileImageUrl();
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(context)
                                            .load(profileImageUrl)
                                            .circleCrop()
                                            .placeholder(R.drawable.ic_person)
                                            .error(R.drawable.ic_person)
                                            .into(imageViewPoster);
                                } else {
                                    imageViewPoster.setImageResource(R.drawable.ic_person);
                                }
                                
                                // Load poster name
                                String fullName = user.getFullName();
                                tvPosterName.setText(fullName != null ? fullName : "Unknown");
                                
                                // Load poster bio/about
                                String bio = user.getBio();
                                if (bio != null && !bio.isEmpty()) {
                                    tvPosterBio.setText(bio);
                                } else {
                                    String job = user.getCurrentJob();
                                    tvPosterBio.setText(job != null ? job : "Alumni");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("OpportunityAdapter", "Error loading poster profile", e);
                            imageViewPoster.setImageResource(R.drawable.ic_person);
                        }
                    });
        }
    }
}

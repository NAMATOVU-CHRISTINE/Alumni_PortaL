package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Opportunity;
import java.util.List;

/**
 * Adapter for featured opportunities (horizontal RecyclerView)
 */
public class FeaturedOpportunityAdapter extends RecyclerView.Adapter<FeaturedOpportunityAdapter.FeaturedViewHolder> {

    private Context context;
    private List<Opportunity> opportunities;
    private OnFeaturedOpportunityClickListener listener;

    public interface OnFeaturedOpportunityClickListener {
        void onFeaturedOpportunityClick(Opportunity opportunity);
    }

    public FeaturedOpportunityAdapter(Context context, List<Opportunity> opportunities) {
        this.context = context;
        this.opportunities = opportunities;
    }

    public void setOnFeaturedOpportunityClickListener(OnFeaturedOpportunityClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_opportunity_featured, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Opportunity opportunity = opportunities.get(position);
        holder.bind(opportunity, position);
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    public void updateOpportunities(List<Opportunity> newOpportunities) {
        this.opportunities.clear();
        this.opportunities.addAll(newOpportunities);
        notifyDataSetChanged();
    }

    class FeaturedViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCompanyLogo, tvCompany, tvDatePosted, tvTitle, tvDescription, tvLocation, tvDeadline;
        private Chip chipCategory;
        private MaterialButton btnViewDetails;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCompanyLogo = itemView.findViewById(R.id.tvCompanyLogo);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvDatePosted = itemView.findViewById(R.id.tvDatePosted);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFeaturedOpportunityClick(opportunities.get(position));
                    }
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFeaturedOpportunityClick(opportunities.get(position));
                    }
                }
            });
        }

        public void bind(Opportunity opportunity, int position) {
            // Company logo - use first letter or emoji
            if (opportunity.getCompanyLogo() != null) {
                tvCompanyLogo.setText(opportunity.getCompanyLogo());
            } else {
                tvCompanyLogo.setText(opportunity.getCompany().substring(0, 1).toUpperCase());
            }
            
            tvCompany.setText(opportunity.getCompany());
            tvDatePosted.setText(opportunity.getFormattedDatePosted());
            tvTitle.setText(opportunity.getTitle());
            tvDescription.setText(opportunity.getShortDescription());
            
            // Location
            if (opportunity.getLocation() != null && !opportunity.getLocation().isEmpty()) {
                tvLocation.setText("📍 " + opportunity.getLocation());
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }
            
            tvDeadline.setText(opportunity.getFormattedDeadline());
            
            // Category chip
            chipCategory.setText(opportunity.getCategoryIcon() + " " + opportunity.getCategory());
            
            // Set deadline color based on urgency
            if (opportunity.isDeadlineApproaching()) {
                tvDeadline.setTextColor(context.getResources().getColor(R.color.orange));
            } else {
                tvDeadline.setTextColor(context.getResources().getColor(R.color.must_green));
            }
        }
    }
}
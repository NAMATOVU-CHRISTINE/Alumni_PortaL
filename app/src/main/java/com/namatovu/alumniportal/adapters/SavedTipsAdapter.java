package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.CareerTip;

import java.util.List;

/**
 * Adapter for displaying saved career tips in RecyclerView
 * Handles tip display and unsave functionality
 */
public class SavedTipsAdapter extends RecyclerView.Adapter<SavedTipsAdapter.TipViewHolder> {

    private List<CareerTip> savedTips;
    private OnTipUnsavedListener listener;

    public interface OnTipUnsavedListener {
        void onTipUnsaved(CareerTip tip);
    }

    public SavedTipsAdapter(List<CareerTip> savedTips, OnTipUnsavedListener listener) {
        this.savedTips = savedTips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        CareerTip tip = savedTips.get(position);
        holder.bind(tip);
    }

    @Override
    public int getItemCount() {
        return savedTips.size();
    }

    class TipViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tipText;
        private Chip categoryChip;
        private MaterialButton unsaveButton;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tipText = itemView.findViewById(R.id.tipText);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            unsaveButton = itemView.findViewById(R.id.btnUnsave);
        }

        public void bind(CareerTip tip) {
            tipText.setText(tip.getText());
            categoryChip.setText(tip.getCategory());
            
            // Set category-specific colors
            setCategoryColors(tip.getCategory());
            
            unsaveButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTipUnsaved(tip);
                }
            });
        }

        private void setCategoryColors(String category) {
            int backgroundColor, textColor;
            
            switch (category) {
                case "Networking":
                    backgroundColor = R.color.light_blue;
                    textColor = R.color.blue;
                    break;
                case "Job Search":
                    backgroundColor = R.color.light_purple;
                    textColor = R.color.purple;
                    break;
                case "Entrepreneurship":
                    backgroundColor = R.color.light_orange;
                    textColor = R.color.orange;
                    break;
                case "Skill Development":
                    backgroundColor = R.color.light_green;
                    textColor = R.color.must_green;
                    break;
                case "Productivity":
                    backgroundColor = R.color.light_yellow;
                    textColor = R.color.yellow_dark;
                    break;
                case "Financial Management":
                    backgroundColor = R.color.light_red;
                    textColor = R.color.red;
                    break;
                default:
                    backgroundColor = R.color.light_green;
                    textColor = R.color.must_green;
                    break;
            }
            
            categoryChip.setChipBackgroundColorResource(backgroundColor);
            categoryChip.setTextColor(itemView.getContext().getColor(textColor));
        }
    }
}
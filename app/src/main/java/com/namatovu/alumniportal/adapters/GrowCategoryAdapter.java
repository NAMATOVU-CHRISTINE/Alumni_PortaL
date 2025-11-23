package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.GrowCategory;

import java.util.ArrayList;
import java.util.List;

public class GrowCategoryAdapter extends RecyclerView.Adapter<GrowCategoryAdapter.ViewHolder> {

    private Context context;
    private List<GrowCategory> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(GrowCategory category);
    }

    public GrowCategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.listener = listener;
    }

    public void updateCategories(List<GrowCategory> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grow_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GrowCategory category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView iconText;
        TextView titleText;
        TextView descriptionText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewCategory);
            iconText = itemView.findViewById(R.id.tvCategoryIcon);
            titleText = itemView.findViewById(R.id.tvCategoryTitle);
            descriptionText = itemView.findViewById(R.id.tvCategoryDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        void bind(GrowCategory category) {
            iconText.setText(category.getIcon());
            titleText.setText(category.getTitle());
            descriptionText.setText(category.getDescription());

            // Set background color
            try {
                int color = Color.parseColor(category.getBackgroundColor());
                cardView.setCardBackgroundColor(color);
            } catch (Exception e) {
                // Fallback to default color
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.light_background));
            }
        }
    }
}
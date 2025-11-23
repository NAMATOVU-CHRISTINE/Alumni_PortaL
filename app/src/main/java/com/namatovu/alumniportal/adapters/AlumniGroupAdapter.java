package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.AlumniGroup;

import java.util.List;

public class AlumniGroupAdapter extends RecyclerView.Adapter<AlumniGroupAdapter.GroupViewHolder> {
    
    private Context context;
    private List<AlumniGroup> groups;
    private String currentUserId;
    private OnGroupClickListener listener;
    
    public interface OnGroupClickListener {
        void onGroupClick(AlumniGroup group);
    }
    
    public AlumniGroupAdapter(Context context, List<AlumniGroup> groups, String currentUserId) {
        this.context = context;
        this.groups = groups;
        this.currentUserId = currentUserId;
    }
    
    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.listener = listener;
    }
    
    public void updateGroups(List<AlumniGroup> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alumni_group, parent, false);
        return new GroupViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        AlumniGroup group = groups.get(position);
        holder.bind(group);
    }
    
    @Override
    public int getItemCount() {
        return groups.size();
    }
    
    class GroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewGroup;
        private TextView textViewGroupName;
        private TextView textViewDescription;
        private TextView textViewMemberCount;
        private TextView textViewGroupType;
        private ImageView imageViewJoined;
        
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewGroup = itemView.findViewById(R.id.imageViewGroup);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewMemberCount = itemView.findViewById(R.id.textViewMemberCount);
            textViewGroupType = itemView.findViewById(R.id.textViewGroupType);
            imageViewJoined = itemView.findViewById(R.id.imageViewJoined);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onGroupClick(groups.get(position));
                    }
                }
            });
        }
        
        public void bind(AlumniGroup group) {
            textViewGroupName.setText(group.getGroupName());
            textViewDescription.setText(group.getDescription());
            textViewMemberCount.setText(group.getMemberCount() + " members");
            textViewGroupType.setText(group.getGroupType().toUpperCase());
            
            // Show joined indicator if user is a member
            if (group.isMember(currentUserId)) {
                imageViewJoined.setVisibility(View.VISIBLE);
            } else {
                imageViewJoined.setVisibility(View.GONE);
            }
            
            // Load group image
            if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(group.getImageUrl())
                        .placeholder(R.drawable.ic_group)
                        .into(imageViewGroup);
            } else {
                imageViewGroup.setImageResource(R.drawable.ic_group);
            }
        }
    }
}

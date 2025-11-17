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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        holder.bin
package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.JobPosting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JobPostingAdapter extends RecyclerView.Adapter<JobPostingAdapter.JobViewHolder> {
    
    private List<JobPosting> jobPostings = new ArrayList<>();
    private OnJobClickListener listener;
    
    public interface OnJobClickListener {
        void onJobClick(JobPosting jobPosting);
        void onApplyClick(JobPosting jobPosting);
    }
    
    // Constructor
    public JobPostingAdapter(List<JobPosting> jobPostings, OnJobClickListener listener) {
        this.jobPostings = jobPostings != null ? jobPostings : new ArrayList<>();
        this.listener = listener;
    }
    
    public void setOnJobClickListener(OnJobClickListener listener) {
        this.listener = listener;
    }
    
    public void setJobPostings(List<JobPosting> jobPostings) {
        this.jobPostings = jobPostings != null ? jobPostings : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_posting, parent, false);
        return new JobViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobPosting job = jobPostings.get(position);
        holder.bind(job);
    }
    
    @Override
    public int getItemCount() {
        return jobPostings.size();
    }
    
    class JobViewHolder extends RecyclerView.ViewHolder {
        private TextView textCompany;
        private TextView textPosition;
        private TextView textLocation;
        private TextView textSalary;
        private TextView textDescription;
        private TextView textJobType;
        private TextView textPostedDate;
        private View buttonApply;
        
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            textCompany = itemView.findViewById(R.id.textCompany);
            textPosition = itemView.findViewById(R.id.textPosition);
            textLocation = itemView.findViewById(R.id.textLocation);
            textSalary = itemView.findViewById(R.id.textSalary);
            textDescription = itemView.findViewById(R.id.textDescription);
            textJobType = itemView.findViewById(R.id.textJobType);
            textPostedDate = itemView.findViewById(R.id.textPostedDate);
            buttonApply = itemView.findViewById(R.id.buttonApply);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onJobClick(jobPostings.get(getAdapterPosition()));
                }
            });

            if (buttonApply != null) {
                buttonApply.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onApplyClick(jobPostings.get(getAdapterPosition()));
                    }
                });
            }
        }
        
        public void bind(JobPosting job) {
            if (job == null) return;
            
            textCompany.setText(job.getCompany() != null ? job.getCompany() : "Unknown Company");
            textPosition.setText(job.getTitle() != null ? job.getTitle() : "Unknown Position");
            textLocation.setText(job.getLocation() != null ? job.getLocation() : "Location not specified");
            
            // Format salary range
            if (job.getSalaryRange() != null && !job.getSalaryRange().isEmpty()) {
                textSalary.setText(job.getSalaryRange());
                textSalary.setVisibility(View.VISIBLE);
            } else {
                textSalary.setVisibility(View.GONE);
            }
            
            textDescription.setText(job.getDescription() != null ? job.getDescription() : "No description available");
            textJobType.setText(job.getEmploymentType() != null ? job.getEmploymentType() : "Full Time");
            
            // Format posted date
            if (job.getPostedAt() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDate = "Posted " + sdf.format(new Date(job.getPostedAt()));
                textPostedDate.setText(formattedDate);
            } else {
                textPostedDate.setText("Recently posted");
            }
        }
    }
}
package com.namatovu.alumniportal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.namatovu.alumniportal.databinding.ItemJobBinding;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private final List<Job> jobList;

    public JobAdapter(List<Job> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemJobBinding binding = ItemJobBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new JobViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.bind(job);
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        private final ItemJobBinding binding;
        private final Context context;

        JobViewHolder(ItemJobBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();
        }

        void bind(Job job) {
            binding.jobTitleText.setText(job.getTitle());
            binding.companyNameText.setText(job.getCompany());
            binding.locationText.setText(job.getLocation());

            // Set click listener to open the detail view
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra(JobDetailActivity.EXTRA_JOB_ID, job.getId());
                context.startActivity(intent);
            });
        }
    }
}

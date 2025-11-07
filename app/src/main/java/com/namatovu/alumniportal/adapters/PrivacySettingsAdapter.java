package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.namatovu.alumniportal.R;

import java.util.ArrayList;
import java.util.List;

public class PrivacySettingsAdapter extends RecyclerView.Adapter<PrivacySettingsAdapter.SettingViewHolder> {
    
    private List<PrivacySetting> settings = new ArrayList<>();
    private OnSettingChangeListener listener;
    private OnPrivacySettingListener privacyListener;
    
    // Default constructor
    public PrivacySettingsAdapter() {
    }
    
    // Constructor with privacy listener
    public PrivacySettingsAdapter(List<?> items, OnPrivacySettingListener privacyListener) {
        this.privacyListener = privacyListener;
        // Note: items parameter is not used in this simple implementation
    }
    
    public interface OnSettingChangeListener {
        void onSettingChanged(PrivacySetting setting, boolean enabled);
    }
    
    public interface OnPrivacySettingListener {
        void onSwitchToggled(String settingKey, boolean isEnabled);
        void onButtonClicked(String actionKey);
    }
    
    public void setOnSettingChangeListener(OnSettingChangeListener listener) {
        this.listener = listener;
    }
    
    public void setSettings(List<PrivacySetting> settings) {
        this.settings = settings != null ? settings : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_privacy_setting, parent, false);
        return new SettingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        PrivacySetting setting = settings.get(position);
        holder.bind(setting);
    }
    
    @Override
    public int getItemCount() {
        return settings.size();
    }
    
    // Privacy Setting Data Class
    public static class PrivacySetting {
        private String title;
        private String description;
        private boolean enabled;
        private int iconResource;
        private String key;
        
        public PrivacySetting(String key, String title, String description, boolean enabled, int iconResource) {
            this.key = key;
            this.title = title;
            this.description = description;
            this.enabled = enabled;
            this.iconResource = iconResource;
        }
        
        // Getters and setters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getIconResource() { return iconResource; }
        public String getKey() { return key; }
    }
    
    class SettingViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageIcon;
        private TextView textTitle;
        private TextView textDescription;
        private MaterialSwitch switchSetting;
        
        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            switchSetting = itemView.findViewById(R.id.switchSetting);
            
            switchSetting.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    PrivacySetting setting = settings.get(position);
                    setting.setEnabled(isChecked);
                    listener.onSettingChanged(setting, isChecked);
                }
            });
        }
        
        public void bind(PrivacySetting setting) {
            if (setting == null) return;
            
            textTitle.setText(setting.getTitle());
            textDescription.setText(setting.getDescription());
            switchSetting.setChecked(setting.isEnabled());
            
            if (setting.getIconResource() != 0) {
                imageIcon.setImageResource(setting.getIconResource());
            } else {
                imageIcon.setImageResource(R.drawable.ic_settings);
            }
        }
    }
}
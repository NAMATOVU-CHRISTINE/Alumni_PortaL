package com.namatovu.alumniportal.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Job posting model for Alumni Portal
 */
public class JobPosting {
    private String jobId;
    private String title;
    private String company;
    private String description;
    private String requirements;
    private String location;
    private String salary;
    private String jobType; // "full-time", "part-time", "contract", "internship"
    private String experienceLevel; // "entry", "mid", "senior", "executive"
    private String postedBy; // User ID who posted
    private String postedByName; // Name of poster for display
    private long postedAt;
    private long expiresAt;
    private boolean isActive;
    private boolean isRemote;
    private String applicationUrl;
    private String contactEmail;
    private List<String> tags;
    private Map<String, Object> requirements_details;
    private int viewCount;
    private int applicationCount;

    // Default constructor required for Firebase
    public JobPosting() {
        this.isActive = true;
        this.viewCount = 0;
        this.applicationCount = 0;
        this.postedAt = System.currentTimeMillis();
        // Default expiry: 90 days from posting
        this.expiresAt = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000);
        this.requirements_details = new HashMap<>();
    }

    // Constructor for creating new job posting
    public JobPosting(String title, String company, String description, String postedBy, String postedByName) {
        this();
        this.title = title;
        this.company = company;
        this.description = description;
        this.postedBy = postedBy;
        this.postedByName = postedByName;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }

    public long getPostedAt() { return postedAt; }
    public void setPostedAt(long postedAt) { this.postedAt = postedAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isRemote() { return isRemote; }
    public void setRemote(boolean remote) { isRemote = remote; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Map<String, Object> getRequirements_details() { return requirements_details; }
    public void setRequirements_details(Map<String, Object> requirements_details) { this.requirements_details = requirements_details; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getApplicationCount() { return applicationCount; }
    public void setApplicationCount(int applicationCount) { this.applicationCount = applicationCount; }

    // Helper methods
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public boolean isValidForDisplay() {
        return isActive && !isExpired();
    }

    public String getFormattedSalary() {
        if (salary == null || salary.trim().isEmpty()) {
            return "Salary not specified";
        }
        return salary;
    }

    public String getFormattedLocation() {
        if (isRemote) {
            return location != null ? location + " (Remote)" : "Remote";
        }
        return location != null ? location : "Location not specified";
    }

    public String getTimeAgo() {
        long diff = System.currentTimeMillis() - postedAt;
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (days < 1) {
            long hours = diff / (60 * 60 * 1000);
            return hours < 1 ? "Just now" : hours + "h ago";
        } else if (days < 7) {
            return days + "d ago";
        } else if (days < 30) {
            return (days / 7) + "w ago";
        } else {
            return (days / 30) + "m ago";
        }
    }

    public String getDaysUntilExpiry() {
        long diff = expiresAt - System.currentTimeMillis();
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (days <= 0) {
            return "Expired";
        } else if (days == 1) {
            return "1 day left";
        } else {
            return days + " days left";
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementApplicationCount() {
        this.applicationCount++;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("title", title);
        jobMap.put("company", company);
        jobMap.put("description", description);
        jobMap.put("requirements", requirements);
        jobMap.put("location", location);
        jobMap.put("salary", salary);
        jobMap.put("jobType", jobType);
        jobMap.put("experienceLevel", experienceLevel);
        jobMap.put("postedBy", postedBy);
        jobMap.put("postedByName", postedByName);
        jobMap.put("postedAt", postedAt);
        jobMap.put("expiresAt", expiresAt);
        jobMap.put("isActive", isActive);
        jobMap.put("isRemote", isRemote);
        jobMap.put("applicationUrl", applicationUrl);
        jobMap.put("contactEmail", contactEmail);
        jobMap.put("tags", tags);
        jobMap.put("requirements_details", requirements_details);
        jobMap.put("viewCount", viewCount);
        jobMap.put("applicationCount", applicationCount);
        return jobMap;
    }

    // Alias methods for adapter compatibility
    public String getPosition() { 
        return getTitle(); 
    }
    
    public String getSalaryRange() { 
        return getSalary(); 
    }
    
    // Methods for SearchAndFilterManager compatibility
    public Integer getSalaryMin() {
        String salaryStr = getSalary();
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return null;
        }
        
        // Try to parse salary range like "50000-80000" or just "50000"
        try {
            if (salaryStr.contains("-")) {
                String[] parts = salaryStr.split("-");
                if (parts.length >= 1) {
                    return Integer.parseInt(parts[0].trim().replaceAll("[^0-9]", ""));
                }
            } else {
                // Single salary value
                return Integer.parseInt(salaryStr.replaceAll("[^0-9]", ""));
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return null
        }
        return null;
    }
    
    public Integer getSalaryMax() {
        String salaryStr = getSalary();
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return null;
        }
        
        // Try to parse salary range like "50000-80000" or just "50000"
        try {
            if (salaryStr.contains("-")) {
                String[] parts = salaryStr.split("-");
                if (parts.length >= 2) {
                    return Integer.parseInt(parts[1].trim().replaceAll("[^0-9]", ""));
                }
            } else {
                // Single salary value - use it as both min and max
                return Integer.parseInt(salaryStr.replaceAll("[^0-9]", ""));
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return null
        }
        return null;
    }
    
    public String getType() { 
        return getJobType(); 
    }
    
    public java.util.Date getPostedDate() { 
        return new java.util.Date(getPostedAt()); 
    }
}
package com.namatovu.alumniportal.models;

import java.util.Date;

/**
 * Opportunity model for jobs, internships, graduate training, and apprenticeships
 */
public class Opportunity {
    private String id;
    private String title;
    private String company;
    private String category; // Job, Internship, Graduate Training, Apprenticeship
    private String description;
    private String shortDescription;
    private Date applicationDeadline;
    private Date datePosted;
    private String applicationLink;
    private String applicationEmail;
    private String location;
    private String requirements;
    private String benefits;
    private String salaryRange;
    private boolean isFeatured;
    private boolean isActive;
    private String postedBy; // User ID who posted this opportunity
    private String companyLogo; // URL or resource identifier
    private int applicationsCount;
    private boolean isSaved;

    // Constructors
    public Opportunity() {
        this.datePosted = new Date();
        this.isActive = true;
        this.isFeatured = false;
        this.applicationsCount = 0;
        this.isSaved = false;
    }

    public Opportunity(String title, String company, String category, String description, Date applicationDeadline) {
        this();
        this.title = title;
        this.company = company;
        this.category = category;
        this.description = description;
        this.applicationDeadline = applicationDeadline;
        this.id = generateId();
        
        // Generate short description from full description
        if (description != null && description.length() > 100) {
            this.shortDescription = description.substring(0, 97) + "...";
        } else {
            this.shortDescription = description;
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        
        // Auto-generate short description
        if (description != null && description.length() > 100) {
            this.shortDescription = description.substring(0, 97) + "...";
        } else {
            this.shortDescription = description;
        }
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Date getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(Date applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public Date getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(Date datePosted) {
        this.datePosted = datePosted;
    }

    public String getApplicationLink() {
        return applicationLink;
    }

    public void setApplicationLink(String applicationLink) {
        this.applicationLink = applicationLink;
    }

    public String getApplicationEmail() {
        return applicationEmail;
    }

    public void setApplicationEmail(String applicationEmail) {
        this.applicationEmail = applicationEmail;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public int getApplicationsCount() {
        return applicationsCount;
    }

    public void setApplicationsCount(int applicationsCount) {
        this.applicationsCount = applicationsCount;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    // Utility methods
    public String getCategoryIcon() {
        switch (category) {
            case "Job": return "💼";
            case "Internship": return "🎓";
            case "Graduate Training": return "🚀";
            case "Apprenticeship": return "🔧";
            default: return "💼";
        }
    }

    public String getFormattedDeadline() {
        if (applicationDeadline == null) return "No deadline";
        
        long now = System.currentTimeMillis();
        long deadlineTime = applicationDeadline.getTime();
        long diff = deadlineTime - now;
        
        if (diff <= 0) {
            return "Deadline passed";
        } else if (diff < 86400000) { // Less than 1 day
            return "Apply today!";
        } else if (diff < 604800000) { // Less than 1 week
            int days = (int) (diff / 86400000);
            return "Apply in " + days + " days";
        } else {
            // Format as date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
            return "Apply by " + sdf.format(applicationDeadline);
        }
    }

    public String getFormattedDatePosted() {
        if (datePosted == null) return "";
        
        long now = System.currentTimeMillis();
        long postedTime = datePosted.getTime();
        long diff = now - postedTime;
        
        if (diff < 86400000) { // Less than 1 day
            return "Posted today";
        } else if (diff < 604800000) { // Less than 1 week
            int days = (int) (diff / 86400000);
            return "Posted " + days + " days ago";
        } else {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd");
            return "Posted " + sdf.format(datePosted);
        }
    }

    public boolean isDeadlineApproaching() {
        if (applicationDeadline == null) return false;
        
        long now = System.currentTimeMillis();
        long deadlineTime = applicationDeadline.getTime();
        long diff = deadlineTime - now;
        
        // Consider deadline approaching if less than 7 days
        return diff > 0 && diff < 604800000;
    }

    public void incrementApplicationCount() {
        this.applicationsCount++;
    }

    public void toggleSaved() {
        this.isSaved = !this.isSaved;
    }

    private String generateId() {
        return "opp_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
}
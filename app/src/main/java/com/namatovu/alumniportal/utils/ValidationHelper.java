package com.namatovu.alumniportal.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Comprehensive validation helper for form inputs and data validation
 */
public class ValidationHelper {
    private static final String TAG = "ValidationHelper";
    
    // Common patterns
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'.-]{2,50}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[0-9A-Z]{6,12}$");
    private static final Pattern LINKEDIN_PATTERN = Pattern.compile("^https?://(www\\.)?linkedin\\.com/in/[a-zA-Z0-9-]+/?$");
    private static final Pattern GITHUB_PATTERN = Pattern.compile("^https?://(www\\.)?github\\.com/[a-zA-Z0-9-]+/?$");
    private static final Pattern WEBSITE_PATTERN = Pattern.compile("^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$");
    
    // Date patterns
    private static final long MIN_GRADUATION_YEAR = 1950;
    private static final long MAX_GRADUATION_YEAR = 2030;
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        public boolean isValid;
        public String errorMessage;
        public List<String> errorMessages;
        
        public ValidationResult() {
            this.errorMessages = new ArrayList<>();
        }
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this();
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            if (!isValid && errorMessage != null) {
                this.errorMessages.add(errorMessage);
            }
        }
        
        public void addError(String error) {
            this.isValid = false;
            this.errorMessages.add(error);
            if (this.errorMessage == null) {
                this.errorMessage = error;
            }
        }
        
        public String getAllErrors() {
            return TextUtils.join("; ", errorMessages);
        }
    }
    
    /**
     * Email validation
     */
    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(false, "Email is required");
        }
        
        email = email.trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult(false, "Please enter a valid email address");
        }
        
        if (email.length() > 100) {
            return new ValidationResult(false, "Email address is too long");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Password validation with strength requirements
     */
    public static ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();
        
        if (TextUtils.isEmpty(password)) {
            result.addError("Password is required");
            return result;
        }
        
        if (password.length() < 8) {
            result.addError("Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            result.addError("Password is too long (max 128 characters)");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            result.addError("Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            result.addError("Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*[0-9].*")) {
            result.addError("Password must contain at least one number");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            result.addError("Password must contain at least one special character");
        }
        
        // Check for common weak passwords
        if (isCommonPassword(password)) {
            result.addError("Password is too common. Please choose a stronger password");
        }
        
        if (result.errorMessages.isEmpty()) {
            result.isValid = true;
        }
        
        return result;
    }
    
    /**
     * Full name validation
     */
    public static ValidationResult validateFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return new ValidationResult(false, "Full name is required");
        }
        
        fullName = fullName.trim();
        if (fullName.length() < 2) {
            return new ValidationResult(false, "Full name must be at least 2 characters long");
        }
        
        if (fullName.length() > 50) {
            return new ValidationResult(false, "Full name is too long (max 50 characters)");
        }
        
        if (!NAME_PATTERN.matcher(fullName).matches()) {
            return new ValidationResult(false, "Full name contains invalid characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Phone number validation
     */
    public static ValidationResult validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return new ValidationResult(true, null); // Phone is optional
        }
        
        phoneNumber = phoneNumber.trim().replaceAll("\\s+", "");
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            return new ValidationResult(false, "Please enter a valid phone number");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Student ID validation
     */
    public static ValidationResult validateStudentId(String studentId) {
        if (TextUtils.isEmpty(studentId)) {
            return new ValidationResult(false, "Student ID is required");
        }
        
        studentId = studentId.trim().toUpperCase();
        if (!STUDENT_ID_PATTERN.matcher(studentId).matches()) {
            return new ValidationResult(false, "Student ID must be 6-12 alphanumeric characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Graduation year validation
     */
    public static ValidationResult validateGraduationYear(int year) {
        if (year < MIN_GRADUATION_YEAR || year > MAX_GRADUATION_YEAR) {
            return new ValidationResult(false, 
                "Graduation year must be between " + MIN_GRADUATION_YEAR + " and " + MAX_GRADUATION_YEAR);
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Major validation
     */
    public static ValidationResult validateMajor(String major) {
        if (TextUtils.isEmpty(major)) {
            return new ValidationResult(false, "Major is required");
        }
        
        major = major.trim();
        if (major.length() < 2) {
            return new ValidationResult(false, "Major must be at least 2 characters long");
        }
        
        if (major.length() > 100) {
            return new ValidationResult(false, "Major name is too long (max 100 characters)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Bio validation
     */
    public static ValidationResult validateBio(String bio) {
        if (TextUtils.isEmpty(bio)) {
            return new ValidationResult(true, null); // Bio is optional
        }
        
        bio = bio.trim();
        if (bio.length() > 500) {
            return new ValidationResult(false, "Bio is too long (max 500 characters)");
        }
        
        if (containsSuspiciousContent(bio)) {
            return new ValidationResult(false, "Bio contains inappropriate content");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Skills validation
     */
    public static ValidationResult validateSkills(String skills) {
        if (TextUtils.isEmpty(skills)) {
            return new ValidationResult(true, null); // Skills are optional
        }
        
        skills = skills.trim();
        if (skills.length() > 300) {
            return new ValidationResult(false, "Skills list is too long (max 300 characters)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * LinkedIn URL validation
     */
    public static ValidationResult validateLinkedInUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return new ValidationResult(true, null); // LinkedIn is optional
        }
        
        url = url.trim();
        if (!LINKEDIN_PATTERN.matcher(url).matches()) {
            return new ValidationResult(false, "Please enter a valid LinkedIn profile URL");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * GitHub URL validation
     */
    public static ValidationResult validateGitHubUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return new ValidationResult(true, null); // GitHub is optional
        }
        
        url = url.trim();
        if (!GITHUB_PATTERN.matcher(url).matches()) {
            return new ValidationResult(false, "Please enter a valid GitHub profile URL");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Website URL validation
     */
    public static ValidationResult validateWebsiteUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return new ValidationResult(true, null); // Website is optional
        }
        
        url = url.trim();
        if (!WEBSITE_PATTERN.matcher(url).matches()) {
            return new ValidationResult(false, "Please enter a valid website URL");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Job title validation
     */
    public static ValidationResult validateJobTitle(String jobTitle) {
        if (TextUtils.isEmpty(jobTitle)) {
            return new ValidationResult(true, null); // Job title is optional
        }
        
        jobTitle = jobTitle.trim();
        if (jobTitle.length() > 100) {
            return new ValidationResult(false, "Job title is too long (max 100 characters)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Company name validation
     */
    public static ValidationResult validateCompanyName(String company) {
        if (TextUtils.isEmpty(company)) {
            return new ValidationResult(true, null); // Company is optional
        }
        
        company = company.trim();
        if (company.length() > 100) {
            return new ValidationResult(false, "Company name is too long (max 100 characters)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Location validation
     */
    public static ValidationResult validateLocation(String location) {
        if (TextUtils.isEmpty(location)) {
            return new ValidationResult(true, null); // Location is optional
        }
        
        location = location.trim();
        if (location.length() > 100) {
            return new ValidationResult(false, "Location is too long (max 100 characters)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Message content validation
     */
    public static ValidationResult validateMessageContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return new ValidationResult(false, "Message cannot be empty");
        }
        
        content = content.trim();
        if (content.length() > 2000) {
            return new ValidationResult(false, "Message is too long (max 2000 characters)");
        }
        
        if (containsSuspiciousContent(content)) {
            return new ValidationResult(false, "Message contains inappropriate content");
        }
        
        if (isSpamMessage(content)) {
            return new ValidationResult(false, "Message appears to be spam");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Event title validation
     */
    public static ValidationResult validateEventTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return new ValidationResult(false, "Event title is required");
        }
        
        title = title.trim();
        if (title.length() < 3) {
            return new ValidationResult(false, "Event title must be at least 3 characters long");
        }
        
        if (title.length() > 100) {
            return new ValidationResult(false, "Event title is too long (max 100 characters)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Event description validation
     */
    public static ValidationResult validateEventDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            return new ValidationResult(false, "Event description is required");
        }
        
        description = description.trim();
        if (description.length() < 10) {
            return new ValidationResult(false, "Event description must be at least 10 characters long");
        }
        
        if (description.length() > 1000) {
            return new ValidationResult(false, "Event description is too long (max 1000 characters)");
        }
        
        if (containsSuspiciousContent(description)) {
            return new ValidationResult(false, "Event description contains inappropriate content");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Job posting validation
     */
    public static ValidationResult validateJobPosting(String company, String position, String description) {
        ValidationResult result = new ValidationResult();
        
        if (TextUtils.isEmpty(company)) {
            result.addError("Company name is required");
        } else if (company.trim().length() > 100) {
            result.addError("Company name is too long (max 100 characters)");
        }
        
        if (TextUtils.isEmpty(position)) {
            result.addError("Position title is required");
        } else if (position.trim().length() > 100) {
            result.addError("Position title is too long (max 100 characters)");
        }
        
        if (TextUtils.isEmpty(description)) {
            result.addError("Job description is required");
        } else {
            description = description.trim();
            if (description.length() < 50) {
                result.addError("Job description must be at least 50 characters long");
            } else if (description.length() > 2000) {
                result.addError("Job description is too long (max 2000 characters)");
            } else if (containsSuspiciousContent(description)) {
                result.addError("Job description contains inappropriate content");
            }
        }
        
        if (result.errorMessages.isEmpty()) {
            result.isValid = true;
        }
        
        return result;
    }
    
    /**
     * File validation
     */
    public static ValidationResult validateFile(String fileName, long fileSize, String mimeType) {
        ValidationResult result = new ValidationResult();
        
        if (TextUtils.isEmpty(fileName)) {
            result.addError("File name is required");
            return result;
        }
        
        // Check file size (max 10MB)
        if (fileSize > 10 * 1024 * 1024) {
            result.addError("File size is too large (max 10MB)");
        }
        
        // Check file extension
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isAllowedFileExtension(extension)) {
            result.addError("File type not allowed");
        }
        
        // Check MIME type if provided
        if (!TextUtils.isEmpty(mimeType) && !isAllowedMimeType(mimeType)) {
            result.addError("File type not supported");
        }
        
        if (result.errorMessages.isEmpty()) {
            result.isValid = true;
        }
        
        return result;
    }
    
    // Helper methods
    private static boolean isCommonPassword(String password) {
        String[] commonPasswords = {
            "password", "12345678", "qwerty123", "admin123", "password123",
            "letmein", "welcome", "monkey", "1234567890", "iloveyou"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean containsSuspiciousContent(String text) {
        String[] suspiciousPatterns = {
            "<script", "javascript:", "onload=", "onerror=", "data:text/html",
            "vbscript:", "expression(", "eval(", "document.cookie"
        };
        
        String lowerText = text.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isSpamMessage(String message) {
        // Simple spam detection
        String lowerMessage = message.toLowerCase();
        
        // Check for excessive repetition
        if (hasExcessiveRepetition(message)) {
            return true;
        }
        
        // Check for spam keywords
        String[] spamKeywords = {
            "click here", "free money", "urgent", "congratulations you won",
            "act now", "limited time", "call now", "cash prize"
        };
        
        for (String keyword : spamKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean hasExcessiveRepetition(String text) {
        if (text.length() < 10) return false;
        
        // Check for repeated characters
        char prevChar = text.charAt(0);
        int consecutiveCount = 1;
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) == prevChar) {
                consecutiveCount++;
                if (consecutiveCount > 5) {
                    return true;
                }
            } else {
                consecutiveCount = 1;
                prevChar = text.charAt(i);
            }
        }
        
        return false;
    }
    
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
    
    private static boolean isAllowedFileExtension(String extension) {
        String[] allowedExtensions = {
            "jpg", "jpeg", "png", "gif", "webp", "bmp",
            "pdf", "doc", "docx", "txt", "rtf",
            "xls", "xlsx", "csv",
            "ppt", "pptx",
            "zip", "rar", "7z"
        };
        
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isAllowedMimeType(String mimeType) {
        String[] allowedMimeTypes = {
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "application/pdf", "text/plain", "application/rtf",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv", "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/zip", "application/x-rar-compressed", "application/x-7z-compressed"
        };
        
        for (String allowed : allowedMimeTypes) {
            if (allowed.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }
}
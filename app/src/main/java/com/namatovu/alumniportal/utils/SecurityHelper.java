package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.regex.Pattern;

/**
 * Security utilities for the Alumni Portal app
 * Handles secure storage, input validation, and data protection
 */
public class SecurityHelper {
    private static final String TAG = "SecurityHelper";
    private static final String ENCRYPTED_PREFS_FILE = "secure_prefs";
    
    // Pattern for email validation - more strict than just checking @ symbol
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Pattern for username validation - alphanumeric and underscores only
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    // Pattern for student ID validation - adjust based on your institution's format
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[0-9]{6,10}$");
    
    // Password strength requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    private static SharedPreferences encryptedPrefs;
    
    /**
     * Initialize encrypted shared preferences
     */
    public static void initialize(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            
            Log.d(TAG, "Encrypted SharedPreferences initialized");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to initialize encrypted preferences", e);
            // Fallback to regular SharedPreferences with warning
            encryptedPrefs = context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE);
        }
    }
    
    /**
     * Store sensitive data securely
     */
    public static void storeSecureString(String key, String value) {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().putString(key, value).apply();
        }
    }
    
    /**
     * Retrieve sensitive data securely
     */
    public static String getSecureString(String key, String defaultValue) {
        if (encryptedPrefs != null) {
            return encryptedPrefs.getString(key, defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * Clear all sensitive data
     */
    public static void clearSecureData() {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().clear().apply();
        }
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate username format and security
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        return USERNAME_PATTERN.matcher(trimmed).matches() && 
               !isCommonUsername(trimmed);
    }
    
    /**
     * Check for common/weak usernames
     */
    private static boolean isCommonUsername(String username) {
        String[] commonUsernames = {
            "admin", "user", "test", "guest", "anonymous", 
            "username", "password", "login", "root", "administrator"
        };
        
        String lower = username.toLowerCase();
        for (String common : commonUsernames) {
            if (lower.equals(common)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate student ID format
     */
    public static boolean isValidStudentId(String studentId) {
        return studentId != null && STUDENT_ID_PATTERN.matcher(studentId.trim()).matches();
    }
    
    /**
     * Validate password strength
     */
    public static PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            result.isValid = false;
            result.message = "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long";
            return result;
        }
        
        if (!PASSWORD_UPPERCASE.matcher(password).matches()) {
            result.isValid = false;
            result.message = "Password must contain at least one uppercase letter";
            return result;
        }
        
        if (!PASSWORD_LOWERCASE.matcher(password).matches()) {
            result.isValid = false;
            result.message = "Password must contain at least one lowercase letter";
            return result;
        }
        
        if (!PASSWORD_DIGIT.matcher(password).matches()) {
            result.isValid = false;
            result.message = "Password must contain at least one number";
            return result;
        }
        
        if (!PASSWORD_SPECIAL.matcher(password).matches()) {
            result.isValid = false;
            result.message = "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;':\",./<>?)";
            return result;
        }
        
        if (isCommonPassword(password)) {
            result.isValid = false;
            result.message = "Password is too common. Please choose a more unique password";
            return result;
        }
        
        result.isValid = true;
        result.message = "Password meets security requirements";
        return result;
    }
    
    /**
     * Check for common/weak passwords
     */
    private static boolean isCommonPassword(String password) {
        String[] commonPasswords = {
            "password", "12345678", "qwerty123", "admin123", "password123",
            "letmein", "welcome", "monkey", "1234567890", "iloveyou"
        };
        
        String lower = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lower.contains(common)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sanitize input to prevent injection attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        
        // Remove potential HTML/script tags
        String sanitized = input.replaceAll("<[^>]*>", "");
        
        // Remove potential SQL injection characters
        sanitized = sanitized.replaceAll("[';\"\\\\]", "");
        
        // Trim and limit length
        sanitized = sanitized.trim();
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
        }
        
        return sanitized;
    }
    
    /**
     * Validate and sanitize profile data
     */
    public static boolean isValidProfileData(String fullName, String bio, String skills) {
        if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 50) {
            return false;
        }
        
        if (bio != null && bio.length() > 500) {
            return false;
        }
        
        if (skills != null && skills.length() > 200) {
            return false;
        }
        
        // Check for suspicious patterns
        String[] suspiciousPatterns = {"<script", "javascript:", "onload=", "onerror="};
        String combined = (fullName + " " + (bio != null ? bio : "") + " " + (skills != null ? skills : "")).toLowerCase();
        
        for (String pattern : suspiciousPatterns) {
            if (combined.contains(pattern)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate chat message content
     */
    public static boolean isValidMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Check length limits
        if (content.length() > 2000) {
            return false;
        }
        
        // Check for suspicious patterns
        String[] suspiciousPatterns = {
            "<script", "javascript:", "onload=", "onerror=", 
            "data:text/html", "vbscript:", "expression("
        };
        
        String lowerContent = content.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerContent.contains(pattern)) {
                return false;
            }
        }
        
        // Check for excessive repetition (spam detection)
        if (isExcessiveRepetition(content)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate chat data structure
     */
    public static boolean isValidChatData(Object chatData, String currentUserId) {
        if (chatData == null || currentUserId == null) {
            return false;
        }
        
        // This would be expanded based on your Chat model structure
        // For now, basic validation
        return true;
    }
    
    /**
     * Validate message data structure
     */
    public static boolean isValidMessageData(Object messageData) {
        if (messageData == null) {
            return false;
        }
        
        // This would be expanded based on your ChatMessage model structure
        // For now, basic validation
        return true;
    }
    
    /**
     * Check for excessive repetition in text (spam detection)
     */
    private static boolean isExcessiveRepetition(String text) {
        if (text.length() < 10) return false;
        
        // Check for repeated characters
        char prevChar = text.charAt(0);
        int consecutiveCount = 1;
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) == prevChar) {
                consecutiveCount++;
                if (consecutiveCount > 10) { // More than 10 consecutive same characters
                    return true;
                }
            } else {
                consecutiveCount = 1;
                prevChar = text.charAt(i);
            }
        }
        
        // Check for repeated words
        String[] words = text.toLowerCase().split("\\s+");
        if (words.length > 3) {
            int repeatedWords = 0;
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].equals(words[i + 1]) && words[i].length() > 2) {
                    repeatedWords++;
                    if (repeatedWords > 3) { // More than 3 repeated words
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Validate file upload
     */
    public static boolean isValidFileUpload(String fileName, long fileSize, String mimeType) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // Check file size (limit to 10MB)
        if (fileSize > 10 * 1024 * 1024) {
            return false;
        }
        
        // Check for dangerous file extensions
        String[] dangerousExtensions = {
            ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", 
            ".jar", ".app", ".deb", ".pkg", ".dmg", ".msi"
        };
        
        String lowerFileName = fileName.toLowerCase();
        for (String ext : dangerousExtensions) {
            if (lowerFileName.endsWith(ext)) {
                return false;
            }
        }
        
        // Validate MIME type if provided
        if (mimeType != null) {
            String[] allowedMimeTypes = {
                "image/jpeg", "image/png", "image/gif", "image/webp",
                "application/pdf", "text/plain", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
            
            boolean isAllowed = false;
            for (String allowed : allowedMimeTypes) {
                if (mimeType.equals(allowed)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Password validation result class
     */
    public static class PasswordValidationResult {
        public boolean isValid;
        public String message;
    }
}
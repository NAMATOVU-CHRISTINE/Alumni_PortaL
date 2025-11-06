package com.namatovu.alumniportal.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;
import com.namatovu.alumniportal.utils.ValidationHelper.ValidationResult;

/**
 * Real-time form validation UI helper
 */
public class FormValidationHelper {
    
    /**
     * Real-time email validation
     */
    public static void setupEmailValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                ValidationResult result = ValidationHelper.validateEmail(email);
                
                if (!result.isValid && !email.isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        });
    }
    
    /**
     * Real-time password validation with strength indicator
     */
    public static void setupPasswordValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                ValidationResult result = ValidationHelper.validatePassword(password);
                
                if (!result.isValid && !password.isEmpty()) {
                    textInputLayout.setError(result.getAllErrors());
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                    
                    // Show password strength
                    if (!password.isEmpty()) {
                        String strength = getPasswordStrength(password);
                        textInputLayout.setHelperText("Password strength: " + strength);
                    } else {
                        textInputLayout.setHelperText(null);
                    }
                }
            }
        });
    }
    
    /**
     * Real-time full name validation
     */
    public static void setupFullNameValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String fullName = s.toString().trim();
                ValidationResult result = ValidationHelper.validateFullName(fullName);
                
                if (!result.isValid && !fullName.isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        });
    }
    
    /**
     * Real-time phone number validation
     */
    public static void setupPhoneValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();
                ValidationResult result = ValidationHelper.validatePhoneNumber(phone);
                
                if (!result.isValid && !phone.isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        });
    }
    
    /**
     * Real-time student ID validation
     */
    public static void setupStudentIdValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String studentId = s.toString().trim();
                ValidationResult result = ValidationHelper.validateStudentId(studentId);
                
                if (!result.isValid && !studentId.isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        });
    }
    
    /**
     * Real-time URL validation (LinkedIn, GitHub, Website)
     */
    public static void setupUrlValidation(TextInputLayout textInputLayout, EditText editText, UrlType urlType) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                ValidationResult result;
                
                switch (urlType) {
                    case LINKEDIN:
                        result = ValidationHelper.validateLinkedInUrl(url);
                        break;
                    case GITHUB:
                        result = ValidationHelper.validateGitHubUrl(url);
                        break;
                    case WEBSITE:
                        result = ValidationHelper.validateWebsiteUrl(url);
                        break;
                    default:
                        result = new ValidationResult(true, null);
                        break;
                }
                
                if (!result.isValid && !url.isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        });
    }
    
    /**
     * Real-time bio validation with character count
     */
    public static void setupBioValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String bio = s.toString();
                ValidationResult result = ValidationHelper.validateBio(bio);
                
                if (!result.isValid) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
                
                // Show character count
                int count = bio.length();
                int maxCount = 500;
                textInputLayout.setHelperText(count + "/" + maxCount + " characters");
            }
        });
    }
    
    /**
     * Real-time message validation with character count
     */
    public static void setupMessageValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String message = s.toString();
                ValidationResult result = ValidationHelper.validateMessageContent(message);
                
                if (!result.isValid && !message.trim().isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
                
                // Show character count
                int count = message.length();
                int maxCount = 2000;
                textInputLayout.setHelperText(count + "/" + maxCount + " characters");
            }
        });
    }
    
    /**
     * Real-time event title validation
     */
    public static void setupEventTitleValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String title = s.toString().trim();
                ValidationResult result = ValidationHelper.validateEventTitle(title);
                
                if (!result.isValid && !title.isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        });
    }
    
    /**
     * Real-time event description validation
     */
    public static void setupEventDescriptionValidation(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String description = s.toString();
                ValidationResult result = ValidationHelper.validateEventDescription(description);
                
                if (!result.isValid && !description.trim().isEmpty()) {
                    textInputLayout.setError(result.errorMessage);
                    textInputLayout.setErrorEnabled(true);
                } else {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
                
                // Show character count
                int count = description.length();
                int maxCount = 1000;
                textInputLayout.setHelperText(count + "/" + maxCount + " characters");
            }
        });
    }
    
    /**
     * Validate entire form and return overall result
     */
    public static ValidationResult validateForm(ValidationField... fields) {
        ValidationResult overallResult = new ValidationResult();
        overallResult.isValid = true;
        
        for (ValidationField field : fields) {
            ValidationResult fieldResult = field.validate();
            if (!fieldResult.isValid) {
                overallResult.isValid = false;
                overallResult.errorMessages.addAll(fieldResult.errorMessages);
                if (overallResult.errorMessage == null) {
                    overallResult.errorMessage = fieldResult.errorMessage;
                }
            }
        }
        
        return overallResult;
    }
    
    // Helper method to determine password strength
    private static String getPasswordStrength(String password) {
        int score = 0;
        
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;
        if (password.length() >= 12) score++;
        
        switch (score) {
            case 0:
            case 1:
            case 2:
                return "Weak";
            case 3:
            case 4:
                return "Medium";
            case 5:
            case 6:
                return "Strong";
            default:
                return "Very Strong";
        }
    }
    
    // Enum for URL types
    public enum UrlType {
        LINKEDIN, GITHUB, WEBSITE
    }
    
    // Interface for validation fields
    public interface ValidationField {
        ValidationResult validate();
    }
    
    // Predefined validation fields
    public static class EmailField implements ValidationField {
        private final String email;
        
        public EmailField(String email) {
            this.email = email;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationHelper.validateEmail(email);
        }
    }
    
    public static class PasswordField implements ValidationField {
        private final String password;
        
        public PasswordField(String password) {
            this.password = password;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationHelper.validatePassword(password);
        }
    }
    
    public static class FullNameField implements ValidationField {
        private final String fullName;
        
        public FullNameField(String fullName) {
            this.fullName = fullName;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationHelper.validateFullName(fullName);
        }
    }
    
    public static class StudentIdField implements ValidationField {
        private final String studentId;
        
        public StudentIdField(String studentId) {
            this.studentId = studentId;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationHelper.validateStudentId(studentId);
        }
    }
    
    public static class JobPostingField implements ValidationField {
        private final String company;
        private final String position;
        private final String description;
        
        public JobPostingField(String company, String position, String description) {
            this.company = company;
            this.position = position;
            this.description = description;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationHelper.validateJobPosting(company, position, description);
        }
    }
}
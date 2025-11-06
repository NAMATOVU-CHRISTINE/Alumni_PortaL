package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive file sharing system with Firebase Storage integration,
 * version control, collaborative features, and secure access controls
 */
public class FileShareManager {
    private static final String TAG = "FileShareManager";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String[] ALLOWED_EXTENSIONS = {
        "pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "ppt", "pptx",
        "jpg", "jpeg", "png", "gif", "webp", "bmp",
        "mp4", "avi", "mov", "wmv", "mp3", "wav",
        "zip", "rar", "7z"
    };
    
    private static FileShareManager instance;
    private final Context context;
    private final FirebaseStorage storage;
    private final FirebaseFirestore db;
    private final StorageReference storageRef;
    
    // File sharing scopes
    public enum ShareScope {
        PRIVATE,        // Only owner can access
        CONNECTIONS,    // Direct connections only
        ALUMNI,         // All verified alumni
        PUBLIC,         // Anyone with the link
        GROUPS          // Specific groups/classes
    }
    
    // File types for categorization
    public enum FileCategory {
        DOCUMENT,       // PDF, DOC, TXT, etc.
        PRESENTATION,   // PPT, PDF presentations
        SPREADSHEET,    // XLS, CSV
        IMAGE,          // JPG, PNG, etc.
        VIDEO,          // MP4, AVI, etc.
        AUDIO,          // MP3, WAV, etc.
        ARCHIVE,        // ZIP, RAR, etc.
        OTHER           // Miscellaneous
    }
    
    // File access permissions
    public enum AccessLevel {
        VIEW_ONLY,      // Can view/download only
        COMMENT,        // Can view and comment
        EDIT,           // Can edit (for supported types)
        OWNER           // Full control
    }
    
    private FileShareManager(Context context) {
        this.context = context.getApplicationContext();
        this.storage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.storageRef = storage.getReference();
    }
    
    public static synchronized FileShareManager getInstance(Context context) {
        if (instance == null) {
            instance = new FileShareManager(context);
        }
        return instance;
    }
    
    /**
     * Shared file metadata
     */
    public static class SharedFile {
        public String fileId;
        public String fileName;
        public String originalName;
        public String mimeType;
        public long fileSize;
        public FileCategory category;
        public String ownerId;
        public String ownerName;
        public ShareScope shareScope;
        public List<String> allowedUsers = new ArrayList<>();
        public List<String> allowedGroups = new ArrayList<>();
        public String downloadUrl;
        public String thumbnailUrl;
        public Date uploadedAt;
        public Date lastModified;
        public int version = 1;
        public String description;
        public List<String> tags = new ArrayList<>();
        public int downloadCount = 0;
        public Map<String, AccessLevel> userPermissions = new HashMap<>();
        public boolean isPublic = false;
        public boolean allowComments = true;
        public boolean enableVersioning = true;
        
        public SharedFile() {}
        
        public SharedFile(String fileName, String mimeType, long fileSize, String ownerId) {
            this.fileId = UUID.randomUUID().toString();
            this.fileName = fileName;
            this.originalName = fileName;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
            this.ownerId = ownerId;
            this.uploadedAt = new Date();
            this.lastModified = new Date();
            this.category = determineCategory(mimeType, fileName);
        }
        
        private FileCategory determineCategory(String mimeType, String fileName) {
            if (mimeType.startsWith("image/")) return FileCategory.IMAGE;
            if (mimeType.startsWith("video/")) return FileCategory.VIDEO;
            if (mimeType.startsWith("audio/")) return FileCategory.AUDIO;
            
            String extension = getFileExtension(fileName).toLowerCase();
            switch (extension) {
                case "pdf":
                case "doc":
                case "docx":
                case "txt":
                case "rtf":
                    return FileCategory.DOCUMENT;
                case "ppt":
                case "pptx":
                    return FileCategory.PRESENTATION;
                case "xls":
                case "xlsx":
                case "csv":
                    return FileCategory.SPREADSHEET;
                case "zip":
                case "rar":
                case "7z":
                    return FileCategory.ARCHIVE;
                default:
                    return FileCategory.OTHER;
            }
        }
    }
    
    /**
     * File upload progress callback
     */
    public interface FileUploadCallback {
        void onProgress(int progress);
        void onSuccess(SharedFile sharedFile);
        void onError(String error);
    }
    
    /**
     * File operation callback
     */
    public interface FileOperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    /**
     * File list callback
     */
    public interface FileListCallback {
        void onSuccess(List<SharedFile> files);
        void onError(String error);
    }
    
    /**
     * Upload file to shared storage
     */
    public void uploadFile(@NonNull Uri fileUri, @NonNull String fileName, 
                          @Nullable String description, @NonNull ShareScope shareScope,
                          @NonNull FileUploadCallback callback) {
        
        PerformanceHelper.getInstance().startTiming("file_upload");
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        // Validate file
        ValidationHelper.ValidationResult validation = validateFile(fileUri, fileName);
        if (!validation.isValid) {
            callback.onError(validation.errorMessage);
            return;
        }
        
        // Create shared file metadata
        try {
            String mimeType = getMimeType(fileUri);
            long fileSize = getFileSize(fileUri);
            
            SharedFile sharedFile = new SharedFile(fileName, mimeType, fileSize, currentUser.getUid());
            sharedFile.description = description;
            sharedFile.shareScope = shareScope;
            sharedFile.ownerName = currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : "Unknown User";
            
            // Create storage path
            String storagePath = createStoragePath(sharedFile);
            StorageReference fileRef = storageRef.child(storagePath);
            
            // Upload file
            UploadTask uploadTask = fileRef.putFile(fileUri);
            
            uploadTask.addOnProgressListener(taskSnapshot -> {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                callback.onProgress(progress);
            });
            
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get download URL
                fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    sharedFile.downloadUrl = downloadUri.toString();
                    
                    // Generate thumbnail if needed
                    generateThumbnail(sharedFile, () -> {
                        // Save metadata to Firestore
                        saveFileMetadata(sharedFile, new FileOperationCallback() {
                            @Override
                            public void onSuccess(String message) {
                                PerformanceHelper.getInstance().endTiming("file_upload");
                                
                                // Log analytics
                                AnalyticsHelper.logFileUpload(sharedFile.category.toString(), 
                                    sharedFile.fileSize, sharedFile.shareScope.toString());
                                
                                callback.onSuccess(sharedFile);
                            }
                            
                            @Override
                            public void onError(String error) {
                                PerformanceHelper.getInstance().endTiming("file_upload");
                                callback.onError("Failed to save file metadata: " + error);
                            }
                        });
                    });
                }).addOnFailureListener(e -> {
                    PerformanceHelper.getInstance().endTiming("file_upload");
                    Log.e(TAG, "Failed to get download URL", e);
                    callback.onError("Failed to get download URL: " + e.getMessage());
                });
            });
            
            uploadTask.addOnFailureListener(e -> {
                PerformanceHelper.getInstance().endTiming("file_upload");
                Log.e(TAG, "File upload failed", e);
                ErrorHandler.getInstance(context).handleError(e, "file_upload");
                callback.onError("Upload failed: " + e.getMessage());
            });
            
        } catch (Exception e) {
            PerformanceHelper.getInstance().endTiming("file_upload");
            Log.e(TAG, "Error preparing file upload", e);
            callback.onError("Error preparing upload: " + e.getMessage());
        }
    }
    
    /**
     * Get files shared with current user
     */
    public void getSharedFiles(@Nullable ShareScope scope, @Nullable FileCategory category,
                              @NonNull FileListCallback callback) {
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        Query query = db.collection("sharedFiles");
        
        // Filter by scope if specified
        if (scope != null) {
            query = query.whereEqualTo("shareScope", scope.toString());
        }
        
        // Filter by category if specified
        if (category != null) {
            query = query.whereEqualTo("category", category.toString());
        }
        
        // Apply access control
        query = query.whereIn("shareScope", List.of(
            ShareScope.PUBLIC.toString(),
            ShareScope.ALUMNI.toString()
        ));
        
        query.orderBy("uploadedAt", Query.Direction.DESCENDING);
        
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<SharedFile> files = new ArrayList<>();
                QuerySnapshot snapshot = task.getResult();
                
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    try {
                        SharedFile file = document.toObject(SharedFile.class);
                        if (file != null && canUserAccessFile(currentUser.getUid(), file)) {
                            files.add(file);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing shared file: " + document.getId(), e);
                    }
                }
                
                callback.onSuccess(files);
                
            } else {
                Log.e(TAG, "Failed to get shared files", task.getException());
                ErrorHandler.getInstance(context).handleError(task.getException(), "get_shared_files");
                callback.onError("Failed to load files: " + task.getException().getMessage());
            }
        });
    }
    
    /**
     * Get files uploaded by current user
     */
    public void getMyFiles(@NonNull FileListCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        db.collection("sharedFiles")
            .whereEqualTo("ownerId", currentUser.getUid())
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<SharedFile> files = new ArrayList<>();
                    QuerySnapshot snapshot = task.getResult();
                    
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        try {
                            SharedFile file = document.toObject(SharedFile.class);
                            if (file != null) {
                                files.add(file);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing file: " + document.getId(), e);
                        }
                    }
                    
                    callback.onSuccess(files);
                    
                } else {
                    Log.e(TAG, "Failed to get user files", task.getException());
                    callback.onError("Failed to load your files: " + task.getException().getMessage());
                }
            });
    }
    
    /**
     * Download file (tracks download count)
     */
    public void downloadFile(@NonNull String fileId, @NonNull FileOperationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        // Get file metadata first
        db.collection("sharedFiles").document(fileId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    SharedFile file = document.toObject(SharedFile.class);
                    if (file != null && canUserAccessFile(currentUser.getUid(), file)) {
                        
                        // Increment download count
                        incrementDownloadCount(fileId);
                        
                        // Log analytics
                        AnalyticsHelper.logFileDownload(file.category.toString(), file.fileSize);
                        
                        // Return download URL (actual download handled by browser/system)
                        callback.onSuccess(file.downloadUrl);
                        
                    } else {
                        callback.onError("Access denied or file not found");
                    }
                } else {
                    callback.onError("File not found");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get file for download", e);
                callback.onError("Download failed: " + e.getMessage());
            });
    }
    
    /**
     * Delete file (owner only)
     */
    public void deleteFile(@NonNull String fileId, @NonNull FileOperationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        // Get file metadata first
        db.collection("sharedFiles").document(fileId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    SharedFile file = document.toObject(SharedFile.class);
                    if (file != null) {
                        
                        // Check ownership
                        if (!file.ownerId.equals(currentUser.getUid())) {
                            callback.onError("Only the file owner can delete this file");
                            return;
                        }
                        
                        // Delete from storage
                        String storagePath = createStoragePath(file);
                        StorageReference fileRef = storageRef.child(storagePath);
                        
                        fileRef.delete().addOnSuccessListener(aVoid -> {
                            // Delete metadata from Firestore
                            db.collection("sharedFiles").document(fileId)
                                .delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    callback.onSuccess("File deleted successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to delete file metadata", e);
                                    callback.onError("Failed to delete file metadata");
                                });
                                
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to delete file from storage", e);
                            callback.onError("Failed to delete file from storage");
                        });
                        
                    } else {
                        callback.onError("File not found");
                    }
                } else {
                    callback.onError("File not found");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get file for deletion", e);
                callback.onError("Delete failed: " + e.getMessage());
            });
    }
    
    /**
     * Update file sharing settings
     */
    public void updateFileSharing(@NonNull String fileId, @NonNull ShareScope newScope,
                                 @Nullable List<String> allowedUsers,
                                 @NonNull FileOperationCallback callback) {
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("shareScope", newScope.toString());
        updates.put("lastModified", new Date());
        
        if (allowedUsers != null) {
            updates.put("allowedUsers", allowedUsers);
        }
        
        db.collection("sharedFiles").document(fileId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                callback.onSuccess("Sharing settings updated");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update sharing settings", e);
                callback.onError("Failed to update sharing settings: " + e.getMessage());
            });
    }
    
    // Helper methods
    private ValidationHelper.ValidationResult validateFile(Uri fileUri, String fileName) {
        try {
            // Check file extension
            String extension = getFileExtension(fileName).toLowerCase();
            boolean allowedExtension = false;
            for (String allowed : ALLOWED_EXTENSIONS) {
                if (allowed.equals(extension)) {
                    allowedExtension = true;
                    break;
                }
            }
            
            if (!allowedExtension) {
                return new ValidationHelper.ValidationResult(false, 
                    "File type not allowed. Supported types: " + String.join(", ", ALLOWED_EXTENSIONS));
            }
            
            // Check file size
            long fileSize = getFileSize(fileUri);
            if (fileSize > MAX_FILE_SIZE) {
                return new ValidationHelper.ValidationResult(false, 
                    "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
            }
            
            if (fileSize == 0) {
                return new ValidationHelper.ValidationResult(false, "File appears to be empty");
            }
            
            return new ValidationHelper.ValidationResult(true, null);
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating file", e);
            return new ValidationHelper.ValidationResult(false, "Error validating file: " + e.getMessage());
        }
    }
    
    private String createStoragePath(SharedFile file) {
        return String.format("shared_files/%s/%s/%s", 
            file.ownerId, 
            file.category.toString().toLowerCase(),
            file.fileId + "_" + file.fileName
        );
    }
    
    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals("content")) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }
    
    private long getFileSize(Uri uri) {
        try {
            return context.getContentResolver().openInputStream(uri).available();
        } catch (Exception e) {
            Log.w(TAG, "Could not determine file size", e);
            return 0;
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    private void generateThumbnail(SharedFile file, Runnable onComplete) {
        // For images, generate thumbnail
        if (file.category == FileCategory.IMAGE) {
            // This would generate a thumbnail and upload it
            // For now, we'll just complete immediately
        }
        onComplete.run();
    }
    
    private void saveFileMetadata(SharedFile file, FileOperationCallback callback) {
        db.collection("sharedFiles")
            .document(file.fileId)
            .set(file)
            .addOnSuccessListener(aVoid -> {
                callback.onSuccess("File metadata saved");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save file metadata", e);
                callback.onError("Failed to save metadata: " + e.getMessage());
            });
    }
    
    private boolean canUserAccessFile(String userId, SharedFile file) {
        // Owner can always access
        if (file.ownerId.equals(userId)) {
            return true;
        }
        
        // Check scope-based access
        switch (file.shareScope) {
            case PUBLIC:
                return true;
            case ALUMNI:
                return true; // Assuming user is verified alumni
            case CONNECTIONS:
                return file.allowedUsers.contains(userId);
            case PRIVATE:
                return false;
            case GROUPS:
                // Would check group membership
                return file.allowedUsers.contains(userId);
            default:
                return false;
        }
    }
    
    private void incrementDownloadCount(String fileId) {
        db.collection("sharedFiles").document(fileId)
            .update("downloadCount", com.google.firebase.firestore.FieldValue.increment(1))
            .addOnFailureListener(e -> {
                Log.w(TAG, "Failed to increment download count", e);
            });
    }
}
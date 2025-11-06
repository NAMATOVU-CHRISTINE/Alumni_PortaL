package com.namatovu.alumniportal.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat message model for Alumni Portal
 */
public class ChatMessage {
    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String senderProfileImage;
    private String receiverId;
    private String messageText;
    private String messageType; // "text", "image", "file", "location", "system"
    private String fileUrl;
    private String fileName;
    private String fileType;
    private long fileSizeBytes;
    private long timestamp;
    private boolean isRead;
    private boolean isDelivered;
    private boolean isEdited;
    private boolean isDeleted;
    private long editedAt;
    private long deletedAt;
    private String replyToMessageId;
    private String replyToText;
    private Map<String, Object> metadata;

    // Default constructor required for Firebase
    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.isDelivered = false;
        this.isEdited = false;
        this.isDeleted = false;
        this.messageType = "text";
        this.metadata = new HashMap<>();
    }

    // Constructor for text message
    public ChatMessage(String chatId, String senderId, String senderName, String receiverId, String messageText) {
        this();
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.messageText = messageText;
    }

    // Constructor for file/image message
    public ChatMessage(String chatId, String senderId, String senderName, String receiverId, 
                      String messageType, String fileUrl, String fileName) {
        this();
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.messageType = messageType;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderProfileImage() { return senderProfileImage; }
    public void setSenderProfileImage(String senderProfileImage) { this.senderProfileImage = senderProfileImage; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isDelivered() { return isDelivered; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }

    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public long getEditedAt() { return editedAt; }
    public void setEditedAt(long editedAt) { this.editedAt = editedAt; }

    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }

    public String getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(String replyToMessageId) { this.replyToMessageId = replyToMessageId; }

    public String getReplyToText() { return replyToText; }
    public void setReplyToText(String replyToText) { this.replyToText = replyToText; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Helper methods
    public boolean isTextMessage() { return "text".equals(messageType); }
    public boolean isImageMessage() { return "image".equals(messageType); }
    public boolean isFileMessage() { return "file".equals(messageType); }
    public boolean isSystemMessage() { return "system".equals(messageType); }

    public String getTimeAgo() {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m";
        if (hours < 24) return hours + "h";
        if (days < 7) return days + "d";
        return java.text.DateFormat.getDateInstance().format(new java.util.Date(timestamp));
    }

    public String getFormattedTime() {
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("h:mm a");
        return timeFormat.format(new java.util.Date(timestamp));
    }

    public String getFormattedDate() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return dateFormat.format(new java.util.Date(timestamp));
    }

    public String getDisplayText() {
        if (isDeleted) return "Message deleted";
        
        switch (messageType) {
            case "text":
                return messageText;
            case "image":
                return "📷 Photo";
            case "file":
                return "📎 " + (fileName != null ? fileName : "File");
            case "location":
                return "📍 Location";
            case "system":
                return messageText;
            default:
                return messageText;
        }
    }

    public String getFileSizeFormatted() {
        if (fileSizeBytes <= 0) return "Unknown size";
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSizeBytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    public boolean isReply() {
        return replyToMessageId != null && !replyToMessageId.isEmpty();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsDelivered() {
        this.isDelivered = true;
    }

    public void editMessage(String newText) {
        this.messageText = newText;
        this.isEdited = true;
        this.editedAt = System.currentTimeMillis();
    }

    public void deleteMessage() {
        this.isDeleted = true;
        this.deletedAt = System.currentTimeMillis();
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("chatId", chatId);
        messageMap.put("senderId", senderId);
        messageMap.put("senderName", senderName);
        messageMap.put("senderProfileImage", senderProfileImage);
        messageMap.put("receiverId", receiverId);
        messageMap.put("messageText", messageText);
        messageMap.put("messageType", messageType);
        messageMap.put("fileUrl", fileUrl);
        messageMap.put("fileName", fileName);
        messageMap.put("fileType", fileType);
        messageMap.put("fileSizeBytes", fileSizeBytes);
        messageMap.put("timestamp", timestamp);
        messageMap.put("isRead", isRead);
        messageMap.put("isDelivered", isDelivered);
        messageMap.put("isEdited", isEdited);
        messageMap.put("isDeleted", isDeleted);
        messageMap.put("editedAt", editedAt);
        messageMap.put("deletedAt", deletedAt);
        messageMap.put("replyToMessageId", replyToMessageId);
        messageMap.put("replyToText", replyToText);
        messageMap.put("metadata", metadata);
        return messageMap;
    }
}
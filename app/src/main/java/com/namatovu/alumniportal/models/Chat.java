package com.namatovu.alumniportal.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat conversation model for Alumni Portal
 */
public class Chat {
    private String chatId;
    private List<String> participantIds;
    private Map<String, String> participantNames;
    private Map<String, String> participantImages;
    private String lastMessageText;
    private String lastMessageSenderId;
    private String lastMessageType;
    private long lastMessageTimestamp;
    private long createdAt;
    private long updatedAt;
    private Map<String, Integer> unreadCounts;
    private Map<String, Long> lastSeenTimestamps;
    private boolean isActive;
    private String chatType; // "direct", "group", "mentorship"
    private String chatName;
    private String chatImage;
    private Map<String, Object> metadata;

    // Default constructor required for Firebase
    public Chat() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = true;
        this.chatType = "direct";
        this.unreadCounts = new HashMap<>();
        this.lastSeenTimestamps = new HashMap<>();
        this.participantNames = new HashMap<>();
        this.participantImages = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    // Constructor for direct chat
    public Chat(String user1Id, String user1Name, String user2Id, String user2Name) {
        this();
        this.participantIds = java.util.Arrays.asList(user1Id, user2Id);
        this.participantNames.put(user1Id, user1Name);
        this.participantNames.put(user2Id, user2Name);
        this.unreadCounts.put(user1Id, 0);
        this.unreadCounts.put(user2Id, 0);
        this.lastSeenTimestamps.put(user1Id, createdAt);
        this.lastSeenTimestamps.put(user2Id, createdAt);
    }

    // Constructor for group chat
    public Chat(List<String> participantIds, Map<String, String> participantNames, String chatName) {
        this();
        this.participantIds = participantIds;
        this.participantNames = participantNames;
        this.chatName = chatName;
        this.chatType = "group";
        
        // Initialize unread counts and last seen timestamps
        for (String participantId : participantIds) {
            this.unreadCounts.put(participantId, 0);
            this.lastSeenTimestamps.put(participantId, createdAt);
        }
    }

    // Getters and Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public Map<String, String> getParticipantNames() { return participantNames; }
    public void setParticipantNames(Map<String, String> participantNames) { this.participantNames = participantNames; }

    public Map<String, String> getParticipantImages() { return participantImages; }
    public void setParticipantImages(Map<String, String> participantImages) { this.participantImages = participantImages; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    public String getLastMessageType() { return lastMessageType; }
    public void setLastMessageType(String lastMessageType) { this.lastMessageType = lastMessageType; }

    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Integer> getUnreadCounts() { return unreadCounts; }
    public void setUnreadCounts(Map<String, Integer> unreadCounts) { this.unreadCounts = unreadCounts; }

    public Map<String, Long> getLastSeenTimestamps() { return lastSeenTimestamps; }
    public void setLastSeenTimestamps(Map<String, Long> lastSeenTimestamps) { this.lastSeenTimestamps = lastSeenTimestamps; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getChatType() { return chatType; }
    public void setChatType(String chatType) { this.chatType = chatType; }

    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }

    public String getChatImage() { return chatImage; }
    public void setChatImage(String chatImage) { this.chatImage = chatImage; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Helper methods
    public boolean isDirectChat() { return "direct".equals(chatType); }
    public boolean isGroupChat() { return "group".equals(chatType); }
    public boolean isMentorshipChat() { return "mentorship".equals(chatType); }

    public String getOtherParticipantId(String currentUserId) {
        if (!isDirectChat() || participantIds == null || participantIds.size() != 2) {
            return null;
        }
        
        for (String participantId : participantIds) {
            if (!participantId.equals(currentUserId)) {
                return participantId;
            }
        }
        return null;
    }

    public String getOtherParticipantName(String currentUserId) {
        String otherParticipantId = getOtherParticipantId(currentUserId);
        if (otherParticipantId != null && participantNames != null) {
            return participantNames.get(otherParticipantId);
        }
        return "Unknown User";
    }

    public String getOtherParticipantImage(String currentUserId) {
        String otherParticipantId = getOtherParticipantId(currentUserId);
        if (otherParticipantId != null && participantImages != null) {
            return participantImages.get(otherParticipantId);
        }
        return null;
    }

    public String getDisplayName(String currentUserId) {
        if (isDirectChat()) {
            return getOtherParticipantName(currentUserId);
        } else if (chatName != null && !chatName.isEmpty()) {
            return chatName;
        } else {
            return "Group Chat";
        }
    }

    public String getDisplayImage(String currentUserId) {
        if (isDirectChat()) {
            return getOtherParticipantImage(currentUserId);
        } else {
            return chatImage;
        }
    }

    public int getUnreadCount(String userId) {
        if (unreadCounts != null && unreadCounts.containsKey(userId)) {
            return unreadCounts.get(userId);
        }
        return 0;
    }

    public void incrementUnreadCount(String userId) {
        if (unreadCounts == null) {
            unreadCounts = new HashMap<>();
        }
        int currentCount = unreadCounts.getOrDefault(userId, 0);
        unreadCounts.put(userId, currentCount + 1);
    }

    public void resetUnreadCount(String userId) {
        if (unreadCounts != null) {
            unreadCounts.put(userId, 0);
        }
    }

    public void updateLastSeen(String userId) {
        if (lastSeenTimestamps == null) {
            lastSeenTimestamps = new HashMap<>();
        }
        lastSeenTimestamps.put(userId, System.currentTimeMillis());
    }

    public String getLastMessageDisplayText() {
        if (lastMessageText == null || lastMessageText.isEmpty()) {
            return "No messages yet";
        }
        
        if ("image".equals(lastMessageType)) {
            return "📷 Photo";
        } else if ("file".equals(lastMessageType)) {
            return "📎 File";
        } else if ("location".equals(lastMessageType)) {
            return "📍 Location";
        }
        
        return lastMessageText.length() > 50 ? 
               lastMessageText.substring(0, 47) + "..." : lastMessageText;
    }

    public String getLastMessageTimeAgo() {
        if (lastMessageTimestamp <= 0) return "";
        
        long diff = System.currentTimeMillis() - lastMessageTimestamp;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (minutes < 1) return "Now";
        if (minutes < 60) return minutes + "m";
        if (hours < 24) return hours + "h";
        if (days < 7) return days + "d";
        
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd");
        return dateFormat.format(new java.util.Date(lastMessageTimestamp));
    }

    public void updateWithMessage(ChatMessage message) {
        this.lastMessageText = message.getDisplayText();
        this.lastMessageSenderId = message.getSenderId();
        this.lastMessageType = message.getMessageType();
        this.lastMessageTimestamp = message.getTimestamp();
        this.updatedAt = System.currentTimeMillis();
        
        // Increment unread count for all participants except sender
        if (participantIds != null) {
            for (String participantId : participantIds) {
                if (!participantId.equals(message.getSenderId())) {
                    incrementUnreadCount(participantId);
                }
            }
        }
    }

    public boolean hasUnreadMessages(String userId) {
        return getUnreadCount(userId) > 0;
    }

    public static String generateChatId(String user1Id, String user2Id) {
        // Create consistent chat ID for direct messages
        String[] userIds = {user1Id, user2Id};
        java.util.Arrays.sort(userIds);
        return userIds[0] + "_" + userIds[1];
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> chatMap = new HashMap<>();
        chatMap.put("participantIds", participantIds);
        chatMap.put("participantNames", participantNames);
        chatMap.put("participantImages", participantImages);
        chatMap.put("lastMessageText", lastMessageText);
        chatMap.put("lastMessageSenderId", lastMessageSenderId);
        chatMap.put("lastMessageType", lastMessageType);
        chatMap.put("lastMessageTimestamp", lastMessageTimestamp);
        chatMap.put("createdAt", createdAt);
        chatMap.put("updatedAt", updatedAt);
        chatMap.put("unreadCounts", unreadCounts);
        chatMap.put("lastSeenTimestamps", lastSeenTimestamps);
        chatMap.put("isActive", isActive);
        chatMap.put("chatType", chatType);
        chatMap.put("chatName", chatName);
        chatMap.put("chatImage", chatImage);
        chatMap.put("metadata", metadata);
        return chatMap;
    }
}
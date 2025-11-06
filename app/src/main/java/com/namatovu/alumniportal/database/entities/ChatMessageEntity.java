package com.namatovu.alumniportal.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

/**
 * Room entity for offline chat message storage
 */
@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "message_id")
    public String messageId;
    
    @ColumnInfo(name = "chat_id")
    public String chatId;
    
    @ColumnInfo(name = "sender_id")
    public String senderId;
    
    @ColumnInfo(name = "sender_name")
    public String senderName;
    
    @ColumnInfo(name = "content")
    public String content;
    
    @ColumnInfo(name = "message_type")
    public String messageType;
    
    @ColumnInfo(name = "file_url")
    public String fileUrl;
    
    @ColumnInfo(name = "file_name")
    public String fileName;
    
    @ColumnInfo(name = "file_size")
    public long fileSize;
    
    @ColumnInfo(name = "timestamp")
    public long timestamp;
    
    @ColumnInfo(name = "read_status")
    public String readStatus;
    
    @ColumnInfo(name = "read_timestamp")
    public long readTimestamp;
    
    @ColumnInfo(name = "reply_to_message_id")
    public String replyToMessageId;
    
    @ColumnInfo(name = "is_edited")
    public boolean isEdited;
    
    @ColumnInfo(name = "edit_timestamp")
    public long editTimestamp;
    
    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;
    
    @ColumnInfo(name = "delete_timestamp")
    public long deleteTimestamp;
    
    @ColumnInfo(name = "sync_status")
    public String syncStatus; // "synced", "pending", "failed"
    
    @ColumnInfo(name = "created_at")
    public long createdAt;
    
    @ColumnInfo(name = "updated_at")
    public long updatedAt;
    
    // Default constructor
    public ChatMessageEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending";
    }
}
package com.namatovu.alumniportal.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey
    @NonNull
    public String messageId;
    public String chatId;
    public String senderId;
    public String senderName;
    public String content;
    public String messageType;
    public String fileUrl;
    public String fileName;
    public long fileSize;
    public long timestamp;
    public boolean readStatus;
    public long readTimestamp;
    public String replyToMessageId;
    public boolean isEdited;
    public long editTimestamp;
    public boolean isDeleted;
    public long deleteTimestamp;
    public long updatedAt;
    public String syncStatus;
}

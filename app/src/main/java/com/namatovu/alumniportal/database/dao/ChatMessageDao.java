package com.namatovu.alumniportal.database.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.namatovu.alumniportal.database.entities.ChatMessageEntity;
import java.util.List;

/**
 * Data Access Object for chat messages
 */
@Dao
public interface ChatMessageDao {
    
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY timestamp ASC")
    LiveData<List<ChatMessageEntity>> getMessagesForChat(String chatId);
    
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY timestamp ASC")
    List<ChatMessageEntity> getMessagesForChatSync(String chatId);
    
    @Query("SELECT * FROM chat_messages WHERE sync_status = :syncStatus")
    List<ChatMessageEntity> getMessagesBySyncStatus(String syncStatus);
    
    @Query("SELECT * FROM chat_messages WHERE sync_status = 'pending' OR sync_status = 'failed'")
    List<ChatMessageEntity> getUnsyncedMessages();
    
    @Query("SELECT * FROM chat_messages WHERE message_id = :messageId")
    ChatMessageEntity getMessageById(String messageId);
    
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId AND timestamp > :timestamp ORDER BY timestamp ASC")
    List<ChatMessageEntity> getMessagesAfterTimestamp(String chatId, long timestamp);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE chat_id = :chatId")
    int getMessageCount(String chatId);
    
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY timestamp DESC LIMIT 1")
    ChatMessageEntity getLastMessageForChat(String chatId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(ChatMessageEntity message);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<ChatMessageEntity> messages);
    
    @Update
    void updateMessage(ChatMessageEntity message);
    
    @Query("UPDATE chat_messages SET sync_status = :syncStatus, updated_at = :updatedAt WHERE message_id = :messageId")
    void updateSyncStatus(String messageId, String syncStatus, long updatedAt);
    
    @Query("UPDATE chat_messages SET read_status = :readStatus, read_timestamp = :readTimestamp WHERE message_id = :messageId")
    void updateReadStatus(String messageId, String readStatus, long readTimestamp);
    
    @Delete
    void deleteMessage(ChatMessageEntity message);
    
    @Query("DELETE FROM chat_messages WHERE message_id = :messageId")
    void deleteMessageById(String messageId);
    
    @Query("DELETE FROM chat_messages WHERE chat_id = :chatId")
    void deleteMessagesForChat(String chatId);
    
    @Query("DELETE FROM chat_messages WHERE timestamp < :timestamp")
    void deleteOldMessages(long timestamp);
    
    @Query("DELETE FROM chat_messages")
    void deleteAllMessages();
}
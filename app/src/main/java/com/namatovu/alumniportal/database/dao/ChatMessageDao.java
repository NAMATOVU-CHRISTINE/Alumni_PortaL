package com.namatovu.alumniportal.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.namatovu.alumniportal.database.entities.ChatMessageEntity;
import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<ChatMessageEntity> messages);

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    LiveData<List<ChatMessageEntity>> getMessagesForChat(String chatId);

    @Query("DELETE FROM chat_messages")
    void clearAll();
}

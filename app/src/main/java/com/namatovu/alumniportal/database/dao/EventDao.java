package com.namatovu.alumniportal.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.namatovu.alumniportal.database.entities.EventEntity;
import java.util.List;

@Dao
public interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvents(List<EventEntity> events);

    @Query("SELECT * FROM events WHERE eventId = :eventId")
    LiveData<EventEntity> getEventById(String eventId);

    @Query("SELECT * FROM events ORDER BY dateTime DESC")
    LiveData<List<EventEntity>> getAllEvents();

    @Query("DELETE FROM events")
    void clearAll();
}

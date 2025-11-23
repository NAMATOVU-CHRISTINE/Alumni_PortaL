package com.namatovu.alumniportal.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.namatovu.alumniportal.database.entities.EventEntity;

import java.util.List;

@Dao
public interface EventDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(EventEntity event);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvents(List<EventEntity> events);
    
    @Update
    void updateEvent(EventEntity event);
    
    @Delete
    void deleteEvent(EventEntity event);
    
    @Query("SELECT * FROM events WHERE eventId = :eventId")
    EventEntity getEventById(String eventId);
    
    @Query("SELECT * FROM events ORDER BY eventDate ASC")
    List<EventEntity> getAllEvents();
    
    @Query("SELECT * FROM events WHERE eventDate >= :currentTime ORDER BY eventDate ASC")
    List<EventEntity> getUpcomingEvents(long currentTime);
    
    @Query("SELECT * FROM events WHERE eventDate < :currentTime ORDER BY eventDate DESC")
    List<EventEntity> getPastEvents(long currentTime);
    
    @Query("SELECT * FROM events WHERE isAttending = 1 ORDER BY eventDate ASC")
    List<EventEntity> getAttendingEvents();
    
    @Query("SELECT * FROM events WHERE category = :category ORDER BY eventDate ASC")
    List<EventEntity> getEventsByCategory(String category);
    
    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY eventDate ASC")
    List<EventEntity> searchEvents(String query);
    
    @Query("DELETE FROM events")
    void deleteAllEvents();
    
    @Query("DELETE FROM events WHERE eventDate < :cutoffTime")
    void deleteOldEvents(long cutoffTime);
    
    @Query("SELECT COUNT(*) FROM events")
    int getEventCount();
    
    @Query("UPDATE events SET isAttending = :isAttending, attendeeCount = :attendeeCount WHERE eventId = :eventId")
    void updateEventAttendance(String eventId, boolean isAttending, int attendeeCount);
}

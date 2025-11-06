package com.namatovu.alumniportal.database.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.namatovu.alumniportal.database.entities.EventEntity;
import java.util.List;

/**
 * Data Access Object for events
 */
@Dao
public interface EventDao {
    
    @Query("SELECT * FROM events WHERE is_active = 1 ORDER BY date_time ASC")
    LiveData<List<EventEntity>> getAllActiveEvents();
    
    @Query("SELECT * FROM events WHERE is_active = 1 ORDER BY date_time ASC")
    List<EventEntity> getAllActiveEventsSync();
    
    @Query("SELECT * FROM events WHERE event_id = :eventId")
    LiveData<EventEntity> getEventById(String eventId);
    
    @Query("SELECT * FROM events WHERE event_id = :eventId")
    EventEntity getEventByIdSync(String eventId);
    
    @Query("SELECT * FROM events WHERE organizer_id = :organizerId ORDER BY date_time DESC")
    List<EventEntity> getEventsByOrganizer(String organizerId);
    
    @Query("SELECT * FROM events WHERE category = :category AND is_active = 1 ORDER BY date_time ASC")
    List<EventEntity> getEventsByCategory(String category);
    
    @Query("SELECT * FROM events WHERE date_time >= :startTime AND date_time <= :endTime AND is_active = 1 ORDER BY date_time ASC")
    List<EventEntity> getEventsInDateRange(long startTime, long endTime);
    
    @Query("SELECT * FROM events WHERE date_time > :currentTime AND is_active = 1 ORDER BY date_time ASC")
    List<EventEntity> getUpcomingEvents(long currentTime);
    
    @Query("SELECT * FROM events WHERE date_time < :currentTime AND is_active = 1 ORDER BY date_time DESC")
    List<EventEntity> getPastEvents(long currentTime);
    
    @Query("SELECT * FROM events WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND is_active = 1 ORDER BY date_time ASC")
    List<EventEntity> searchEvents(String query);
    
    @Query("SELECT * FROM events WHERE registration_deadline > :currentTime AND date_time > :currentTime AND is_active = 1 ORDER BY registration_deadline ASC")
    List<EventEntity> getEventsWithOpenRegistration(long currentTime);
    
    @Query("SELECT * FROM events WHERE is_virtual = :isVirtual AND is_active = 1 ORDER BY date_time ASC")
    List<EventEntity> getEventsByVirtualStatus(boolean isVirtual);
    
    @Query("SELECT * FROM events WHERE sync_status = :syncStatus")
    List<EventEntity> getEventsBySyncStatus(String syncStatus);
    
    @Query("SELECT * FROM events WHERE sync_status = 'pending' OR sync_status = 'failed'")
    List<EventEntity> getUnsyncedEvents();
    
    @Query("SELECT * FROM events WHERE last_sync < :timestamp")
    List<EventEntity> getEventsNeedingSync(long timestamp);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(EventEntity event);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvents(List<EventEntity> events);
    
    @Update
    void updateEvent(EventEntity event);
    
    @Query("UPDATE events SET sync_status = :syncStatus, last_sync = :lastSync WHERE event_id = :eventId")
    void updateSyncStatus(String eventId, String syncStatus, long lastSync);
    
    @Query("UPDATE events SET current_attendees = :currentAttendees WHERE event_id = :eventId")
    void updateAttendeeCount(String eventId, int currentAttendees);
    
    @Query("UPDATE events SET is_active = :isActive WHERE event_id = :eventId")
    void updateActiveStatus(String eventId, boolean isActive);
    
    @Delete
    void deleteEvent(EventEntity event);
    
    @Query("DELETE FROM events WHERE event_id = :eventId")
    void deleteEventById(String eventId);
    
    @Query("DELETE FROM events WHERE date_time < :timestamp AND is_active = 0")
    void deleteOldInactiveEvents(long timestamp);
    
    @Query("DELETE FROM events")
    void deleteAllEvents();
}
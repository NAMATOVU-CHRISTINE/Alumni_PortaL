package com.namatovu.alumniportal.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventEntity event);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EventEntity> events);

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    EventEntity findById(String id);

    @Query("SELECT * FROM events")
    List<EventEntity> getAll();

    @Query("DELETE FROM events")
    void deleteAll();
}

package com.namatovu.alumniportal.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(JobEntity job);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<JobEntity> jobs);

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    JobEntity findById(String id);

    @Query("SELECT * FROM jobs")
    List<JobEntity> getAll();

    @Query("DELETE FROM jobs")
    void deleteAll();
}

package com.namatovu.alumniportal.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.namatovu.alumniportal.database.entities.JobPostingEntity;
import java.util.List;

@Dao
public interface JobPostingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJobs(List<JobPostingEntity> jobs);

    @Query("SELECT * FROM job_postings WHERE jobId = :jobId")
    LiveData<JobPostingEntity> getJobById(String jobId);

    @Query("SELECT * FROM job_postings ORDER BY postedAt DESC")
    LiveData<List<JobPostingEntity>> getAllJobs();

    @Query("DELETE FROM job_postings")
    void clearAll();
}

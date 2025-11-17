package com.namatovu.alumniportal.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.namatovu.alumniportal.database.entities.JobEntity;

import java.util.List;

@Dao
public interface JobDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJob(JobEntity job);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJobs(List<JobEntity> jobs);
    
    @Update
    void updateJob(JobEntity job);
    
    @Delete
    void deleteJob(JobEntity job);
    
    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    JobEntity getJobById(String jobId);
    
    @Query("SELECT * FROM jobs ORDER BY postedDate DESC")
    List<JobEntity> getAllJobs();
    
    @Query("SELECT * FROM jobs WHERE isSaved = 1 ORDER BY postedDate DESC")
    List<JobEntity> getSavedJobs();
    
    @Query("SELECT * FROM jobs WHERE jobType = :jobType ORDER BY postedDate DESC")
    List<JobEntity> getJobsByType(String jobType);
    
    @Query("SELECT * FROM jobs WHERE company LIKE '%' || :company || '%' ORDER BY postedDate DESC")
    List<JobEntity> getJobsByCompany(String company);
    
    @Query("SELECT * FROM jobs WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY postedDate DESC")
    List<JobEntity> searchJobs(String query);
    
    @Query("DELETE FROM jobs")
    void deleteAllJobs();
    
    @Query("DELETE FROM jobs WHERE deadline < :currentTime")
    void deleteExpiredJobs(long currentTime);
    
    @Query("SELECT COUNT(*) FROM jobs")
    int getJobCount();
    
    @Query("UPDATE jobs SET isSaved = :isSaved WHERE jobId = :jobId")
    void updateJobSavedStatus(String jobId, boolean isSaved);
}

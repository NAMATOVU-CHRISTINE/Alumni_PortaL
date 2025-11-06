package com.namatovu.alumniportal.database.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.namatovu.alumniportal.database.entities.JobPostingEntity;
import java.util.List;

/**
 * Data Access Object for job postings
 */
@Dao
public interface JobPostingDao {
    
    @Query("SELECT * FROM job_postings WHERE is_active = 1 ORDER BY posted_at DESC")
    LiveData<List<JobPostingEntity>> getAllActiveJobs();
    
    @Query("SELECT * FROM job_postings WHERE is_active = 1 ORDER BY posted_at DESC")
    List<JobPostingEntity> getAllActiveJobsSync();
    
    @Query("SELECT * FROM job_postings WHERE job_id = :jobId")
    LiveData<JobPostingEntity> getJobById(String jobId);
    
    @Query("SELECT * FROM job_postings WHERE job_id = :jobId")
    JobPostingEntity getJobByIdSync(String jobId);
    
    @Query("SELECT * FROM job_postings WHERE posted_by_user_id = :userId ORDER BY posted_at DESC")
    List<JobPostingEntity> getJobsByUser(String userId);
    
    @Query("SELECT * FROM job_postings WHERE job_type = :jobType AND is_active = 1 ORDER BY posted_at DESC")
    List<JobPostingEntity> getJobsByType(String jobType);
    
    @Query("SELECT * FROM job_postings WHERE experience_level = :experienceLevel AND is_active = 1 ORDER BY posted_at DESC")
    List<JobPostingEntity> getJobsByExperienceLevel(String experienceLevel);
    
    @Query("SELECT * FROM job_postings WHERE (position LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND is_active = 1 ORDER BY posted_at DESC")
    List<JobPostingEntity> searchJobs(String query);
    
    @Query("SELECT * FROM job_postings WHERE application_deadline > :currentTime AND is_active = 1 ORDER BY application_deadline ASC")
    List<JobPostingEntity> getJobsWithUpcomingDeadlines(long currentTime);
    
    @Query("SELECT * FROM job_postings WHERE sync_status = :syncStatus")
    List<JobPostingEntity> getJobsBySyncStatus(String syncStatus);
    
    @Query("SELECT * FROM job_postings WHERE sync_status = 'pending' OR sync_status = 'failed'")
    List<JobPostingEntity> getUnsyncedJobs();
    
    @Query("SELECT * FROM job_postings WHERE last_sync < :timestamp")
    List<JobPostingEntity> getJobsNeedingSync(long timestamp);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJob(JobPostingEntity job);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJobs(List<JobPostingEntity> jobs);
    
    @Update
    void updateJob(JobPostingEntity job);
    
    @Query("UPDATE job_postings SET sync_status = :syncStatus, last_sync = :lastSync WHERE job_id = :jobId")
    void updateSyncStatus(String jobId, String syncStatus, long lastSync);
    
    @Query("UPDATE job_postings SET is_active = :isActive WHERE job_id = :jobId")
    void updateActiveStatus(String jobId, boolean isActive);
    
    @Delete
    void deleteJob(JobPostingEntity job);
    
    @Query("DELETE FROM job_postings WHERE job_id = :jobId")
    void deleteJobById(String jobId);
    
    @Query("DELETE FROM job_postings WHERE application_deadline < :timestamp")
    void deleteExpiredJobs(long timestamp);
    
    @Query("DELETE FROM job_postings")
    void deleteAllJobs();
}
package com.namatovu.alumniportal.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.namatovu.alumniportal.database.entities.MentorEntity;

import java.util.List;

@Dao
public interface MentorDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMentor(MentorEntity mentor);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMentors(List<MentorEntity> mentors);
    
    @Update
    void updateMentor(MentorEntity mentor);
    
    @Delete
    void deleteMentor(MentorEntity mentor);
    
    @Query("SELECT * FROM mentors WHERE mentorId = :mentorId")
    MentorEntity getMentorById(String mentorId);
    
    @Query("SELECT * FROM mentors ORDER BY fullName ASC")
    List<MentorEntity> getAllMentors();
    
    @Query("SELECT * FROM mentors WHERE isAvailable = 1 ORDER BY rating DESC")
    List<MentorEntity> getAvailableMentors();
    
    @Query("SELECT * FROM mentors WHERE category = :category ORDER BY rating DESC")
    List<MentorEntity> getMentorsByCategory(String category);
    
    @Query("SELECT * FROM mentors WHERE graduationYear = :year ORDER BY fullName ASC")
    List<MentorEntity> getMentorsByGraduationYear(String year);
    
    @Query("SELECT * FROM mentors WHERE expertise LIKE '%' || :expertise || '%' ORDER BY rating DESC")
    List<MentorEntity> getMentorsByExpertise(String expertise);
    
    @Query("SELECT * FROM mentors WHERE fullName LIKE '%' || :query || '%' OR expertise LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' ORDER BY rating DESC")
    List<MentorEntity> searchMentors(String query);
    
    @Query("SELECT DISTINCT category FROM mentors WHERE category IS NOT NULL ORDER BY category ASC")
    List<String> getAllCategories();
    
    @Query("SELECT DISTINCT graduationYear FROM mentors WHERE graduationYear IS NOT NULL ORDER BY graduationYear DESC")
    List<String> getAllGraduationYears();
    
    @Query("DELETE FROM mentors")
    void deleteAllMentors();
    
    @Query("SELECT COUNT(*) FROM mentors")
    int getMentorCount();
}

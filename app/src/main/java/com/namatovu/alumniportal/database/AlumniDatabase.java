package com.namatovu.alumniportal.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.namatovu.alumniportal.database.dao.EventDao;
import com.namatovu.alumniportal.database.dao.JobDao;
import com.namatovu.alumniportal.database.dao.UserDao;
import com.namatovu.alumniportal.database.entities.EventEntity;
import com.namatovu.alumniportal.database.entities.JobEntity;
import com.namatovu.alumniportal.database.entities.UserEntity;

@Database(
    entities = {UserEntity.class, JobEntity.class, EventEntity.class, com.namatovu.alumniportal.database.entities.MentorEntity.class},
    version = 2,
    exportSchema = false
)
public abstract class AlumniDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "alumni_portal_db";
    private static volatile AlumniDatabase INSTANCE;
    
    // DAOs
    public abstract UserDao userDao();
    public abstract JobDao jobDao();
    public abstract EventDao eventDao();
    public abstract com.namatovu.alumniportal.database.dao.MentorDao mentorDao();
    
    // Singleton pattern
    public static AlumniDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AlumniDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AlumniDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration() // For development
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    // Clear all data
    public void clearAllTables() {
        if (INSTANCE != null) {
            INSTANCE.clearAllTables();
        }
    }
}

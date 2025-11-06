package com.namatovu.alumniportal.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.namatovu.alumniportal.database.entities.ChatMessageEntity;
import com.namatovu.alumniportal.database.entities.UserEntity;
import com.namatovu.alumniportal.database.entities.JobPostingEntity;
import com.namatovu.alumniportal.database.entities.EventEntity;
import com.namatovu.alumniportal.database.dao.ChatMessageDao;
import com.namatovu.alumniportal.database.dao.UserDao;
import com.namatovu.alumniportal.database.dao.JobPostingDao;
import com.namatovu.alumniportal.database.dao.EventDao;

/**
 * Main Room database for offline storage
 */
@Database(
    entities = {
        ChatMessageEntity.class,
        UserEntity.class,
        JobPostingEntity.class,
        EventEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AlumniDatabase extends RoomDatabase {
    
    private static volatile AlumniDatabase INSTANCE;
    private static final String DATABASE_NAME = "alumni_portal_database";
    
    // Abstract methods to get DAOs
    public abstract ChatMessageDao chatMessageDao();
    public abstract UserDao userDao();
    public abstract JobPostingDao jobPostingDao();
    public abstract EventDao eventDao();
    
    /**
     * Get database instance (Singleton pattern)
     */
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
    
    /**
     * Clear all data from database
     */
    public void clearAllData() {
        if (INSTANCE != null) {
            INSTANCE.runInTransaction(() -> {
                INSTANCE.chatMessageDao().deleteAllMessages();
                INSTANCE.userDao().deleteAllUsers();
                INSTANCE.jobPostingDao().deleteAllJobs();
                INSTANCE.eventDao().deleteAllEvents();
            });
        }
    }
    
    /**
     * Close database
     */
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}
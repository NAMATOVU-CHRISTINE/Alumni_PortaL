package com.namatovu.alumniportal.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.namatovu.alumniportal.database.dao.ChatMessageDao;
import com.namatovu.alumniportal.database.dao.EventDao;
import com.namatovu.alumniportal.database.dao.JobPostingDao;
import com.namatovu.alumniportal.database.dao.UserDao;
import com.namatovu.alumniportal.database.entities.ChatMessageEntity;
import com.namatovu.alumniportal.database.entities.EventEntity;
import com.namatovu.alumniportal.database.entities.JobPostingEntity;
import com.namatovu.alumniportal.database.entities.UserEntity;

@Database(entities = {UserEntity.class, JobPostingEntity.class, EventEntity.class, ChatMessageEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract JobPostingDao jobPostingDao();
    public abstract EventDao eventDao();
    public abstract ChatMessageDao chatMessageDao();

    public void clearAllData() {
        userDao().clearAll();
        jobPostingDao().clearAll();
        eventDao().clearAll();
        chatMessageDao().clearAll();
    }

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "alumni_portal_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

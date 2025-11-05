package com.namatovu.alumniportal.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {JobEntity.class, EventEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "alumni_portal_db";

    public abstract JobDao jobDao();
    public abstract EventDao eventDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

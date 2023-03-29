package com.example.ttgo_smartwatch_app.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.ttgo_smartwatch_app.database.entity.Movement;

@Database(entities = {Movement.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao appDao();
}

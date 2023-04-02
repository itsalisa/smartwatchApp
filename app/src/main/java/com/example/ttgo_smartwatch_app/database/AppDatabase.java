package com.example.ttgo_smartwatch_app.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.ttgo_smartwatch_app.database.entity.Movement;
import com.example.ttgo_smartwatch_app.database.entity.Date;
import com.example.ttgo_smartwatch_app.database.entity.Time;
import com.example.ttgo_smartwatch_app.database.entity.Location;

@Database(entities = {Movement.class, Date.class, Time.class, Location.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao appDao();
}


package com.example.ttgo_smartwatch_app.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseManager {

    private AppDatabase db;
    public AppDao dao;

    public DatabaseManager(Context context) {
        db = Room.databaseBuilder(context, AppDatabase.class, "watch-database").build();
        dao = db.appDao();
    }

}

package com.example.ttgo_smartwatch_app.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "times")
public class Time {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "hour")
    public int hour;

    @ColumnInfo(name = "minutes")
    public int minutes;

    @ColumnInfo(name = "seconds")
    public int seconds;

}

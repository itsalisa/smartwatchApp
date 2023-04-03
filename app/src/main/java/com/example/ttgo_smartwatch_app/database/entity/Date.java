package com.example.ttgo_smartwatch_app.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dates")
public class Date {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "year")
    public int year;

    @ColumnInfo(name = "month")
    public int month;

    @ColumnInfo(name = "day")
    public int day;

}

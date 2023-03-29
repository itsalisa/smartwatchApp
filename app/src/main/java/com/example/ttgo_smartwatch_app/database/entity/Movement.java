package com.example.ttgo_smartwatch_app.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Movement {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "battery")
    public int battery;

    @ColumnInfo(name = "temperature")
    public int temperature;

}

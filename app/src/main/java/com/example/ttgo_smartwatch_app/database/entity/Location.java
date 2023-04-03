package com.example.ttgo_smartwatch_app.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class Location {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "lattitude")
    public int lattitude;

    @ColumnInfo(name = "longitude")
    public int longitude;

}

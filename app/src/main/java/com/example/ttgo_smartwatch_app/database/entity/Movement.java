package com.example.ttgo_smartwatch_app.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "movements")
public class Movement {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "battery")
    public int battery;

    @ColumnInfo(name = "temperature")
    public int temperature;

    @ColumnInfo(name = "is_Charging")
    public int isCharging;

    @ColumnInfo(name = "accelerometer_x")
    public int accelerometerX;

    @ColumnInfo(name = "accelerometer_y")
    public int accelerometerY;

    @ColumnInfo(name = "accelerometer_z")
    public int accelerometerZ;

    @ColumnInfo(name = "Step_Counter")
    public int StepCounter;

    @ColumnInfo(name = "timestamp")
    public long timeStamp;
}


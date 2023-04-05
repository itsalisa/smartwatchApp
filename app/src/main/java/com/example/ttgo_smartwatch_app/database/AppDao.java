package com.example.ttgo_smartwatch_app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ttgo_smartwatch_app.database.entity.Date;
import com.example.ttgo_smartwatch_app.database.entity.Location;
import com.example.ttgo_smartwatch_app.database.entity.Movement;
import com.example.ttgo_smartwatch_app.database.entity.Time;

import java.util.List;

@Dao
public interface AppDao {

    @Query("SELECT * FROM movements")
    List<Movement> getAllMovements();

    @Query("SELECT * FROM movements WHERE timestamp > :date ORDER BY timestamp")
    List<Movement> getLastMovements(long date);

    @Query("SELECT * FROM dates")
    List<Date> getAllDates();

    @Query("SELECT * FROM times")
    List<Time> getAllTimes();

    @Query("SELECT * FROM locations")
    List<Location> getAllLocations();

    @Insert
    void insertAllMovements(Movement... movements);

    @Insert
    void insertAllDates(Date... dates);

    @Insert
    void insertAllTimes(Time... times);

    @Insert
    void insertAllLocations(Location... locations);

    @Delete
    void delete(Movement user);

    @Delete
    void delete(Date user);

    @Delete
    void delete(Time user);

    @Delete
    void delete(Location user);

}

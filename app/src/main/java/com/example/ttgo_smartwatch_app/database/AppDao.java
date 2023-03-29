package com.example.ttgo_smartwatch_app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ttgo_smartwatch_app.database.entity.Movement;

import java.util.List;

@Dao
public interface AppDao {

    @Query("SELECT * FROM movement")
    List<Movement> getAllMovements();

    @Insert
    void insertAllMovements(Movement... movements);

    @Delete
    void delete(Movement user);

}

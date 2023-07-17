package com.example.absensiapp.service;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.absensiapp.model.Absensi;

import java.util.List;

@Dao
public interface AbsensiDAO {
    @Query("SELECT * FROM Absensi")
    List<Absensi> getAllAbsensi();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAbsensi(Absensi absensi);

}

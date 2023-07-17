package com.example.absensiapp.service;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.absensiapp.model.Absensi;
import com.example.absensiapp.model.Converters;

@Database(entities = {Absensi.class}, version = 1)
@TypeConverters(Converters.class)
public abstract class AbsensiDatabase extends RoomDatabase {
    public abstract AbsensiDAO absensiDAO();
}

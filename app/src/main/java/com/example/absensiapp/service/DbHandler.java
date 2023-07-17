package com.example.absensiapp.service;

import android.content.Context;

import androidx.room.Room;

public class DbHandler {
    private Context context;
    private AbsensiDatabase database;
    private static DbHandler instance;

    public DbHandler(Context context){
        this.context = context;
        database = Room.databaseBuilder(context, AbsensiDatabase.class,"Absensi.db").allowMainThreadQueries().build();
    }


    public static DbHandler getInstance(Context context) {
        if (instance==null){
            instance = new DbHandler(context);
        }
        return instance;
    }
    public AbsensiDatabase getDatabase(){
        return database;
    }
}

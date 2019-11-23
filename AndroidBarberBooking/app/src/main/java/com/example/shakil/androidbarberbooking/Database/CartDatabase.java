package com.example.shakil.androidbarberbooking.Database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Context;

@Database(version = 1, entities = CartItem.class, exportSchema = false)
public abstract class CartDatabase extends RoomDatabase {
    private static CartDatabase instence;

    public abstract CartDAO cartDAO();

    public static CartDatabase getInstance(Context context) {
        if (instence == null) {
            instence = Room.databaseBuilder(context, CartDatabase.class, "MtBarBerDB").build();
        }
        return instence;
    }
}
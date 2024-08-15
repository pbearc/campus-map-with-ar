package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BLEData.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "rssi_data";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "mac1_rssi INTEGER, " +
                    "mac2_rssi INTEGER, " +
                    "mac3_rssi INTEGER, " +
                    "point TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.d("database", "onupdate being invoked");
    }

    public void insertRSSIData(int mac1_rssi, int mac2_rssi, int mac3_rssi, String point) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mac1_rssi", mac1_rssi);
        values.put("mac2_rssi", mac2_rssi);
        values.put("mac3_rssi", mac3_rssi);
        values.put("point", point);
        db.insert(TABLE_NAME, null, values);
    }

    public void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }
}

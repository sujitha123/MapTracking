package com.sujitha.maptracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class OfflineDatabase extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "trackingMap.db";
    private static final int DATABASE_VERSION = 1;
    private static final String OFFLINE_LOC_TABEL = "OFFLINE_LOC_TABEL";
    // COL
    private static final String OFFLINE_LOC_ID = "OFFLINE_LOC_ID";
    private static final String OFFLINE_LOC_LONGITUDE = "OFFLINE_LOC_LONGITUDE";
    private static final String OFFLINE_LOC_LATITUDE = "OFFLINE_LOC_LATITUDE";
    private static final String OFFLINE_LOC_TIME_STAMP = "OFFLINE_LOC_TIME_STAMP";


    public OfflineDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_OFFLINE_LOC_DB = "CREATE TABLE " + OFFLINE_LOC_TABEL + "(" +
                OFFLINE_LOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                OFFLINE_LOC_LONGITUDE + " TEXT," +
                OFFLINE_LOC_LATITUDE + " TEXT," +
                OFFLINE_LOC_TIME_STAMP + " DEFAULT CURRENT_TIMESTAMP" + ");";
        db.execSQL(CREATE_OFFLINE_LOC_DB);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    //////////////////////////// add user loc to db
    public int addUserLocToDbWhenUserisOffline(UserLocDataModel userLocDataModel) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OFFLINE_LOC_LONGITUDE, userLocDataModel.getLongitude());
        values.put(OFFLINE_LOC_LATITUDE, userLocDataModel.getLatitude());
        values.put(OFFLINE_LOC_TIME_STAMP, userLocDataModel.getUnixTimeStamp());
        int checkValue = (int) database.insert(OFFLINE_LOC_TABEL, null, values);
        database.close();
        return checkValue;
    }

    ////////////////////////////// get user loc from db
    public ArrayList<UserLocDataModel> getUserLocFromDb() {
        ArrayList<UserLocDataModel> userLocDataModels = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM " + OFFLINE_LOC_TABEL;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                userLocDataModels.add(new UserLocDataModel(cursor.getString(cursor.getColumnIndex(OFFLINE_LOC_ID)),
                        cursor.getString(cursor.getColumnIndex(OFFLINE_LOC_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(OFFLINE_LOC_LATITUDE)),
                        cursor.getString(cursor.getColumnIndex(OFFLINE_LOC_TIME_STAMP))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        sqLiteDatabase.close();
        return userLocDataModels;
    }


    ////////////////////////////// get user loc from db
    public int getUserTrackCountFromDb() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM " + OFFLINE_LOC_TABEL;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        return cursor.getCount();
    }


}

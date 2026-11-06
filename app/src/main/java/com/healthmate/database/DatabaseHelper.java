package com.healthmate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "healthmate.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "health_records";

    public static final String COL_ID = "id";
    public static final String COL_DATE = "date";
    public static final String COL_STEPS = "steps";
    public static final String COL_CALORIES = "calories";
    public static final String COL_WATER = "water";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create the database table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_STEPS + " INTEGER NOT NULL, " +
                COL_CALORIES + " INTEGER NOT NULL, " +
                COL_WATER + " INTEGER NOT NULL)";

        db.execSQL(createTable);
    }

    // Handle future database upgrades
    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // CREATE: Add a new health record
    public long addRecord(String date, int steps,
                          int calories, int water) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);

        return db.insert(TABLE_NAME, null, values);
    }

    // READ: Retrieve all health records
    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COL_DATE + " DESC, " + COL_ID + " DESC"
        );
    }

    // UPDATE: Modify an existing health record
    public int updateRecord(int id, String date,
                            int steps, int calories, int water) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);

        return db.update(
                TABLE_NAME,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // DELETE: Remove a health record
    public int deleteRecord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                TABLE_NAME,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // SEARCH: Retrieve records for a selected date
    public Cursor getRecordsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_NAME,
                null,
                COL_DATE + "=?",
                new String[]{date},
                null,
                null,
                COL_ID + " DESC"
        );
    }

    public Cursor getRecordById(int id) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_NAME,
                null,
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
    }




}

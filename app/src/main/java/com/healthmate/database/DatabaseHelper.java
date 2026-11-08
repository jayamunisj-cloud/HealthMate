package com.healthmate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.healthmate.PasswordUtils;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "healthmate.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_NAME = "health_records";

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME = "last_name";
    public static final String COL_MOBILE = "mobile";
    public static final String COL_DATE_OF_BIRTH = "date_of_birth";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_PASSWORD_SALT = "password_salt";


    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERNAME + " TEXT NOT NULL, " +
                    COL_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COL_PASSWORD_HASH + " TEXT NOT NULL, " +
                    COL_PASSWORD_SALT + " TEXT NOT NULL, " +
                    COL_FIRST_NAME + " TEXT, " +
                    COL_LAST_NAME + " TEXT, " +
                    COL_MOBILE + " TEXT, " +
                    COL_DATE_OF_BIRTH + " TEXT)";


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
        db.execSQL(CREATE_USERS_TABLE);
    }

    // Handle future database upgrades

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL(CREATE_USERS_TABLE);
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS +
                    " ADD COLUMN " + COL_FIRST_NAME + " TEXT");

            db.execSQL("ALTER TABLE " + TABLE_USERS +
                    " ADD COLUMN " + COL_LAST_NAME + " TEXT");

            db.execSQL("ALTER TABLE " + TABLE_USERS +
                    " ADD COLUMN " + COL_MOBILE + " TEXT");
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_USERS +
                    " ADD COLUMN " + COL_DATE_OF_BIRTH + " TEXT");
        }
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

    public long registerUser(String firstName, String lastName,
                             String email, String mobile,
                             String dateOfBirth, String password) {

        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_USERNAME, firstName.trim() + " " + lastName.trim());
        values.put(COL_FIRST_NAME, firstName.trim());
        values.put(COL_LAST_NAME, lastName.trim());
        values.put(COL_MOBILE, mobile.trim());
        values.put(COL_EMAIL, email.trim().toLowerCase(java.util.Locale.ROOT));

        // Save birthday as YYYY-MM-DD in SQLite
        String[] dateParts = dateOfBirth.split("/");

        String databaseDate = dateParts[2] + "-" +
                dateParts[1] + "-" + dateParts[0];

        values.put(COL_DATE_OF_BIRTH, databaseDate);

        values.put(COL_PASSWORD_HASH, passwordHash);
        values.put(COL_PASSWORD_SALT, salt);

        return db.insert(TABLE_USERS, null, values);
    }

    public Cursor getUserByEmail(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{
                        email.trim().toLowerCase(java.util.Locale.ROOT)
                },
                null,
                null,
                null
        );

    }
    public boolean verifyUser(String email, String password) {

        try (Cursor cursor = getUserByEmail(email)) {

            if (!cursor.moveToFirst()) {
                return false;
            }

            String storedHash = cursor.getString(
                    cursor.getColumnIndexOrThrow(COL_PASSWORD_HASH)
            );

            String storedSalt = cursor.getString(
                    cursor.getColumnIndexOrThrow(COL_PASSWORD_SALT)
            );

            return PasswordUtils.verifyPassword(
                    password,
                    storedSalt,
                    storedHash
            );
        }
    }

    public boolean validateUser(String email, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        String normalizedEmail =
                email.trim().toLowerCase(java.util.Locale.ROOT);

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_PASSWORD_HASH, COL_PASSWORD_SALT},
                COL_EMAIL + " = ?",
                new String[]{normalizedEmail},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {

                String storedHash = cursor.getString(0);
                String storedSalt = cursor.getString(1);

                String enteredHash =
                        PasswordUtils.hashPassword(password, storedSalt);

                return java.security.MessageDigest.isEqual(
                        storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        enteredHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
            }

            return false;

        } finally {
            cursor.close();
        }
    }

}

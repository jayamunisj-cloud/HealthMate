package com.healthmate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.healthmate.PasswordUtils;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "healthmate.db";
    private static final int DATABASE_VERSION = 5;

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
    private final Context context;

    public static final String COL_ID = "id";
    public static final String COL_DATE = "date";
    public static final String COL_STEPS = "steps";
    public static final String COL_CALORIES = "calories";
    public static final String COL_WATER = "water";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    // Create the database table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_STEPS + " INTEGER NOT NULL, " +
                COL_CALORIES + " INTEGER NOT NULL, " +
                COL_WATER + " INTEGER NOT NULL, " +
                "user_id INTEGER)";

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

        if (oldVersion < 5) {
            db.execSQL(
                    "ALTER TABLE " + TABLE_NAME +
                            " ADD COLUMN user_id INTEGER"
            );
        }

    }

    // CREATE: Add a new health record

    public long addRecord(String date, int steps,
                          int calories, int water) {

        long userId = getCurrentUserId();

        // Never save a record without a valid user
        if (userId == -1) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);
        values.put(COL_USER_ID, userId);

        return db.insert(TABLE_NAME, null, values);
    }


    // READ: Retrieve all health records

    public Cursor getAllRecords() {

        // Get the signed-in user's email
        long userId = getCurrentUserId();

        SQLiteDatabase db = this.getReadableDatabase();

        // Return no records if there is no valid user
        if (userId == -1) {
            return db.query(
                    TABLE_NAME,
                    null,
                    "1=0",
                    null,
                    null,
                    null,
                    null
            );
        }

        // Retrieve only this user's records
        return db.query(
                TABLE_NAME,
                null,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COL_DATE + " DESC, " + COL_ID + " DESC"
        );
    }


    // UPDATE: Modify an existing health record

    public int updateRecord(int id, String date,
                            int steps, int calories, int water) {

        long userId = getCurrentUserId();

        if (userId == -1) {
            return 0;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);

        // Update only when the record belongs to this user
        return db.update(
                TABLE_NAME,
                values,
                COL_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{
                        String.valueOf(id),
                        String.valueOf(userId)
                }
        );
    }

    // DELETE: Remove a health record

    public int deleteRecord(int id) {

        // Get the currently signed-in user's email
        long userId = getCurrentUserId();

        if (userId == -1) {
            return 0;
        }

        SQLiteDatabase db = this.getWritableDatabase();


        return db.delete(
                TABLE_NAME,
                COL_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{
                        String.valueOf(id),
                        String.valueOf(userId)
                }
        );
    }


    // SEARCH: Retrieve records for a selected date

    public Cursor getRecordsByDate(String date) {

        // Get the currently signed-in user's email
        long userId = getCurrentUserId();

        SQLiteDatabase db = this.getReadableDatabase();

        // Return no records if the user is not signed in
        if (userId == -1) {
            return db.query(
                    TABLE_NAME,
                    null,
                    "1=0",
                    null,
                    null,
                    null,
                    null
            );
        }

        // Search only records owned by the signed-in user
        return db.query(
                TABLE_NAME,
                null,
                COL_DATE + "=? AND " + COL_USER_ID + "=?",
                new String[]{
                        date,
                        String.valueOf(userId)
                },
                null,
                null,
                COL_ID + " DESC"
        );
    }

    public Cursor getRecordById(int id) {

        long userId = getCurrentUserId();

        SQLiteDatabase db = this.getReadableDatabase();

        // Do not return records without a valid signed-in user.
        if (userId == -1) {
            return db.query(
                    TABLE_NAME,
                    null,
                    "1=0",
                    null,
                    null,
                    null,
                    null
            );
        }

        // Match both the record ID and its owner.
        return db.query(
                TABLE_NAME,
                null,
                COL_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{
                        String.valueOf(id),
                        String.valueOf(userId)
                },
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

    public int updateUserProfile(
            String email,
            String firstName,
            String lastName,
            String mobile,
            String dateOfBirth) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_FIRST_NAME, firstName.trim());
        values.put(COL_LAST_NAME, lastName.trim());
        values.put(
                COL_USERNAME,
                firstName.trim() + " " + lastName.trim()
        );
        values.put(COL_MOBILE, mobile.trim());
        values.put(COL_DATE_OF_BIRTH, dateOfBirth);

        return db.update(
                TABLE_USERS,
                values,
                COL_EMAIL + "=?",
                new String[]{
                        email.trim().toLowerCase(java.util.Locale.ROOT)
                }
        );
    }

    public boolean changePassword(
            String email,
            String currentPassword,
            String newPassword) {

        if (email == null || email.trim().isEmpty()
                || currentPassword == null
                || newPassword == null
                || newPassword.length() < 12
                || currentPassword.equals(newPassword)) {
            return false;
        }

        String normalizedEmail =
                email.trim().toLowerCase(java.util.Locale.ROOT);

        // Verify the user's current password
        if (!validateUser(normalizedEmail, currentPassword)) {
            return false;
        }

        // Generate a new salt and password hash
        String newSalt = PasswordUtils.generateSalt();

        String newPasswordHash =
                PasswordUtils.hashPassword(newPassword, newSalt);

        // Store only the hash and salt, never the password
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_PASSWORD_HASH, newPasswordHash);
        values.put(COL_PASSWORD_SALT, newSalt);

        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                COL_EMAIL + "=?",
                new String[]{normalizedEmail}
        );

        return rowsUpdated > 0;
    }
    private long getCurrentUserId() {

        android.content.SharedPreferences session =
                context.getSharedPreferences(
                        "HealthMateSession",
                        Context.MODE_PRIVATE
                );

        boolean isLoggedIn =
                session.getBoolean("isLoggedIn", false);

        String email =
                session.getString("userEmail", null);

        // Never allow record access without a valid session
        if (!isLoggedIn || email == null || email.trim().isEmpty()) {
            return -1;
        }

        // Confirm the account exists in SQLite
        return getUserIdByEmail(email);
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

    public long getUserIdByEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_EMAIL + "=?",
                new String[]{
                        email.trim().toLowerCase(java.util.Locale.ROOT)
                },
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        }

        return -1;
    }


}

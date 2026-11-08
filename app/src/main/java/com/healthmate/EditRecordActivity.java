package com.healthmate;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import com.healthmate.database.DatabaseHelper;
import android.app.DatePickerDialog;
import java.util.Calendar;
import android.content.Intent;


public class EditRecordActivity extends AppCompatActivity {

    private EditText etEditDate;
    private EditText etEditSteps;
    private EditText etEditCalories;
    private EditText etEditWater;

    private Button btnUpdateRecord;

    private DatabaseHelper databaseHelper;
    private int recordId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isLoggedIn = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getBoolean("isLoggedIn", false);

        String userEmail = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getString("userEmail", "");

        if (!isLoggedIn || userEmail == null || userEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Please sign in to edit health records",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    EditRecordActivity.this,
                    SignInActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_record);
        databaseHelper = new DatabaseHelper(this);

        etEditDate = findViewById(R.id.etEditDate);
        etEditDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            String currentDate = etEditDate.getText().toString().trim();

            if (currentDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = currentDate.split("-");
                calendar.set(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2])
                );
            }

            DatePickerDialog datePicker = new DatePickerDialog(
                    EditRecordActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                dayOfMonth
                        );

                        etEditDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });

        etEditSteps = findViewById(R.id.etEditSteps);

        etEditCalories = findViewById(R.id.etEditCalories);

        etEditWater = findViewById(R.id.etEditWater);

        btnUpdateRecord = findViewById(R.id.btnUpdateRecord);

        recordId = getIntent().getIntExtra("record_id", -1);

        if (recordId == -1) {
            Toast.makeText(
                    this,
                    "Invalid health record",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }
        loadRecord();


        btnUpdateRecord.setOnClickListener(v -> {
            updateRecord();
        });

    }
    private void updateRecord() {

        String date = etEditDate.getText().toString().trim();
        String stepsText = etEditSteps.getText().toString().trim();
        String caloriesText = etEditCalories.getText().toString().trim();
        String waterText = etEditWater.getText().toString().trim();


        if (date.isEmpty() || stepsText.isEmpty() ||
                caloriesText.isEmpty() || waterText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validate date format
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        format.setLenient(false);

        try {
            Date parsedDate = format.parse(date);

            if (parsedDate == null ||
                    !format.format(parsedDate).equals(date)) {

                etEditDate.setError("Use YYYY-MM-DD");
                return;
            }

        } catch (ParseException e) {

            etEditDate.setError("Use YYYY-MM-DD");
            return;
        }

        int steps;
        int calories;
        int water;

        try {
            steps = Integer.parseInt(stepsText);
            calories = Integer.parseInt(caloriesText);
            water = Integer.parseInt(waterText);


        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Please enter valid numbers",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validate negative values
        if (steps < 0 || calories < 0 || water < 0) {

            Toast.makeText(
                    this,
                    "Values cannot be negative",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }
        // Validate maximum allowed values
        if (steps > 50000 || calories > 10000 || water > 10000) {
            Toast.makeText(
                    this,
                    "Steps max: 50,000 | Calories max: 10,000 | Water max: 10,000 ml",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
        int result = databaseHelper.updateRecord(
                recordId,
                date,
                steps,
                calories,
                water
        );

        if (result > 0) {
            Toast.makeText(
                    this,
                    "Health record updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {
            Toast.makeText(
                    this,
                    "Failed to update health record",
                    Toast.LENGTH_SHORT
            ).show();
        }

    }

    private void loadRecord() {

        try (Cursor cursor = databaseHelper.getRecordById(recordId)) {

            if (cursor.moveToFirst()) {

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)
                );

                int steps = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STEPS)
                );

                int calories = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALORIES)
                );

                int water = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER)
                );

                etEditDate.setText(date);
                etEditSteps.setText(String.valueOf(steps));
                etEditCalories.setText(String.valueOf(calories));
                etEditWater.setText(String.valueOf(water));

            } else {
                Toast.makeText(this,
                        "Health record not found",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to load health record",
                    Toast.LENGTH_SHORT).show();
        }


    }
    @Override
    protected void onResume() {
        super.onResume();

        boolean isLoggedIn = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getBoolean("isLoggedIn", false);

        String userEmail = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getString("userEmail", "");

        if (!isLoggedIn || userEmail == null || userEmail.trim().isEmpty()) {

            Intent intent = new Intent(
                    EditRecordActivity.this,
                    SignInActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }
    }

}
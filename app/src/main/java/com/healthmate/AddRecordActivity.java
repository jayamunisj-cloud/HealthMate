package com.healthmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.healthmate.database.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private EditText etDate, etSteps, etCalories, etWater;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_record);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            bars.left, bars.top,
                            bars.right, bars.bottom
                    );
                    return insets;
                }

        );

        etDate = findViewById(R.id.etDate);
        etDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    AddRecordActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        String selectedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                selectedYear,
                                selectedMonth + 1,
                                selectedDay
                        );

                        etDate.setText(selectedDate);
                    },
                    year,
                    month,
                    day
            );

            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });
        etSteps = findViewById(R.id.etSteps);
        etCalories = findViewById(R.id.etCalories);
        etWater = findViewById(R.id.etWater);

        Button btnSaveRecord = findViewById(R.id.btnSaveRecord);

        databaseHelper = new DatabaseHelper(this);

        btnSaveRecord.setOnClickListener(v -> {
            if (validateHealthRecord()) {
                saveRecord();
            }
        });
    }

    private boolean validateHealthRecord() {

        String date = etDate.getText().toString().trim();
        String stepsText = etSteps.getText().toString().trim();
        String caloriesText = etCalories.getText().toString().trim();
        String waterText = etWater.getText().toString().trim();
        boolean isValid = true;

        // Date is required
        if (date.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select a date",
                    Toast.LENGTH_LONG
            ).show();
            isValid = false;
        }

        // Check date format and calendar validity
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd", java.util.Locale.US);
        format.setLenient(false);

        if (!date.isEmpty()) {
            try {
                java.util.Date selectedDate = format.parse(date);

                if (selectedDate == null ||
                        !format.format(selectedDate).equals(date)) {
                    etDate.setError("Invalid date");
                    isValid = false;
                }

                java.util.Calendar minimum =
                        java.util.Calendar.getInstance();
                minimum.clear();
                minimum.set(2000, java.util.Calendar.JANUARY, 1);

                java.util.Calendar tomorrow =
                        java.util.Calendar.getInstance();
                tomorrow.add(java.util.Calendar.DAY_OF_YEAR, 1);
                tomorrow.set(java.util.Calendar.HOUR_OF_DAY, 0);
                tomorrow.set(java.util.Calendar.MINUTE, 0);
                tomorrow.set(java.util.Calendar.SECOND, 0);
                tomorrow.set(java.util.Calendar.MILLISECOND, 0);

                if (selectedDate.before(minimum.getTime()) ||
                        !selectedDate.before(tomorrow.getTime())) {
                    etDate.setError("Select a date from 2000-01-01 to today");
                    isValid = false;
                }

            } catch (java.text.ParseException e) {
                etDate.setError("Please select a valid date");
                isValid = false;
            }
        }


        // Steps validation
        if (!validNumber(stepsText, 50000)) {
            etSteps.setError("Steps must be between 0 and 50,000");
            isValid = false;
        }

        // Calories validation
        if (!validNumber(caloriesText, 10000)) {
            etCalories.setError("Calories must be between 0 and 10,000");
            isValid = false;
        }

        // Water validation
        if (!validNumber(waterText, 10000)) {
            etWater.setError("Water must be between 0 and 10,000 ml");
            isValid = false;
        }

        if (!isValid) {
            StringBuilder errors = new StringBuilder();

            if (etDate.getError() != null) {
                errors.append("Date: Please select a valid date\n\n");
            }

            if (etSteps.getError() != null) {
                errors.append("Steps must be between 0 and 50,000\n\n");
            }

            if (etCalories.getError() != null) {
                errors.append("Calories must be between 0 and 10,000\n\n");
            }

            if (etWater.getError() != null) {
                errors.append("Water must be between 0 and 10,000 ml\n\n");
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Invalid Health Record")
                    .setMessage("Please correct the following errors:\n\n" + errors)
                    .setPositiveButton("OK", null)
                    .show();
        }

        return isValid;
    }

    private boolean validNumber(String value, int maximum) {
        if (value.isEmpty() || !value.matches("[0-9]+")) {
            return false;
        }

        try {
            long number = Long.parseLong(value);
            return number <= maximum;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void saveRecord() {

        String date = etDate.getText().toString().trim();
        String stepsText = etSteps.getText().toString().trim();
        String caloriesText = etCalories.getText().toString().trim();
        String waterText = etWater.getText().toString().trim();

        if (date.isEmpty() || stepsText.isEmpty()
                || caloriesText.isEmpty() || waterText.isEmpty()) {

            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate the date format
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);

        try {
            if (format.parse(date) == null ||
                    !format.format(format.parse(date)).equals(date)) {
                etDate.setError("Use YYYY-MM-DD");
                return;
            }
        } catch (ParseException e) {
            etDate.setError("Use YYYY-MM-DD");
            return;
        }

        try {
            int steps = Integer.parseInt(stepsText);
            int calories = Integer.parseInt(caloriesText);
            int water = Integer.parseInt(waterText);

            if (steps < 0 || calories < 0 || water < 0) {
                Toast.makeText(this,
                        "Values cannot be negative",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            long result = databaseHelper.addRecord(
                    date, steps, calories, water
            );

            if (result != -1) {
                Toast.makeText(this,
                        "Health record saved successfully!",
                        Toast.LENGTH_LONG).show();

                finish();
            } else {
                Toast.makeText(this,
                        "Failed to save record",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Please enter valid numbers",
                    Toast.LENGTH_SHORT).show();
        } catch (android.database.SQLException e) {
            Toast.makeText(this,
                    "Database error while saving",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}

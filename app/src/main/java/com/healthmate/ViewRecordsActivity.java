package com.healthmate;

import android.database.Cursor;
import android.os.Bundle;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;
import android.app.DatePickerDialog;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

import com.healthmate.database.DatabaseHelper;

public class ViewRecordsActivity extends AppCompatActivity {

    private LinearLayout recordsContainer;
    private DatabaseHelper databaseHelper;
    private String selectedSearchDate = null;

    private EditText etSearchDate;
    private Button btnSearchRecords;
    private Button btnShowAllRecords;


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
                    "Please sign in to view health records",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    ViewRecordsActivity.this,
                    SignInActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_view_records);

        recordsContainer =
                findViewById(R.id.recordsContainer);

        databaseHelper = new DatabaseHelper(this);

        // Initialize Search by Date controls
        etSearchDate = findViewById(R.id.etSearchDate);
        etSearchDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    ViewRecordsActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        String selectedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                selectedYear,
                                selectedMonth + 1,
                                selectedDay
                        );

                        etSearchDate.setText(selectedDate);
                    },
                    year,
                    month,
                    day
            );

            datePicker.show();
        });
        btnSearchRecords = findViewById(R.id.btnSearchRecords);
        btnShowAllRecords = findViewById(R.id.btnShowAllRecords);
        btnShowAllRecords.setOnClickListener(v -> {
            selectedSearchDate = null;
            etSearchDate.setText("");
            displayRecords();
        });

        // Search Records button click
        btnSearchRecords.setOnClickListener(v -> {

            String searchDate = etSearchDate.getText()
                    .toString()
                    .trim();

            if (searchDate.isEmpty()) {
                etSearchDate.setError("Please enter a date");
                return;
            }
            if (!searchDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                Toast.makeText(this,
                        "Please enter a valid date (YYYY-MM-DD)",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            selectedSearchDate = searchDate;
            displayRecords();
            try {
                SimpleDateFormat dateFormat =
                        new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                dateFormat.setLenient(false);
                dateFormat.parse(searchDate);

            } catch (ParseException e) {
                Toast.makeText(this,
                        "Please enter a real calendar date",
                        Toast.LENGTH_SHORT).show();
                return;
            }

        });
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
                    ViewRecordsActivity.this,
                    SignInActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }

        // Refresh only the signed-in user's records
        displayRecords();
    }
    private void displayRecords() {

        recordsContainer.removeAllViews();

        try (Cursor cursor = selectedSearchDate == null
                ? databaseHelper.getAllRecords()
                : databaseHelper.getRecordsByDate(selectedSearchDate)) {

            if (cursor.getCount() == 0) {
                TextView emptyMessage = new TextView(this);
                emptyMessage.setText(selectedSearchDate == null
                        ? "No health records saved yet."
                        : "No health records found for " + selectedSearchDate);
                emptyMessage.setTextSize(18);
                recordsContainer.addView(emptyMessage);
                return;
            }


            int idIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_ID);
            int dateIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_DATE);
            int stepsIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_STEPS);
            int caloriesIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_CALORIES);
            int waterIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_WATER);

            while (cursor.moveToNext()) {


                int recordId = cursor.getInt(idIndex);

                String date = cursor.getString(dateIndex);
                int steps = cursor.getInt(stepsIndex);
                int calories = cursor.getInt(caloriesIndex);
                int water = cursor.getInt(waterIndex);

                TextView recordView = new TextView(this);

                recordView.setText(
                        "Date: " + date +
                                "\nSteps Walked: " + steps +
                                "\nCalories Burned: " + calories + " kcal" +
                                "\nWater Intake: " + water + " ml"
                );

                recordView.setTextSize(16);
                recordView.setTextColor(
                        Color.rgb(38, 50, 56)
                );

                recordView.setPadding(20, 20, 20, 20);
                recordView.setBackgroundColor(Color.WHITE);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(0, 0, 0, 20);

                recordsContainer.addView(recordView, params);

                Button btnEdit = new Button(this);
                btnEdit.setText("Edit Record");


                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(
                            ViewRecordsActivity.this,
                            EditRecordActivity.class
                    );

                    intent.putExtra("record_id", recordId);

                    startActivity(intent);

                });
                Button btnDelete = new Button(this);
                btnDelete.setText("DELETE RECORD");

                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Health Record")
                            .setMessage("Are you sure you want to delete this record?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                int result = databaseHelper.deleteRecord(recordId);

                                if (result > 0) {
                                    Toast.makeText(this,
                                            "Health record deleted successfully",
                                            Toast.LENGTH_SHORT).show();

                                    displayRecords();
                                } else {
                                    Toast.makeText(this,
                                            "Failed to delete record",
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                recordsContainer.addView(btnEdit);

                recordsContainer.addView(btnDelete);

            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Unable to load health records",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    private void displayRecordsByDate(String date) {

        try (Cursor cursor = databaseHelper.getRecordsByDate(date)) {

            if (cursor == null) {
                return;
            }

            recordsContainer.removeAllViews();

            if (cursor.getCount() == 0) {
                TextView emptyMessage = new TextView(this);
                emptyMessage.setText("No health records found for " + date);
                emptyMessage.setTextSize(18);
                recordsContainer.addView(emptyMessage);
                return;
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Unable to search health records",
                    Toast.LENGTH_SHORT
            ).show();
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

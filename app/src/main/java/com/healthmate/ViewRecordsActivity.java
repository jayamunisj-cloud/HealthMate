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
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.healthmate.database.DatabaseHelper;

public class ViewRecordsActivity extends AppCompatActivity {

    private LinearLayout recordsContainer;
    private DatabaseHelper databaseHelper;


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
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        recordsContainer =
                findViewById(R.id.recordsContainer);

        databaseHelper = new DatabaseHelper(this);
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

        try (Cursor cursor = databaseHelper.getAllRecords()) {

            if (cursor.getCount() == 0) {
                TextView emptyMessage = new TextView(this);
                emptyMessage.setText("No health records saved yet.");
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

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}

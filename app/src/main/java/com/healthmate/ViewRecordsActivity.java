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
import android.widget.ImageButton;
import android.graphics.drawable.GradientDrawable;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import com.healthmate.database.DatabaseHelper;
import android.view.LayoutInflater;
import java.text.NumberFormat;

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

                android.view.View recordCard = LayoutInflater.from(this)
                        .inflate(R.layout.item_health_record, recordsContainer, false);

                TextView tvDate = recordCard.findViewById(R.id.tvRecordDate);
                TextView tvSteps = recordCard.findViewById(R.id.tvRecordSteps);
                TextView tvCalories = recordCard.findViewById(R.id.tvRecordCalories);
                TextView tvWater = recordCard.findViewById(R.id.tvRecordWater);

                String displayDate = date;

                try {
                    SimpleDateFormat inputFormat =
                            new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                    inputFormat.setLenient(false);

                    java.util.Date parsedDate = inputFormat.parse(date);

                    if (parsedDate != null) {
                        SimpleDateFormat outputFormat =
                                new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);

                        displayDate = outputFormat.format(parsedDate);
                    }
                } catch (ParseException e) {
                    // Keep the original date if formatting fails
                }

                tvDate.setText(displayDate);

// Display actual SQLite values
                NumberFormat numberFormat = NumberFormat.getIntegerInstance();

                tvSteps.setText(numberFormat.format(steps));

                tvCalories.setText(
                        numberFormat.format(calories) + " kcal"
                );

                tvWater.setText(
                        numberFormat.format(water) + " ml"
                );

// Connect Edit and Delete buttons
                Button btnEdit = recordCard.findViewById(R.id.btnRecordEdit);
                Button btnDelete = recordCard.findViewById(R.id.btnRecordDelete);

// Keep Edit functionality
                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(
                            ViewRecordsActivity.this,
                            EditRecordActivity.class
                    );

                    intent.putExtra("record_id", recordId);
                    startActivity(intent);
                });

// Keep Delete confirmation and functionality
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Health Record")
                            .setMessage("Are you sure you want to delete this record?")
                            .setPositiveButton("Delete", (dialog, which) -> {

                                int result = databaseHelper.deleteRecord(recordId);

                                if (result > 0) {
                                    Toast.makeText(
                                            this,
                                            "Health record deleted successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    displayRecords();
                                } else {
                                    Toast.makeText(
                                            this,
                                            "Failed to delete record",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                recordsContainer.addView(recordCard);

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

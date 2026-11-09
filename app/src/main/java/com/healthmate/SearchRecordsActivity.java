package com.healthmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.EditText;
import java.util.Calendar;
import java.util.Locale;
import android.database.Cursor;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.healthmate.database.DatabaseHelper;
import android.widget.ImageButton;
import android.view.LayoutInflater;
import android.view.View;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import androidx.appcompat.app.AppCompatActivity;

public class SearchRecordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences session = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        );

        boolean isLoggedIn = session.getBoolean(
                "isLoggedIn",
                false
        );

        String userEmail = session.getString(
                "userEmail",
                ""
        );

        if (!isLoggedIn ||
                userEmail == null ||
                userEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Please sign in to search health records",
                    Toast.LENGTH_SHORT
            ).show();

            startActivity(new Intent(
                    SearchRecordsActivity.this,
                    SignInActivity.class
            ));

            finish();
            return;
        }

        setContentView(R.layout.activity_search_records);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        EditText etSearchDate = findViewById(R.id.etSearchDate);

        Button btnShowAllRecords = findViewById(R.id.btnShowAllRecords);

        btnShowAllRecords.setOnClickListener(v -> {
            etSearchDate.setText("");
            displayAllRecords();
        });

        etSearchDate.setOnClickListener(v -> {

            Calendar today = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    SearchRecordsActivity.this,
                    (view, year, month, dayOfMonth) -> {

                        String selectedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                dayOfMonth
                        );

                        etSearchDate.setText(selectedDate);
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );

            // Prevent selecting future dates
            datePicker.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            datePicker.show();
        });
        Button btnSearchRecords = findViewById(R.id.btnSearchRecords);

        LinearLayout searchResultsContainer =
                findViewById(R.id.searchResultsContainer);

        btnSearchRecords.setOnClickListener(v -> {

            String selectedDate = etSearchDate.getText()
                    .toString()
                    .trim();

            if (selectedDate.isEmpty()) {
                Toast.makeText(
                        this,
                        "Please select a date",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            searchResultsContainer.removeAllViews();

            DatabaseHelper databaseHelper = new DatabaseHelper(this);

            try (Cursor cursor =
                         databaseHelper.getRecordsByDate(selectedDate)) {

                if (cursor.getCount() == 0) {

                    TextView emptyMessage = new TextView(this);
                    emptyMessage.setText(
                            "No health records found for " + selectedDate
                    );
                    emptyMessage.setTextSize(18);
                    emptyMessage.setPadding(16, 24, 16, 24);

                    searchResultsContainer.addView(emptyMessage);
                    return;
                }

                while (cursor.moveToNext()) {

                    String date = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_DATE
                            )
                    );

                    int steps = cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_STEPS
                            )
                    );

                    int calories = cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_CALORIES
                            )
                    );

                    int water = cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_WATER
                            )
                    );

                    addSearchRecordCard(
                            searchResultsContainer,
                            date,
                            steps,
                            calories,
                            water
                    );
                }

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Unable to search health records",
                        Toast.LENGTH_SHORT
                ).show();

            } finally {
                databaseHelper.close();

            }
        });
    }

    private void displayAllRecords() {

        LinearLayout resultsContainer =
                findViewById(R.id.searchResultsContainer);

        resultsContainer.removeAllViews();

        DatabaseHelper db = new DatabaseHelper(this);

        try (Cursor cursor = db.getAllRecords()) {

            if (cursor.getCount() == 0) {
                TextView emptyMessage = new TextView(this);
                emptyMessage.setText("No health records saved yet.");
                emptyMessage.setTextSize(16);
                resultsContainer.addView(emptyMessage);
                return;
            }

            int dateIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_DATE);

            int stepsIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_STEPS);

            int caloriesIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_CALORIES);

            int waterIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_WATER);

            while (cursor.moveToNext()) {

                String date = cursor.getString(dateIndex);
                int steps = cursor.getInt(stepsIndex);
                int calories = cursor.getInt(caloriesIndex);
                int water = cursor.getInt(waterIndex);

                addSearchRecordCard(
                        resultsContainer,
                        date,
                        steps,
                        calories,
                        water
                );
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to load health records",
                    Toast.LENGTH_SHORT
            ).show();

        } finally {
            db.close();
        }
    }
    private void addSearchRecordCard(
            LinearLayout container,
            String date,
            int steps,
            int calories,
            int water) {

        View recordCard = LayoutInflater.from(this)
                .inflate(R.layout.item_search_record, container, false);

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
            // Keep original date if formatting fails.
        }

        tvDate.setText(displayDate);

        NumberFormat numberFormat = NumberFormat.getIntegerInstance();

        tvSteps.setText(numberFormat.format(steps));
        tvCalories.setText(numberFormat.format(calories) + " kcal");
        tvWater.setText(numberFormat.format(water) + " ml");

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 16);

        container.addView(recordCard, params);
    }
}
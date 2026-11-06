package com.healthmate;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;
import com.healthmate.database.DatabaseHelper;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView txtSteps;
    private TextView txtCalories;
    private TextView txtWater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        // Connect dashboard TextViews
        txtSteps = findViewById(R.id.txtSteps);
        txtCalories = findViewById(R.id.txtCalories);
        txtWater = findViewById(R.id.txtWater);

        // Open the Add Health Record Screen
        Button btnAddRecord = findViewById(R.id.btnAddRecord);

        btnAddRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRecordActivity.class);
            startActivity(intent);
        });
        // Open View Health Records screen
        Button btnViewRecords = findViewById(R.id.btnViewRecords);

        btnViewRecords.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ViewRecordsActivity.class
            );
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
    private void loadDashboardData() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        try (Cursor cursor = databaseHelper.getRecordsByDate(today)) {

            int totalSteps = 0;
            int totalCalories = 0;
            int totalWater = 0;

            if (cursor != null) {

                while (cursor.moveToNext()) {

                    int steps = cursor.getInt(
                            cursor.getColumnIndexOrThrow("steps"));

                    int calories = cursor.getInt(
                            cursor.getColumnIndexOrThrow("calories"));

                    int water = cursor.getInt(
                            cursor.getColumnIndexOrThrow("water"));

                    totalSteps += steps;
                    totalCalories += calories;
                    totalWater += water;
                }
            }

            txtSteps.setText(
                    "Steps Walked: " + totalSteps);

            txtCalories.setText(
                    "Calories Burned: " + totalCalories + " kcal");

            txtWater.setText(
                    "Water Intake: " + totalWater + " ml");

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to load dashboard data",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
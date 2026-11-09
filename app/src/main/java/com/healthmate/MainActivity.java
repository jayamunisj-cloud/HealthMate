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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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

        TextView tvDashboardDate = findViewById(R.id.tvDashboardDate);

        String currentDate = new SimpleDateFormat(
                "EEE, MMM d, yyyy",
                Locale.getDefault()
        ).format(new Date());

        tvDashboardDate.setText(currentDate);


        findViewById(R.id.btnProfile).setOnClickListener(v -> {

            boolean isLoggedIn = getSharedPreferences(
                    "HealthMateSession",
                    MODE_PRIVATE
            ).getBoolean("isLoggedIn", false);


            if (isLoggedIn) {
                Intent intent = new Intent(
                        MainActivity.this,
                        ProfileActivity.class
                );
                startActivity(intent);

        } else {
                Intent intent = new Intent(
                        MainActivity.this,
                        SignInActivity.class
                );
                startActivity(intent);
            }
        });


        databaseHelper = new DatabaseHelper(this);

        // Connect dashboard TextViews
        txtSteps = findViewById(R.id.txtSteps);
        txtCalories = findViewById(R.id.txtCalories);
        txtWater = findViewById(R.id.txtWater);

        // Open the Add Health Record Screen
        LinearLayout btnAddRecord = findViewById(R.id.btnAddRecord);

        btnAddRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRecordActivity.class);
            startActivity(intent);
        });
        // Open View Health Records screen
        LinearLayout btnViewRecords = findViewById(R.id.btnViewRecords);

        btnViewRecords.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ViewRecordsActivity.class
            );
            startActivity(intent);
        });
        findViewById(R.id.btnNavRecords).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ViewRecordsActivity.class
            );
            startActivity(intent);
        });
        findViewById(R.id.btnSearchRecords).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SearchRecordsActivity.class
            );
            startActivity(intent);
        });

        findViewById(R.id.btnHealthInsights).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    InsightsActivity.class
            );
            startActivity(intent);
        });

        findViewById(R.id.btnNavInsights).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    InsightsActivity.class
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
        loadDashboardGreeting();
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

            txtSteps.setText(String.valueOf(totalSteps));
            txtCalories.setText(String.valueOf(totalCalories));
            txtWater.setText(totalWater + " ml");

            int stepsPercent = Math.min(100, Math.round(totalSteps * 100f / 8000));
            int caloriesPercent = Math.min(100, Math.round(totalCalories * 100f / 400));
            int waterPercent = Math.min(100, Math.round(totalWater * 100f / 2000));

            ProgressBar stepsProgress = findViewById(R.id.progressDashboardSteps);
            ProgressBar caloriesProgress = findViewById(R.id.progressDashboardCalories);
            ProgressBar waterProgress = findViewById(R.id.progressDashboardWater);

            stepsProgress.setProgress(stepsPercent);
            caloriesProgress.setProgress(caloriesPercent);
            waterProgress.setProgress(waterPercent);

            TextView stepsPercentText = findViewById(R.id.tvDashboardStepsPercent);
            TextView caloriesPercentText = findViewById(R.id.tvDashboardCaloriesPercent);
            TextView waterPercentText = findViewById(R.id.tvDashboardWaterPercent);

            stepsPercentText.setText(stepsPercent + "%");
            caloriesPercentText.setText(caloriesPercent + "%");
            waterPercentText.setText(waterPercent + "%");


            TextView stepsGoalText = findViewById(R.id.tvDashboardStepsGoal);
            TextView caloriesGoalText = findViewById(R.id.tvDashboardCaloriesGoal);
            TextView waterGoalText = findViewById(R.id.tvDashboardWaterGoal);

            stepsGoalText.setText(totalSteps + " / 8000");
            caloriesGoalText.setText(totalCalories + " / 400");
            waterGoalText.setText(totalWater + " / 2000 ml");

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to load dashboard data",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadDashboardGreeting() {

        TextView greeting = findViewById(R.id.tvDashboardGreeting);

        boolean isLoggedIn = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getBoolean("isLoggedIn", false);

        String userEmail = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getString("userEmail", "");

        // Default greeting for guests
        greeting.setText("Hello, User!");

        if (!isLoggedIn || userEmail.isEmpty()) {
            return;
        }

        DatabaseHelper db = new DatabaseHelper(this);

        try (Cursor cursor = db.getUserByEmail(userEmail)) {

            if (cursor != null && cursor.moveToFirst()) {

                String firstName = cursor.getString(
                        cursor.getColumnIndexOrThrow("first_name")
                );

                if (firstName != null && !firstName.trim().isEmpty()) {
                    greeting.setText("Hello, " + firstName.trim() + "!");
                }
            }

        } finally {
            db.close();
        }
    }
}
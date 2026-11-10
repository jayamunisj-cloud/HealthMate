package com.healthmate;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.healthmate.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InsightsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private TextView tvSteps;
    private TextView tvCalories;
    private TextView tvWater;
    private TextView tvMessage;

    private ProgressBar progressSteps;
    private ProgressBar progressCalories;
    private ProgressBar progressWater;

    // Example daily goals
    private static final int STEPS_GOAL = 7500;
    private static final int CALORIES_GOAL = 1800;
    private static final int WATER_GOAL = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_insights);

        findViewById(R.id.btnInsightsBackTop).setOnClickListener(v -> {
            finish();
        });

        databaseHelper = new DatabaseHelper(this);

        // Connect summary fields
        tvSteps = findViewById(R.id.tvInsightsSteps);
        tvCalories = findViewById(R.id.tvInsightsCalories);
        tvWater = findViewById(R.id.tvInsightsWater);
        tvMessage = findViewById(R.id.tvInsightsMessage);

        // Connect progress bars
        progressSteps = findViewById(R.id.progressSteps);
        progressCalories = findViewById(R.id.progressCalories);
        progressWater = findViewById(R.id.progressWater);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInsights();
    }

    private void loadInsights() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        int totalSteps = 0;
        int totalCalories = 0;
        int totalWater = 0;

        try (Cursor cursor = databaseHelper.getRecordsByDate(today)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    totalSteps += cursor.getInt(
                            cursor.getColumnIndexOrThrow("steps")
                    );

                    totalCalories += cursor.getInt(
                            cursor.getColumnIndexOrThrow("calories")
                    );

                    totalWater += cursor.getInt(
                            cursor.getColumnIndexOrThrow("water")
                    );
                }
            }

        } catch (Exception e) {
            tvMessage.setText("Unable to load health insights.");
            return;
        }

        // Display actual SQLite totals
        tvSteps.setText(totalSteps + " steps");
        tvCalories.setText(totalCalories + " kcal");
        tvWater.setText(totalWater + " ml");

        // Update progress bars
        progressSteps.setProgress(
                calculateProgress(totalSteps, STEPS_GOAL)
        );

        progressCalories.setProgress(
                calculateProgress(totalCalories, CALORIES_GOAL)
        );

        progressWater.setProgress(
                calculateProgress(totalWater, WATER_GOAL)
        );

        // Daily summary message
        if (totalSteps == 0 && totalCalories == 0 && totalWater == 0) {
            tvMessage.setText(
                    "No health records found for today. Add a record to see your progress!"
            );
        } else {
            tvMessage.setText(
                    "Today's activity:\n\n"
                            + "Steps: " + totalSteps + " / " + STEPS_GOAL + "\n"
                            + "Calories: " + totalCalories + " / " + CALORIES_GOAL + " kcal\n"
                            + "Water: " + totalWater + " / " + WATER_GOAL + " ml"
            );
        }
    }

    private int calculateProgress(int value, int goal) {
        return (int) Math.min(
                100,
                (value * 100.0) / goal
        );
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}

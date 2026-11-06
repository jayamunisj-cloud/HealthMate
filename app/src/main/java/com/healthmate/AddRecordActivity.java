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
        etSteps = findViewById(R.id.etSteps);
        etCalories = findViewById(R.id.etCalories);
        etWater = findViewById(R.id.etWater);

        Button btnSaveRecord = findViewById(R.id.btnSaveRecord);

        databaseHelper = new DatabaseHelper(this);

        btnSaveRecord.setOnClickListener(v -> saveRecord());
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

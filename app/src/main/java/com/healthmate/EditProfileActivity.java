package com.healthmate;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.DatePickerDialog;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;
import android.database.Cursor;
import android.widget.EditText;
import com.healthmate.database.DatabaseHelper;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        EditText etEditFirstName = findViewById(R.id.etEditFirstName);
        EditText etEditLastName = findViewById(R.id.etEditLastName);
        EditText etEditMobile = findViewById(R.id.etEditMobile);

        String loggedInEmail = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getString("userEmail", "");

        if (!loggedInEmail.isEmpty()) {

            DatabaseHelper databaseHelper = new DatabaseHelper(this);

            try (Cursor cursor = databaseHelper.getUserByEmail(loggedInEmail)) {

                if (cursor.moveToFirst()) {

                    String firstName = cursor.getString(
                            cursor.getColumnIndexOrThrow("first_name")
                    );

                    String lastName = cursor.getString(
                            cursor.getColumnIndexOrThrow("last_name")
                    );

                    String mobile = cursor.getString(
                            cursor.getColumnIndexOrThrow("mobile")
                    );

                    String dob = cursor.getString(
                            cursor.getColumnIndexOrThrow("date_of_birth")
                    );

                    etEditFirstName.setText(firstName);
                    etEditLastName.setText(lastName);
                    etEditMobile.setText(mobile);

                    TextView dobField = findViewById(R.id.tvEditDob);

                    if (dob != null && !dob.isEmpty()) {
                        String[] parts = dob.split("-");

                        if (parts.length == 3) {
                            dobField.setText(
                                    parts[2] + "/" + parts[1] + "/" + parts[0]
                            );
                        }
                    }
                }

            } finally {
                databaseHelper.close();
            }
        }


        TextView tvCancelEdit = findViewById(R.id.tvCancelEdit);

        tvCancelEdit.setOnClickListener(v -> {
            finish();
        });

        TextView tvEditDob = findViewById(R.id.tvEditDob);

        tvEditDob.setOnClickListener(v -> {

            Calendar today = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    EditProfileActivity.this,
                    (view, year, month, dayOfMonth) -> {

                        String selectedDate = String.format(
                                Locale.US,
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        );

                        tvEditDob.setText(selectedDate);
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

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {

            String firstName = etEditFirstName.getText().toString().trim();
            String lastName = etEditLastName.getText().toString().trim();
            String mobile = etEditMobile.getText().toString().trim();
            String dob = tvEditDob.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this,
                        "Please enter your first and last name",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
                Toast.makeText(this,
                        "Please select a valid date of birth",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String[] parts = dob.split("/");
            String databaseDob =
                    parts[2] + "-" + parts[1] + "-" + parts[0];

            if (loggedInEmail.isEmpty()) {
                Toast.makeText(this,
                        "Please sign in again",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(this);

            int rowsUpdated;

            try {
                rowsUpdated = db.updateUserProfile(
                        loggedInEmail,
                        firstName,
                        lastName,
                        mobile,
                        databaseDob
                );
            } finally {
                db.close();
            }

            if (rowsUpdated > 0) {
                Toast.makeText(this,
                        "Profile updated successfully!",
                        Toast.LENGTH_SHORT).show();

                finish();
            } else {
                Toast.makeText(this,
                        "Unable to update profile",
                        Toast.LENGTH_SHORT).show();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

package com.healthmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.healthmate.database.DatabaseHelper;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileMobile;
    private TextView tvProfileDob;

    private Button btnLogout;
    private TextView tvBackToDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent guests from opening Profile
        boolean isLoggedIn = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            startActivity(new Intent(
                    ProfileActivity.this,
                    SignInActivity.class
            ));
            finish();
            return;
        }

        setContentView(R.layout.activity_profile);

        // Connect Profile information
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileMobile = findViewById(R.id.tvProfileMobile);
        tvProfileDob = findViewById(R.id.tvProfileDob);

        // Connect buttons
        btnLogout = findViewById(R.id.btnLogout);
        tvBackToDashboard = findViewById(
                R.id.tvBackToDashboard
        );

        Button btnEditProfile = findViewById(
                R.id.btnEditProfile
        );

        // Open Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ProfileActivity.this,
                    EditProfileActivity.class
            );
            startActivity(intent);
        });

        // Return to Dashboard
        tvBackToDashboard.setOnClickListener(v -> finish());

        // Log out
        btnLogout.setOnClickListener(v -> {

            getSharedPreferences(
                    "HealthMateSession",
                    MODE_PRIVATE
            ).edit()
                    .remove("isLoggedIn")
                    .remove("userEmail")
                    .apply();

            Intent intent = new Intent(
                    ProfileActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    // Refresh profile whenever this screen becomes visible
    @Override
    protected void onResume() {
        super.onResume();

        // Avoid loading data if the screen was closed
        // before the views were initialized
        if (tvProfileName != null) {
            loadProfileData();
        }
    }

    // Load latest user information from SQLite
    private void loadProfileData() {

        String userEmail = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getString("userEmail", "");

        if (userEmail.isEmpty()) {
            return;
        }

        DatabaseHelper databaseHelper =
                new DatabaseHelper(this);

        try (Cursor cursor =
                     databaseHelper.getUserByEmail(userEmail)) {

            if (cursor != null && cursor.moveToFirst()) {

                String firstName = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "first_name"
                        )
                );

                String lastName = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "last_name"
                        )
                );

                String email = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "email"
                        )
                );

                String mobile = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "mobile"
                        )
                );

                // Display full name
                String fullName =
                        (firstName == null ? "" : firstName)
                                + " "
                                + (lastName == null ? "" : lastName);

                tvProfileName.setText(fullName.trim());

                // Display email
                tvProfileEmail.setText(email);

                // Display mobile number
                if (mobile == null || mobile.trim().isEmpty()) {
                    tvProfileMobile.setText("Not provided");
                } else {
                    tvProfileMobile.setText(mobile);
                }

                // Display Date of Birth
                int dobIndex = cursor.getColumnIndex(
                        "date_of_birth"
                );

                if (dobIndex != -1) {

                    String dob = cursor.getString(dobIndex);

                    if (dob != null && !dob.trim().isEmpty()) {

                        // Convert YYYY-MM-DD to DD/MM/YYYY
                        String[] parts = dob.split("-");

                        if (parts.length == 3) {
                            String formattedDob =
                                    parts[2] + "/"
                                            + parts[1] + "/"
                                            + parts[0];

                            tvProfileDob.setText(formattedDob);
                        } else {
                            tvProfileDob.setText(dob);
                        }

                    } else {
                        tvProfileDob.setText("Not provided");
                    }

                } else {
                    tvProfileDob.setText("Not provided");
                }
            }

        } finally {
            databaseHelper.close();
        }
    }
}

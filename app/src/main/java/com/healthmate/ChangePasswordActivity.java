package com.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.healthmate.database.DatabaseHelper;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check whether the user is signed in
        boolean isLoggedIn = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            startActivity(new Intent(
                    this,
                    SignInActivity.class
            ));
            finish();
            return;
        }

        setContentView(R.layout.activity_change_password);

        EditText etCurrentPassword =
                findViewById(R.id.etCurrentPassword);

        EditText etNewPassword =
                findViewById(R.id.etNewPassword);

        EditText etConfirmPassword =
                findViewById(R.id.etConfirmPassword);

        Button btnChangePassword =
                findViewById(R.id.btnChangePassword);

        TextView tvCancelPassword =
                findViewById(R.id.tvCancelPassword);

        // Cancel button
        tvCancelPassword.setOnClickListener(v -> finish());

        // Update Password button
        btnChangePassword.setOnClickListener(v -> {

            String currentPassword =
                    etCurrentPassword.getText().toString();

            String newPassword =
                    etNewPassword.getText().toString();

            String confirmPassword =
                    etConfirmPassword.getText().toString();

            // Check required fields
            if (currentPassword.isEmpty()) {
                etCurrentPassword.setError(
                        "Enter your current password"
                );
                return;
            }

            if (newPassword.length() < 12) {
                etNewPassword.setError(
                        "Use at least 12 characters"
                );
                return;
            }

            if (newPassword.equals(currentPassword)) {
                etNewPassword.setError(
                        "Choose a different password"
                );
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError(
                        "Passwords do not match"
                );
                return;
            }

            // Get signed-in user's email
            String userEmail = getSharedPreferences(
                    "HealthMateSession",
                    MODE_PRIVATE
            ).getString("userEmail", "");

            if (userEmail.isEmpty()) {
                Toast.makeText(
                        this,
                        "Please sign in again",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Verify current password and save new hash
            DatabaseHelper databaseHelper =
                    new DatabaseHelper(this);

            boolean passwordChanged;

            try {
                passwordChanged = databaseHelper.changePassword(
                        userEmail,
                        currentPassword,
                        newPassword
                );
            } finally {
                databaseHelper.close();
            }

            if (passwordChanged) {

                Toast.makeText(
                        this,
                        "Password updated successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                etCurrentPassword.setError(
                        "Could not change password. Check your current password."
                );
            }
        });
    }
}

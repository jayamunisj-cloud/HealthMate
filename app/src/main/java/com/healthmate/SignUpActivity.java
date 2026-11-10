
package com.healthmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.healthmate.database.DatabaseHelper;

import java.util.Calendar;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etMobile;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private TextView etSignUpDob;
    private ImageView ivDobCalendar;

    private Button btnCreateAccount;
    private TextView tvGoToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Check whether user is already signed in
        boolean isLoggedIn = getSharedPreferences(
                "HealthMateSession",
                MODE_PRIVATE
        ).getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(
                    SignUpActivity.this,
                    MainActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }

        // Connect input fields
        etFirstName = findViewById(R.id.etSignUpFirstName);
        etLastName = findViewById(R.id.etSignUpLastName);
        etEmail = findViewById(R.id.etSignUpEmail);
        etMobile = findViewById(R.id.etSignUpMobile);
        etPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(
                R.id.etSignUpConfirmPassword
        );

        // Connect Date of Birth field
        etSignUpDob = findViewById(R.id.etSignUpDob);
        ivDobCalendar = findViewById(R.id.ivDobCalendar);

        // Connect buttons
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToSignIn = findViewById(R.id.tvGoToSignIn);

        // Open calendar when DOB field or icon is clicked
        View.OnClickListener openCalendar = v -> {

            Calendar today = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    SignUpActivity.this,
                    (view, year, month, dayOfMonth) -> {

                        String selectedDate = String.format(
                                Locale.US,
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        );

                        etSignUpDob.setText(selectedDate);
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );

            // Prevent future dates
            datePicker.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            datePicker.show();
        };

        etSignUpDob.setOnClickListener(openCalendar);
        ivDobCalendar.setOnClickListener(openCalendar);

        // Create account
        btnCreateAccount.setOnClickListener(
                v -> createAccount()
        );

        // Navigate to Sign In
        tvGoToSignIn.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SignUpActivity.this,
                    SignInActivity.class
            );

            startActivity(intent);
        });

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    private void createAccount() {

        String firstName = etFirstName.getText()
                .toString().trim();

        String lastName = etLastName.getText()
                .toString().trim();

        String email = etEmail.getText()
                .toString().trim();

        String mobile = etMobile.getText()
                .toString().trim();

        String dateOfBirth = etSignUpDob.getText()
                .toString().trim();

        String password = etPassword.getText()
                .toString();

        String confirmPassword = etConfirmPassword.getText()
                .toString();

        // Mobile number is optional
        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()
                || dateOfBirth.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill in all required fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validate email
        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {

            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Validate optional international mobile number
        if (!mobile.isEmpty()) {

            // International format: + followed by 8 to 15 digits
            if (!mobile.matches("^\\+[1-9]\\d{7,14}$")) {

                etMobile.setError(
                        "Enter your number with country code (e.g. +94771234567)"
                );

                etMobile.requestFocus();
                return;
            }
        }


        // Validate Date of Birth
        if (!dateOfBirth.matches(
                "\\d{2}/\\d{2}/\\d{4}"
        )) {

            Toast.makeText(
                    this,
                    "Please select your date of birth",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validate password length

        if (password.length() < 10) {
            etPassword.setError(
                    "Password must contain at least 10 characters"
            );
            etPassword.requestFocus();
            return;
        }

        // Password must contain an uppercase letter
        if (!password.matches("(?s).*[A-Z].*")) {
            etPassword.setError(
                    "Password must contain at least 1 capital letter (A-Z)"
            );
            etPassword.requestFocus();
            return;
        }

        // Password must contain a special symbol
        if (!password.matches("(?s).*[^a-zA-Z0-9\\s].*")) {
            etPassword.setError(
                    "Password must contain at least 1 special symbol (@, #, !, $)"
            );
            etPassword.requestFocus();
            return;
        }


        // Check matching passwords
        if (!password.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        // Save user and birthday to SQLite
        DatabaseHelper databaseHelper =
                new DatabaseHelper(this);

        long result;

        try {
            result = databaseHelper.registerUser(
                    firstName,
                    lastName,
                    email,
                    mobile,
                    dateOfBirth,
                    password
            );
        } finally {
            databaseHelper.close();
        }

        if (result != -1) {

            Toast.makeText(
                    this,
                    "Account created successfully!",
                    Toast.LENGTH_LONG
            ).show();

            // Open Sign In after successful registration
            Intent intent = new Intent(
                    SignUpActivity.this,
                    SignInActivity.class
            );

            startActivity(intent);
            finish();

        } else {

            etEmail.setError(
                    "Registration failed. Email may already be registered."
            );

            etEmail.requestFocus();
        }
    }
}

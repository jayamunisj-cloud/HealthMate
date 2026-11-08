package com.healthmate;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.healthmate.database.DatabaseHelper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;



public class SignUpActivity extends AppCompatActivity {

    private EditText etFirstName;
    private EditText etLastName;

    private EditText etEmail;
    private EditText etMobile;
    private EditText etPassword;

    private EditText etConfirmPassword;

    private Button btnCreateAccount;
    private TextView tvGoToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
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
        etFirstName = findViewById(R.id.etSignUpFirstName);
        etLastName = findViewById(R.id.etSignUpLastName);
        etEmail = findViewById(R.id.etSignUpEmail);
        etMobile = findViewById(R.id.etSignUpMobile);
        etPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etSignUpConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToSignIn = findViewById(R.id.tvGoToSignIn);
        btnCreateAccount.setOnClickListener(v -> createAccount());
        tvGoToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SignUpActivity.this,
                    SignInActivity.class
            );
            startActivity(intent);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    private void createAccount() {

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Check required fields
        if (firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || mobile.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email address
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Validate password length
        if (password.length() < 8) {
            etPassword.setError("Password must have at least 8 characters");
            etPassword.requestFocus();
            return;
        }

        // Check matching passwords
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Save account in SQLite
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        long result = databaseHelper.registerUser(
                firstName,
                lastName,
                email,
                mobile,
                password
        );

        databaseHelper.close();

        if (result != -1) {
            Toast.makeText(this,
                    "Account created successfully!",
                    Toast.LENGTH_LONG).show();

            // Login navigation will be added next
        } else {
            etEmail.setError("Registration failed. Email may already be registered.");
            etEmail.requestFocus();
        }
    }
}
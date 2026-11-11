package com.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.healthmate.database.DatabaseHelper;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignIn;
    private TextView tvGoToSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);
        setContentView(R.layout.activity_sign_in);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());


        // Connect Sign In screen components
        etEmail = findViewById(R.id.etSignInEmail);
        etPassword = findViewById(R.id.etSignInPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        btnSignIn.setOnClickListener(v -> signInUser());

        // Navigate to Sign Up screen
        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SignInActivity.this,
                    SignUpActivity.class
            );
            startActivity(intent);
        });

    }

    private void signInUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Check required fields
        if (email.isEmpty()) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {

            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        DatabaseHelper databaseHelper =
                new DatabaseHelper(this);

        boolean isValid = databaseHelper.validateUser(
                email,
                password
        );

        databaseHelper.close();

        if (isValid) {

            String normalizedEmail =
                    email.trim().toLowerCase(java.util.Locale.ROOT);

            boolean sessionSaved = getSharedPreferences(
                    "HealthMateSession",
                    MODE_PRIVATE
            ).edit()
                    .clear()
                    .putBoolean("isLoggedIn", true)
                    .putString("userEmail", normalizedEmail)
                    .commit();

            if (!sessionSaved) {
                Toast.makeText(
                        this,
                        "Unable to save sign-in session. Please try again.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    this,
                    "Sign In successful!",
                    Toast.LENGTH_SHORT
            ).show();

            // Open HealthMate Dashboard
            Intent intent = new Intent(
                    SignInActivity.this,
                    MainActivity.class
            );

            startActivity(intent);
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Incorrect email or password",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

}

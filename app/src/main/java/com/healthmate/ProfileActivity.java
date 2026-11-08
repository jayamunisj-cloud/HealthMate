
package com.healthmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.net.Uri;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


import androidx.appcompat.app.AppCompatActivity;

import com.healthmate.database.DatabaseHelper;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileMobile;
    private TextView tvProfileDob;

    private Button btnLogout;
    private TextView tvBackToDashboard;

    private ImageView ivProfilePhoto;


    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            saveProfilePhoto(uri);
                        }
                    }
            );



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

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        loadProfilePhoto();

        ivProfilePhoto.setOnClickListener(v -> {
            photoPickerLauncher.launch("image/*");
        });


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

        Button btnOpenChangePassword =
                findViewById(R.id.btnOpenChangePassword);

        btnOpenChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ProfileActivity.this,
                    ChangePasswordActivity.class
            );
            startActivity(intent);
        });


        // Return to Dashboard
        tvBackToDashboard.setOnClickListener(v -> finish());

        // Log out
        // Log out securely
        btnLogout.setOnClickListener(v -> {

            boolean sessionCleared = getSharedPreferences(
                    "HealthMateSession",
                    MODE_PRIVATE
            ).edit()
                    .clear()
                    .commit();

            if (!sessionCleared) {
                android.widget.Toast.makeText(
                        ProfileActivity.this,
                        "Unable to sign out. Please try again.",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Return to Dashboard with the previous activity stack cleared
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

    private File getProfilePhotoFile() {
        String email = getSharedPreferences(
                "HealthMateSession", MODE_PRIVATE
        ).getString("userEmail", "");

        if (email.isEmpty()) {
            return null;
        }

        String fileName = "profile_" + Integer.toHexString(
                email.trim().toLowerCase(java.util.Locale.ROOT).hashCode()
        ) + ".png";

        return new File(getFilesDir(), fileName);
    }

    private void saveProfilePhoto(Uri uri) {
        File photoFile = getProfilePhotoFile();

        if (photoFile == null) {
            return;
        }

        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri)) {

            if (inputStream == null) {
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                return;
            }

            try (FileOutputStream outputStream =
                         new FileOutputStream(photoFile)) {

                if (!bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        outputStream)) {
                    throw new java.io.IOException("Could not save photo");
                }
            }


            bitmap.recycle();

            runOnUiThread(() -> {
                loadProfilePhoto();
            });


        } catch (Exception e) {
            android.widget.Toast.makeText(
                    this,
                    "Could not save profile photo",
                    android.widget.Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadProfilePhoto() {
        File photoFile = getProfilePhotoFile();

        if (photoFile != null && photoFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(
                    photoFile.getAbsolutePath()
            );

            if (bitmap != null) {
                ivProfilePhoto.setImageTintList(null);
                ivProfilePhoto.setPadding(0, 0, 0, 0);
                ivProfilePhoto.setImageBitmap(bitmap);
            }
        }
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

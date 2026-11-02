package com.example.qure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qure.activities.AdminDashboardActivity;
import com.example.qure.activities.DoctorDashboardActivity;
import com.example.qure.activities.LoginActivity;
import com.example.qure.activities.PatientDashboardActivity;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity.java — Welcome Screen (Updated for Phase 4)
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   This is still the Welcome Screen.
 *   But now, when the app starts, we FIRST CHECK if a user
 *   is already logged in. If yes, we skip the Welcome Screen
 *   and go directly to their dashboard.
 *
 *   This is the "auto-login" feature — the user doesn't have
 *   to log in every time they open the app.
 *
 *   Flow when app starts:
 *     1. Check if Firebase has a current user session
 *     2. If YES → get their role from SharedPreferences
 *                → go directly to their dashboard
 *     3. If NO  → show the Welcome Screen normally
 *
 * WHERE THIS FILE IS:
 *   app/src/main/java/com/example/qure/MainActivity.java
 * -------------------------------------------------------
 */
import com.example.qure.utils.NotificationHelper;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private MaterialButton buttonGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce consistent, crisp high-contrast theme across all devices
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        // Initialize local SQLite database bridge
        com.example.qure.database.FirebaseHelper.init(this);

        // -------------------------------------------------------
        // INITIALIZE NOTIFICATIONS
        // -------------------------------------------------------
        NotificationHelper.createNotificationChannel(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        // -------------------------------------------------------
        // AUTO-LOGIN CHECK
        // Do this BEFORE setContentView — so if the user is already
        // logged in, we navigate away without showing the welcome
        // screen at all (no flash / flicker).
        // -------------------------------------------------------
        if (isUserAlreadyLoggedIn()) {
            // User is logged in — go to their dashboard and stop here
            return;
        }
        // User is NOT logged in — show the Welcome Screen normally
        setContentView(R.layout.activity_main);
        buttonGetStarted = findViewById(R.id.buttonGetStarted);
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    // -------------------------------------------------------
    // isUserAlreadyLoggedIn()
    //
    // Returns TRUE if:
    //   - Firebase still has an active session (user is logged in)
    //   - AND we have a stored role in SharedPreferences
    //
    // If both are true, we navigate directly to the dashboard
    // and return TRUE (so onCreate stops early).
    //
    // Returns FALSE if the user needs to log in.
    // -------------------------------------------------------
    private boolean isUserAlreadyLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(
                Constants.PREF_FILE_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
        String role = prefs.getString(Constants.PREF_USER_ROLE, "");

        if (isLoggedIn && !role.isEmpty()) {
            navigateToDashboard(role);
            return true;
        }
        // Fallback check if Firebase has an active session
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !role.isEmpty()) {
            navigateToDashboard(role);
            return true;
        }
        // No active session or no stored role — user must log in
        return false;
    }
    // -------------------------------------------------------
    // navigateToDashboard()
    //
    // Opens the correct dashboard based on the user's role.
    // -------------------------------------------------------
    private void navigateToDashboard(String role) {
        Intent intent;
        if (Constants.ROLE_DOCTOR.equals(role)) {
            intent = new Intent(this, DoctorDashboardActivity.class);
        } else if (Constants.ROLE_ADMIN.equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, PatientDashboardActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
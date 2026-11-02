package com.example.qure.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qure.R;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity.java — Login Screen (Phase 4 — Firebase Auth)
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   This activity handles the real Firebase login.
 *   Flow:
 *     1. User enters email + password
 *     2. We validate the fields (Phase 3 logic)
 *     3. We call FirebaseHelper.loginUser()
 *     4. Firebase checks credentials
 *     5. If correct → fetch user role from Firestore
 *     6. Save name + role to SharedPreferences
 *     7. Open the correct dashboard (Patient / Doctor / Admin)
 *     8. If wrong → show error message
 *
 * WHERE THIS FILE IS:
 *   app/src/main/java/com/example/qure/activities/LoginActivity.java
 *
 * CONNECTS TO:
 *   - activity_login.xml
 *   - FirebaseHelper.java (handles Firebase calls)
 *   - PatientDashboardActivity, DoctorDashboardActivity, AdminDashboardActivity
 * -------------------------------------------------------
 */
public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private MaterialButton buttonLogin;
    private TextView textGoToRegister;

    // Firebase helper
    private FirebaseHelper firebaseHelper;

    // Loading dialog — shown while Firebase is working
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase helper
        firebaseHelper = new FirebaseHelper();

        // Connect UI components
        layoutEmail      = findViewById(R.id.layoutEmail);
        layoutPassword   = findViewById(R.id.layoutPassword);
        editEmail        = findViewById(R.id.editEmail);
        editPassword     = findViewById(R.id.editPassword);
        buttonLogin      = findViewById(R.id.buttonLogin);
        textGoToRegister = findViewById(R.id.textGoToRegister);

        // Set up the loading dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false); // User can't dismiss it while loading

        // -------------------------------------------------------
        // LOGIN BUTTON
        // -------------------------------------------------------
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email    = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                // First validate the fields (same as Phase 3)
                if (!validateInputs(email, password)) {
                    return;
                }

                // Show loading spinner
                progressDialog.show();
                buttonLogin.setEnabled(false); // Prevent double-clicking

                // Call Firebase login via FirebaseHelper
                firebaseHelper.loginUser(email, password,
                        new FirebaseHelper.RoleCallback() {

                            @Override
                            public void onRoleReceived(String role, String name) {
                                // Login + role fetch successful!
                                progressDialog.dismiss();
                                buttonLogin.setEnabled(true);

                                // Save user info locally so other screens can use it
                                saveUserToPreferences(name, email, role);

                                // Open the correct dashboard based on role
                                navigateToDashboard(role);
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                // Login failed
                                progressDialog.dismiss();
                                buttonLogin.setEnabled(true);
                                Toast.makeText(LoginActivity.this,
                                        errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // -------------------------------------------------------
        // REGISTER LINK
        // -------------------------------------------------------
        textGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // -------------------------------------------------------
    // saveUserToPreferences()
    //
    // Saves the user's name and role to SharedPreferences
    // so we can read it in any activity without hitting Firestore.
    // SharedPreferences = simple local storage on the device.
    // -------------------------------------------------------
    private void saveUserToPreferences(String name, String email, String role) {
        SharedPreferences prefs = getSharedPreferences(
                Constants.PREF_FILE_NAME, MODE_PRIVATE);
        String userId = prefs.getString(Constants.PREF_USER_ID, null);
        if (userId == null || userId.isEmpty()) {
            userId = "user_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }
        prefs.edit()
                .putString(Constants.PREF_USER_ID, userId)
                .putString(Constants.PREF_USER_NAME, name)
                .putString(Constants.PREF_USER_EMAIL, email)
                .putString(Constants.PREF_USER_ROLE, role)
                .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                .apply();
    }

    // -------------------------------------------------------
    // navigateToDashboard()
    //
    // Sends the user to the correct dashboard based on their role.
    // Clears the activity back stack so pressing Back exits the app.
    // -------------------------------------------------------
    private void navigateToDashboard(String role) {
        Intent intent;

        if (Constants.ROLE_DOCTOR.equals(role)) {
            intent = new Intent(this, DoctorDashboardActivity.class);
        } else if (Constants.ROLE_ADMIN.equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            // Default to Patient dashboard
            intent = new Intent(this, PatientDashboardActivity.class);
        }

        // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK clears the back stack.
        // This means the user can't press Back to get to the Login screen.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------
    // validateInputs() — same validation logic from Phase 3
    // -------------------------------------------------------
    private boolean validateInputs(String email, String password) {
        layoutEmail.setError(null);
        layoutPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email is required");
            editEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Please enter a valid email address");
            editEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError("Password is required");
            editPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            layoutPassword.setError("Password must be at least 6 characters");
            editPassword.requestFocus();
            return false;
        }

        return true;
    }
}

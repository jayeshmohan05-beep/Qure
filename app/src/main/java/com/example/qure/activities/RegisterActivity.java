package com.example.qure.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.qure.R;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * RegisterActivity.java — Register Screen (Phase 4 — Firebase Auth)
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   This activity handles real Firebase user registration.
 *   Flow:
 *     1. User fills all fields + selects a role
 *     2. We validate all inputs
 *     3. We call FirebaseHelper.registerUser()
 *     4. Firebase creates the account + saves to Firestore
 *     5. We save name + role to SharedPreferences
 *     6. We navigate to the correct dashboard immediately
 *
 * WHERE THIS FILE IS:
 *   app/src/main/java/com/example/qure/activities/RegisterActivity.java
 * -------------------------------------------------------
 */
public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout layoutFullName;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;
    private TextInputLayout layoutDoctorDepartment;
    private AutoCompleteTextView spinnerDoctorDepartment;

    private TextInputEditText editFullName;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirmPassword;

    private MaterialButton buttonRolePatient;
    private MaterialButton buttonRoleDoctor;
    private MaterialButton buttonRoleAdmin;
    private TextView textSelectedRole;

    private MaterialButton buttonRegister;
    private TextView textGoToLogin;

    // Firebase helper
    private FirebaseHelper firebaseHelper;

    // Loading dialog
    private ProgressDialog progressDialog;

    // Currently selected role
    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHelper = new FirebaseHelper();

        // Connect UI components
        layoutFullName        = findViewById(R.id.layoutFullName);
        layoutEmail           = findViewById(R.id.layoutEmail);
        layoutPassword        = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        layoutDoctorDepartment = findViewById(R.id.layoutDoctorDepartment);
        spinnerDoctorDepartment = findViewById(R.id.spinnerDoctorDepartment);

        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, Constants.DEPARTMENTS);
        spinnerDoctorDepartment.setAdapter(deptAdapter);
        spinnerDoctorDepartment.setOnClickListener(v -> spinnerDoctorDepartment.showDropDown());

        editFullName        = findViewById(R.id.editFullName);
        editEmail           = findViewById(R.id.editEmail);
        editPassword        = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        buttonRolePatient = findViewById(R.id.buttonRolePatient);
        buttonRoleDoctor  = findViewById(R.id.buttonRoleDoctor);
        buttonRoleAdmin   = findViewById(R.id.buttonRoleAdmin);
        textSelectedRole  = findViewById(R.id.textSelectedRole);

        buttonRegister = findViewById(R.id.buttonRegister);
        textGoToLogin  = findViewById(R.id.textGoToLogin);

        // Set up loading dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your account...");
        progressDialog.setCancelable(false);

        // -------------------------------------------------------
        // ROLE SELECTION BUTTONS
        // -------------------------------------------------------
        buttonRolePatient.setOnClickListener(v -> selectRole(Constants.ROLE_PATIENT));
        buttonRoleDoctor.setOnClickListener(v  -> selectRole(Constants.ROLE_DOCTOR));
        buttonRoleAdmin.setOnClickListener(v   -> selectRole(Constants.ROLE_ADMIN));

        // -------------------------------------------------------
        // REGISTER BUTTON — Firebase registration
        // -------------------------------------------------------
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name            = editFullName.getText().toString().trim();
                String email           = editEmail.getText().toString().trim();
                String password        = editPassword.getText().toString().trim();
                String confirmPassword = editConfirmPassword.getText().toString().trim();
                String department      = Constants.ROLE_DOCTOR.equals(selectedRole)
                        ? spinnerDoctorDepartment.getText().toString().trim()
                        : "";

                // Validate all fields
                if (!validateInputs(name, email, password, confirmPassword)) {
                    return;
                }

                // Show loading
                progressDialog.show();
                buttonRegister.setEnabled(false);

                // Call Firebase registration
                firebaseHelper.registerUser(name, email, password, selectedRole, department,
                        new FirebaseHelper.AuthCallback() {

                            @Override
                            public void onSuccess() {
                                // Registration and Firestore save successful!
                                progressDialog.dismiss();
                                buttonRegister.setEnabled(true);

                                Toast.makeText(RegisterActivity.this,
                                        "Account created successfully!",
                                        Toast.LENGTH_SHORT).show();

                                // Save user info locally
                                saveUserToPreferences(name, email, selectedRole, department);

                                // Navigate to the correct dashboard
                                navigateToDashboard(selectedRole);
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                progressDialog.dismiss();
                                buttonRegister.setEnabled(true);
                                Toast.makeText(RegisterActivity.this,
                                        errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // -------------------------------------------------------
        // LOGIN LINK
        // -------------------------------------------------------
        textGoToLogin.setOnClickListener(v -> finish());
    }

    // -------------------------------------------------------
    // selectRole() — highlights the chosen role button
    // -------------------------------------------------------
    private void selectRole(String role) {
        selectedRole = role;
        textSelectedRole.setText("Selected: " + role);

        resetRoleButton(buttonRolePatient);
        resetRoleButton(buttonRoleDoctor);
        resetRoleButton(buttonRoleAdmin);

        if (role.equals(Constants.ROLE_PATIENT)) {
            highlightRoleButton(buttonRolePatient);
            layoutDoctorDepartment.setVisibility(View.GONE);
        } else if (role.equals(Constants.ROLE_DOCTOR)) {
            highlightRoleButton(buttonRoleDoctor);
            layoutDoctorDepartment.setVisibility(View.VISIBLE);
        } else if (role.equals(Constants.ROLE_ADMIN)) {
            highlightRoleButton(buttonRoleAdmin);
            layoutDoctorDepartment.setVisibility(View.GONE);
        }
    }

    private void highlightRoleButton(MaterialButton button) {
        button.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.colorPrimary));
        button.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void resetRoleButton(MaterialButton button) {
        button.setBackgroundTintList(
                ContextCompat.getColorStateList(this, android.R.color.transparent));
        button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    // -------------------------------------------------------
    // saveUserToPreferences() — same as in LoginActivity
    // -------------------------------------------------------
    private void saveUserToPreferences(String name, String email, String role, String department) {
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
                .putString(Constants.FIELD_DEPARTMENT, department)
                .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                .apply();
    }

    // -------------------------------------------------------
    // navigateToDashboard() — role-based navigation
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

    // -------------------------------------------------------
    // validateInputs() — same as Phase 3
    // -------------------------------------------------------
    private boolean validateInputs(String name, String email,
                                   String password, String confirmPassword) {
        layoutFullName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        if (layoutDoctorDepartment != null) layoutDoctorDepartment.setError(null);

        if (TextUtils.isEmpty(name)) {
            layoutFullName.setError("Full name is required");
            editFullName.requestFocus();
            return false;
        }

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

        if (TextUtils.isEmpty(confirmPassword)) {
            layoutConfirmPassword.setError("Please confirm your password");
            editConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(selectedRole)) {
            Toast.makeText(this,
                    "Please select a role (Patient, Doctor, or Admin)",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Constants.ROLE_DOCTOR.equals(selectedRole)) {
            String dept = spinnerDoctorDepartment.getText().toString().trim();
            if (TextUtils.isEmpty(dept)) {
                layoutDoctorDepartment.setError("Please select your specialization/department");
                spinnerDoctorDepartment.requestFocus();
                return false;
            }
        }

        return true;
    }
}

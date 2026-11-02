package com.example.qure.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qure.MainActivity;
import com.example.qure.R;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;

/**
 * ProfileActivity.java — User Profile & Account Settings Screen
 * -------------------------------------------------------------
 * Displays:
 *   1. Full Name, Role & Initial Avatar.
 *   2. Registered Gmail / Email.
 *   3. Mobile Number (with edit/update dialog).
 *   4. Specialization/Department (for Doctors).
 *   5. Delete Account option with confirmation dialog.
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private TextView textAvatarInitial, textProfileName, textProfileRole;
    private TextView textProfileEmail, textProfilePhone, textProfileDepartment;
    private LinearLayout layoutDepartmentSection;
    private MaterialButton buttonEditPhone, buttonDeleteAccount;

    private FirebaseHelper firebaseHelper;
    private ProgressDialog progressDialog;

    private String userId, userName, userEmail, userRole, userDepartment, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseHelper = new FirebaseHelper();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting account...");
        progressDialog.setCancelable(false);

        initUI();
        loadProfileData();
    }

    private void initUI() {
        buttonBack = findViewById(R.id.buttonBack);
        textAvatarInitial = findViewById(R.id.textAvatarInitial);
        textProfileName = findViewById(R.id.textProfileName);
        textProfileRole = findViewById(R.id.textProfileRole);
        textProfileEmail = findViewById(R.id.textProfileEmail);
        textProfilePhone = findViewById(R.id.textProfilePhone);
        textProfileDepartment = findViewById(R.id.textProfileDepartment);
        layoutDepartmentSection = findViewById(R.id.layoutDepartmentSection);
        buttonEditPhone = findViewById(R.id.buttonEditPhone);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);

        buttonBack.setOnClickListener(v -> finish());
        buttonEditPhone.setOnClickListener(v -> showEditPhoneDialog());
        buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
        userId = prefs.getString(Constants.PREF_USER_ID, "");
        userName = prefs.getString(Constants.PREF_USER_NAME, "User");
        userEmail = prefs.getString(Constants.PREF_USER_EMAIL, "No email");
        userRole = prefs.getString(Constants.PREF_USER_ROLE, "Patient");
        userDepartment = prefs.getString(Constants.FIELD_DEPARTMENT, "");
        userPhone = prefs.getString(Constants.PREF_USER_PHONE, "");

        if (userPhone.isEmpty() && firebaseHelper.getDb() != null && !userId.isEmpty()) {
            userPhone = firebaseHelper.getDb().getUserPhone(userId);
        }

        textProfileName.setText(userName);
        textProfileRole.setText(userRole + " Account");
        textProfileEmail.setText(userEmail);

        if (userPhone != null && !userPhone.trim().isEmpty()) {
            textProfilePhone.setText(userPhone);
        } else {
            textProfilePhone.setText("Not set (Tap Edit to add)");
        }

        // Set avatar initial
        if (!userName.isEmpty()) {
            String cleanName = userName.replace("Dr. ", "").replace("Dr.", "").trim();
            if (!cleanName.isEmpty()) {
                textAvatarInitial.setText(String.valueOf(cleanName.charAt(0)).toUpperCase());
            } else {
                textAvatarInitial.setText("U");
            }
        }

        // Show doctor department if Doctor role
        if (Constants.ROLE_DOCTOR.equalsIgnoreCase(userRole)) {
            layoutDepartmentSection.setVisibility(View.VISIBLE);
            textProfileDepartment.setText(!userDepartment.isEmpty() ? userDepartment : "General Medicine");
        } else {
            layoutDepartmentSection.setVisibility(View.GONE);
        }
    }

    private void showEditPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Mobile Number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint("Enter mobile number");
        if (userPhone != null && !userPhone.isEmpty() && !userPhone.startsWith("Not set")) {
            input.setText(userPhone);
            input.setSelection(userPhone.length());
        }
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newPhone = input.getText().toString().trim();
            if (newPhone.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            userPhone = newPhone;
            textProfilePhone.setText(newPhone);
            firebaseHelper.updateUserPhone(userId, newPhone, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProfileActivity.this, "Mobile number updated!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(ProfileActivity.this, "Saved locally. Cloud sync pending.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account?\n\nThis will remove your login credentials, booking history, and active appointments from QURE. This action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    progressDialog.show();
                    firebaseHelper.deleteUserAccount(new FirebaseHelper.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Your account has been deleted.", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed to delete account: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

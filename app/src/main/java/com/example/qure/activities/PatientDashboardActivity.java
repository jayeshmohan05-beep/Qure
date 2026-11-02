package com.example.qure.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.qure.MainActivity;
import com.example.qure.R;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;

/**
 * PatientDashboardActivity.java — Patient Dashboard
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   This is the main hub for the Patient. It features 4 cards:
 *     1. Book Appointment (Coming in Phase 7)
 *     2. My Queue Status  (Coming in Phase 8)
 *     3. Medical Reports  (Coming in Phase 14)
 *     4. Hospital Location(Coming in Phase 15)
 *
 *   Right now, clicking these cards will display a Toast message.
 *   The LOGOUT button successfully ends the session and returns
 *   the user to the Welcome Screen.
 *
 * WHERE THIS FILE IS:
 *   app/src/main/java/com/example/qure/activities/PatientDashboardActivity.java
 *
 * CONNECTS TO:
 *   - activity_patient_dashboard.xml (the layout)
 *   - FirebaseHelper.java (for logout)
 *   - MainActivity.java (navigates here after logout)
 * -------------------------------------------------------
 */
public class PatientDashboardActivity extends AppCompatActivity {

    // UI Elements
    private TextView textWelcome;
    private TextView textProfileInitial;
    private CardView cardBookAppointment;
    private CardView cardQueueStatus;
    private CardView cardUploadReport;
    private CardView cardHospitalLocation;
    private MaterialButton buttonLogout;

    // Firebase Helper
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        // Initialize Firebase helper
        firebaseHelper = new FirebaseHelper();

        // Connect UI elements from XML
        textWelcome          = findViewById(R.id.textWelcome);
        cardBookAppointment  = findViewById(R.id.cardBookAppointment);
        cardQueueStatus      = findViewById(R.id.cardQueueStatus);
        cardUploadReport     = findViewById(R.id.cardUploadReport);
        cardHospitalLocation = findViewById(R.id.cardHospitalLocation);
        buttonLogout         = findViewById(R.id.buttonLogout);

        View buttonProfile = findViewById(R.id.buttonProfile);
        textProfileInitial = findViewById(R.id.textProfileInitial);

        buttonProfile.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        updateHeader();

        // 2. Set Click Listeners for Dashboard Cards
        
        cardBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Book Appointment screen
                Intent intent = new Intent(PatientDashboardActivity.this, BookAppointmentActivity.class);
                startActivity(intent);
            }
        });

        cardQueueStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Queue Status screen
                Intent intent = new Intent(PatientDashboardActivity.this, QueueStatusActivity.class);
                startActivity(intent);
            }
        });

        cardUploadReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Medical Reports screen
                Intent intent = new Intent(PatientDashboardActivity.this, MedicalReportsActivity.class);
                startActivity(intent);
            }
        });

        cardHospitalLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Map screen
                Intent intent = new Intent(PatientDashboardActivity.this, HospitalLocationActivity.class);
                startActivity(intent);
            }
        });

        // 3. LOGOUT Button Logic
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        // Sign out of Firebase
        firebaseHelper.logoutUser();

        // Clear all locally saved user data (SharedPreferences)
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Go back to Welcome screen (MainActivity)
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the activity stack so the user can't press Back to come back here
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateHeader() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        String userName = prefs.getString(Constants.PREF_USER_NAME, "Patient");
        if (textWelcome != null) {
            textWelcome.setText("Welcome, " + userName + "!");
        }
        if (textProfileInitial != null && !userName.isEmpty()) {
            textProfileInitial.setText(String.valueOf(userName.charAt(0)).toUpperCase());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeader();
    }

    // Prevent going back to Login after successful login
    @Override
    public void onBackPressed() {
        // Do nothing — user must use the Logout button
    }
}

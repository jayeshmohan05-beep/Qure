package com.example.qure.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qure.R;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.models.Appointment;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * QueueStatusActivity.java — My Queue Status Screen
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   - Checks Firestore for any active appointment.
 *   - Shows the Token Number and Status.
 *   - Allows the patient to Check In when they reach the hospital.
 *
 * WHERE THIS FILE IS:
 *   app/src/main/java/com/example/qure/activities/QueueStatusActivity.java
 * -------------------------------------------------------
 */
public class QueueStatusActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private LinearLayout layoutNoAppointment, layoutActiveAppointment;
    
    private TextView textTokenNumber, textStatus, textDoctorName, textDepartment, textDate, textInstructions;
    private MaterialButton buttonGoBack, buttonCheckIn;

    private FirebaseHelper firebaseHelper;
    private Appointment activeAppointment; // Holds our current appointment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_status);

        firebaseHelper = new FirebaseHelper();

        // Connect UI
        progressBar             = findViewById(R.id.progressBar);
        layoutNoAppointment     = findViewById(R.id.layoutNoAppointment);
        layoutActiveAppointment = findViewById(R.id.layoutActiveAppointment);
        
        textTokenNumber  = findViewById(R.id.textTokenNumber);
        textStatus       = findViewById(R.id.textStatus);
        textDoctorName   = findViewById(R.id.textDoctorName);
        textDepartment   = findViewById(R.id.textDepartment);
        textDate         = findViewById(R.id.textDate);
        textInstructions = findViewById(R.id.textInstructions);

        buttonGoBack  = findViewById(R.id.buttonGoBack);
        buttonCheckIn = findViewById(R.id.buttonCheckIn);

        // Simple go back logic
        buttonGoBack.setOnClickListener(v -> finish());

        // Check-in logic
        buttonCheckIn.setOnClickListener(v -> handleCheckIn());

        // Fetch data
        loadActiveAppointment();
    }

    private void loadActiveAppointment() {
        // Show loading spinner
        progressBar.setVisibility(View.VISIBLE);
        layoutNoAppointment.setVisibility(View.GONE);
        layoutActiveAppointment.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        String patientId = prefs.getString(Constants.PREF_USER_ID, "");
        if (patientId.isEmpty()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                patientId = user.getUid();
            }
        }
        String patientName = prefs.getString(Constants.PREF_USER_NAME, "");

        firebaseHelper.getActiveAppointment(patientId, patientName, new FirebaseHelper.AppointmentCallback() {
            @Override
            public void onAppointmentLoaded(Appointment appointment) {
                progressBar.setVisibility(View.GONE);
                
                if (appointment == null) {
                    // No active appointments
                    layoutNoAppointment.setVisibility(View.VISIBLE);
                } else {
                    // Found one!
                    activeAppointment = appointment;
                    layoutActiveAppointment.setVisibility(View.VISIBLE);
                    updateUIWithAppointmentData();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QueueStatusActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUIWithAppointmentData() {
        // Fill text fields
        textTokenNumber.setText(String.format("%02d", activeAppointment.getTokenNumber()));
        textDoctorName.setText("Doctor: " + activeAppointment.getDoctorName());
        textDepartment.setText("Dept: " + activeAppointment.getDepartment());
        textDate.setText("Date: " + activeAppointment.getAppointmentDate());

        String status = activeAppointment.getStatus();
        textStatus.setText("Status: " + status);

        // Update UI based on current status
        if (Constants.STATUS_REGISTERED.equals(status)) {
            // Patient has booked, but not checked in at hospital yet
            textStatus.setTextColor(getResources().getColor(R.color.colorStatusRegistered));
            buttonCheckIn.setVisibility(View.VISIBLE);
            textInstructions.setText("When you reach the hospital, click Check In to join the live waiting queue.");
            
        } else if (Constants.STATUS_WAITING.equals(status)) {
            // Patient has checked in and is waiting
            textStatus.setTextColor(getResources().getColor(R.color.colorStatusWaiting));
            buttonCheckIn.setVisibility(View.GONE);
            textInstructions.setText("You are checked in! Please wait in the lobby until your token is called.");
            
        } else if (Constants.STATUS_IN_CONSULTATION.equals(status)) {
            // Patient is with the doctor
            textStatus.setTextColor(getResources().getColor(R.color.colorStatusConsultation));
            buttonCheckIn.setVisibility(View.GONE);
            textInstructions.setText("The doctor is ready for you. Please proceed to the consultation room.");
        }
    }

    private void handleCheckIn() {
        if (activeAppointment == null) return;

        progressBar.setVisibility(View.VISIBLE);
        buttonCheckIn.setEnabled(false);

        firebaseHelper.checkInAppointment(activeAppointment.getAppointmentId(), new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                // Instantly refresh the UI to show "Waiting" state
                loadActiveAppointment(); 
                Toast.makeText(QueueStatusActivity.this, "Checked In successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                buttonCheckIn.setEnabled(true);
                Toast.makeText(QueueStatusActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}

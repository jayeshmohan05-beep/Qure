package com.example.qure.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.MainActivity;
import com.example.qure.R;
import com.example.qure.adapters.QueueAdapter;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.models.Appointment;
import com.example.qure.queue.QueueManager;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardActivity extends AppCompatActivity {

    private TextView textWelcome, textActiveToken, textActiveName, textActiveSymptoms, textEmptyQueue;
    private TextView textProfileInitial;
    private View buttonProfile;
    private CardView cardFree, cardActive;
    private MaterialButton buttonCallNext, buttonComplete;
    private ImageButton buttonLogout;
    private RecyclerView recyclerViewQueue;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private QueueAdapter queueAdapter;
    private List<Appointment> currentQueue = new ArrayList<>();
    
    private String doctorName;
    private Appointment currentActivePatient = null;
    private ListenerRegistration appointmentsListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        firebaseHelper = new FirebaseHelper();

        initUI();
        loadDashboardData();
    }

    private void initUI() {
        textWelcome = findViewById(R.id.textWelcome);
        buttonProfile = findViewById(R.id.buttonProfile);
        textProfileInitial = findViewById(R.id.textProfileInitial);

        if (buttonProfile != null) {
            buttonProfile.setOnClickListener(v -> {
                Intent intent = new Intent(DoctorDashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        updateDoctorHeader();
        
        textActiveToken = findViewById(R.id.textActiveToken);
        textActiveName = findViewById(R.id.textActiveName);
        textActiveSymptoms = findViewById(R.id.textActiveSymptoms);
        textEmptyQueue = findViewById(R.id.textEmptyQueue);
        
        cardFree = findViewById(R.id.cardFree);
        cardActive = findViewById(R.id.cardActive);
        
        buttonCallNext = findViewById(R.id.buttonCallNext);
        buttonComplete = findViewById(R.id.buttonComplete);
        buttonLogout = findViewById(R.id.buttonLogout);
        
        recyclerViewQueue = findViewById(R.id.recyclerViewQueue);
        progressBar = findViewById(R.id.progressBar);

        queueAdapter = new QueueAdapter(currentQueue);
        recyclerViewQueue.setAdapter(queueAdapter);

        buttonLogout.setOnClickListener(v -> logout());
        buttonCallNext.setOnClickListener(v -> callNextPatient());
        buttonComplete.setOnClickListener(v -> completeCurrentPatient());
    }

    private void updateDoctorHeader() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        doctorName = prefs.getString(Constants.PREF_USER_NAME, "Doctor");
        if (!doctorName.startsWith("Dr. ")) {
            doctorName = "Dr. " + doctorName;
        }

        String dept = prefs.getString(Constants.FIELD_DEPARTMENT, "");
        if (textWelcome != null) {
            if (!dept.isEmpty()) {
                textWelcome.setText(doctorName + " (" + dept + ")");
            } else {
                textWelcome.setText(doctorName);
            }
        }

        if (textProfileInitial != null && !doctorName.isEmpty()) {
            String initialName = doctorName.startsWith("Dr. ") && doctorName.length() > 4 ? doctorName.substring(4).trim() : doctorName.trim();
            if (!initialName.isEmpty()) {
                textProfileInitial.setText(String.valueOf(initialName.charAt(0)).toUpperCase());
            } else {
                textProfileInitial.setText("D");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDoctorHeader();
        loadDashboardData();
        startRealtimeQueueListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRealtimeQueueListener();
    }

    private void startRealtimeQueueListener() {
        if (appointmentsListener != null) return;
        try {
            appointmentsListener = FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_APPOINTMENTS)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) return;
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots) {
                                Appointment appt = doc.toObject(Appointment.class);
                                if (appt != null && firebaseHelper.getDb() != null) {
                                    firebaseHelper.getDb().insertAppointment(appt);
                                }
                            }
                            if (firebaseHelper.getDb() != null) {
                                currentActivePatient = firebaseHelper.getDb().getDoctorCurrentPatient(doctorName);
                                updateCurrentPatientUI();
                                List<Appointment> queue = firebaseHelper.getDb().getDoctorWaitingQueue(doctorName);
                                currentQueue = QueueManager.sortQueue(queue);
                                queueAdapter.setQueueList(currentQueue);
                                if (currentQueue.isEmpty()) {
                                    textEmptyQueue.setVisibility(View.VISIBLE);
                                    buttonCallNext.setEnabled(false);
                                } else {
                                    textEmptyQueue.setVisibility(View.GONE);
                                    buttonCallNext.setEnabled(true);
                                }
                            }
                        }
                    });
        } catch (Exception ignored) {}
    }

    private void stopRealtimeQueueListener() {
        if (appointmentsListener != null) {
            appointmentsListener.remove();
            appointmentsListener = null;
        }
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);
        
        // 1. Check if Doctor currently has a patient
        firebaseHelper.getDoctorCurrentPatient(doctorName, new FirebaseHelper.AppointmentCallback() {
            @Override
            public void onAppointmentLoaded(Appointment appointment) {
                currentActivePatient = appointment;
                updateCurrentPatientUI();
                
                // 2. Fetch the waiting queue
                loadWaitingQueue();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DoctorDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWaitingQueue() {
        firebaseHelper.getDoctorQueue(doctorName, new FirebaseHelper.QueueCallback() {
            @Override
            public void onQueueLoaded(List<Appointment> queue) {
                progressBar.setVisibility(View.GONE);
                
                // --- THIS IS THE MAGIC ---
                // Here we run the unsorted database results through our OS Scheduling algorithm!
                currentQueue = QueueManager.sortQueue(queue);
                
                queueAdapter.setQueueList(currentQueue);
                
                if (currentQueue.isEmpty()) {
                    textEmptyQueue.setVisibility(View.VISIBLE);
                    buttonCallNext.setEnabled(false);
                } else {
                    textEmptyQueue.setVisibility(View.GONE);
                    buttonCallNext.setEnabled(true);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateCurrentPatientUI() {
        if (currentActivePatient == null) {
            cardActive.setVisibility(View.GONE);
            cardFree.setVisibility(View.VISIBLE);
        } else {
            cardFree.setVisibility(View.GONE);
            cardActive.setVisibility(View.VISIBLE);
            
            textActiveToken.setText("Token: " + currentActivePatient.getTokenNumber());
            textActiveName.setText(currentActivePatient.getPatientName());
            textActiveSymptoms.setText("Symptoms: " + currentActivePatient.getSymptoms());
        }
    }

    private void callNextPatient() {
        if (currentQueue.isEmpty()) return;
        
        // Because the queue is sorted using FCFS + Emergency Preemption, index 0 is ALWAYS the correct next patient!
        Appointment nextPatient = currentQueue.get(0);
        
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.startConsultation(nextPatient.getAppointmentId(), new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                loadDashboardData(); // Refresh everything
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DoctorDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void completeCurrentPatient() {
        if (currentActivePatient == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.completeConsultation(currentActivePatient.getAppointmentId(), new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                loadDashboardData(); // Refresh everything
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DoctorDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        firebaseHelper.logoutUser();
        getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

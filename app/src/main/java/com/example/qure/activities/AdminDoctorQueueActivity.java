package com.example.qure.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.adapters.AdminQueueAdapter;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.models.Appointment;
import com.example.qure.queue.QueueManager;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminDoctorQueueActivity.java — Admin Doctor Queue View & Emergency Flagging
 * ----------------------------------------------------------------------------
 * Allows the Admin to:
 *   1. View the live waiting queue for a specific doctor in FCFS order.
 *   2. Flag any appointment as an Emergency, immediately pre-empting the
 *      queue and promoting the patient to the very front (index 0).
 *   3. Quickly add an appointment for this doctor.
 */
public class AdminDoctorQueueActivity extends AppCompatActivity implements AdminQueueAdapter.OnEmergencyToggleListener {

    private ImageButton buttonBack, buttonDeleteDoctor;
    private TextView textDoctorName, textDoctorEmail, textDoctorDepartment, textQueueSummary;
    private MaterialButton buttonAddAppointment, buttonAddEmptyAppointment;
    private RecyclerView recyclerViewQueue;
    private LinearLayout layoutEmptyQueue;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private AdminQueueAdapter adapter;
    private List<Appointment> currentQueue = new ArrayList<>();

    private String doctorName = "";
    private String department = "";
    private String doctorEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctor_queue);

        firebaseHelper = new FirebaseHelper();

        doctorName = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_NAME);
        department = getIntent().getStringExtra(Constants.EXTRA_DEPARTMENT);
        doctorEmail = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_EMAIL);

        if (doctorName == null || doctorName.isEmpty()) {
            doctorName = "Doctor";
        }
        if (department == null || department.isEmpty()) {
            department = "General Medicine";
        }

        initUI();
        loadQueue();
    }

    private void initUI() {
        buttonBack = findViewById(R.id.buttonBack);
        buttonDeleteDoctor = findViewById(R.id.buttonDeleteDoctor);
        textDoctorName = findViewById(R.id.textDoctorName);
        textDoctorEmail = findViewById(R.id.textDoctorEmail);
        textDoctorDepartment = findViewById(R.id.textDoctorDepartment);
        textQueueSummary = findViewById(R.id.textQueueSummary);
        buttonAddAppointment = findViewById(R.id.buttonAddAppointment);
        buttonAddEmptyAppointment = findViewById(R.id.buttonAddEmptyAppointment);
        recyclerViewQueue = findViewById(R.id.recyclerViewQueue);
        layoutEmptyQueue = findViewById(R.id.layoutEmptyQueue);
        progressBar = findViewById(R.id.progressBar);

        textDoctorName.setText(doctorName);
        updateDoctorEmailUI(doctorEmail);
        textDoctorDepartment.setText(department + " • Live Queue");

        buttonBack.setOnClickListener(v -> finish());
        if (buttonDeleteDoctor != null) {
            buttonDeleteDoctor.setOnClickListener(v -> showDeleteDoctorDialog());
        }

        View.OnClickListener addApptListener = v -> {
            Intent intent = new Intent(AdminDoctorQueueActivity.this, BookAppointmentActivity.class);
            intent.putExtra(Constants.EXTRA_DOCTOR_NAME, doctorName);
            intent.putExtra(Constants.EXTRA_DEPARTMENT, department);
            startActivity(intent);
        };

        buttonAddAppointment.setOnClickListener(addApptListener);
        buttonAddEmptyAppointment.setOnClickListener(addApptListener);

        recyclerViewQueue.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminQueueAdapter(currentQueue, this);
        recyclerViewQueue.setAdapter(adapter);
    }

    private void showDeleteDoctorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Doctor Account")
                .setMessage("Are you sure you want to permanently delete " + doctorName + "'s account" +
                        (doctorEmail.isEmpty() ? "" : " (" + doctorEmail + ")") +
                        "?\n\nThis will remove their profile, delete all their waiting queue appointments, and revoke their system access.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseHelper.deleteDoctorByAdmin(doctorName, doctorEmail, new FirebaseHelper.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminDoctorQueueActivity.this,
                                    doctorName + "'s account has been permanently deleted.",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminDoctorQueueActivity.this,
                                    "Failed to delete: " + errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDoctorEmailUI(String email) {
        if (email != null && !email.trim().isEmpty()) {
            doctorEmail = email.trim();
            textDoctorEmail.setText("✉ " + doctorEmail);
            textDoctorEmail.setVisibility(View.VISIBLE);
        } else {
            // Check local SQLite first
            if (firebaseHelper.getDb() != null) {
                String localEmail = firebaseHelper.getDb().getDoctorEmailByName(doctorName);
                if (localEmail != null && !localEmail.trim().isEmpty()) {
                    doctorEmail = localEmail.trim();
                    textDoctorEmail.setText("✉ " + doctorEmail);
                    textDoctorEmail.setVisibility(View.VISIBLE);
                    return;
                }
            }

            // Fallback: Query Firestore
            firebaseHelper.getDoctorEmail(doctorName, loadedEmail -> {
                if (loadedEmail != null && !loadedEmail.trim().isEmpty()) {
                    doctorEmail = loadedEmail.trim();
                    textDoctorEmail.setText("✉ " + doctorEmail);
                    textDoctorEmail.setVisibility(View.VISIBLE);
                } else {
                    textDoctorEmail.setText("✉ No registered email");
                    textDoctorEmail.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQueue();
    }

    private void loadQueue() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getDoctorAppointments(doctorName, new FirebaseHelper.QueueCallback() {
            @Override
            public void onQueueLoaded(List<Appointment> queue) {
                progressBar.setVisibility(View.GONE);
                currentQueue = QueueManager.sortQueue(queue);
                adapter.setAppointmentList(currentQueue);

                if (currentQueue.isEmpty()) {
                    layoutEmptyQueue.setVisibility(View.VISIBLE);
                    recyclerViewQueue.setVisibility(View.GONE);
                    textQueueSummary.setText("Queue is currently empty (0 waiting).");
                } else {
                    layoutEmptyQueue.setVisibility(View.GONE);
                    recyclerViewQueue.setVisibility(View.VISIBLE);

                    int emergencyCount = 0;
                    for (Appointment a : currentQueue) {
                        if (QueueManager.isEmergency(a)) emergencyCount++;
                    }

                    if (emergencyCount > 0) {
                        textQueueSummary.setText("🚨 " + emergencyCount + " Emergency active! (Preempts queue to index 0) • FCFS for others");
                    } else {
                        textQueueSummary.setText(currentQueue.size() + " patients in queue • Sorted by FCFS (First-Come, First-Served)");
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDoctorQueueActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onToggleEmergency(Appointment appointment, boolean makeEmergency) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.setAppointmentEmergency(appointment.getAppointmentId(), makeEmergency, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                if (makeEmergency) {
                    Toast.makeText(AdminDoctorQueueActivity.this,
                            "🚨 " + appointment.getPatientName() + " flagged as EMERGENCY! Moved to front of queue.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminDoctorQueueActivity.this,
                            "Removed emergency flag. Re-sorted by FCFS.",
                            Toast.LENGTH_SHORT).show();
                }
                loadQueue(); // Immediately reload and sort with QueueManager
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDoctorQueueActivity.this,
                        "Failed to update status: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

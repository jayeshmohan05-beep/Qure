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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.MainActivity;
import com.example.qure.R;
import com.example.qure.adapters.DoctorProfileAdapter;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.models.DoctorProfile;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminDashboardActivity.java — Central Administration & Doctor Queue Hub
 * ----------------------------------------------------------------------
 * Displays:
 *   1. Hospital Live Statistics (Users & Appointments).
 *   2. Available Doctor Profiles with active queue status.
 *   3. Clicking any doctor opens their live waiting queue (FCFS + Emergency).
 *   4. Direct "+ Add Appointment" button to book appointments.
 *   5. Access to the full Hospital Appointment Database.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private TextView textWelcome, textTotalUsers, textTotalAppointments, textNoDoctors;
    private CardView cardAllAppointments, cardManageUsers;
    private MaterialButton buttonAddAppointment;
    private RecyclerView recyclerViewDoctors;
    private ProgressBar progressDoctors;
    private ImageButton buttonLogout;

    private FirebaseHelper firebaseHelper;
    private DoctorProfileAdapter doctorAdapter;
    private List<DoctorProfile> doctorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        firebaseHelper = new FirebaseHelper();

        // Connect UI Elements
        textWelcome = findViewById(R.id.textWelcome);
        textTotalUsers = findViewById(R.id.textTotalUsers);
        textTotalAppointments = findViewById(R.id.textTotalAppointments);
        textNoDoctors = findViewById(R.id.textNoDoctors);

        buttonAddAppointment = findViewById(R.id.buttonAddAppointment);
        cardAllAppointments = findViewById(R.id.cardAllAppointments);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        buttonLogout = findViewById(R.id.buttonLogout);

        recyclerViewDoctors = findViewById(R.id.recyclerViewDoctors);
        progressDoctors = findViewById(R.id.progressDoctors);

        // Load Admin Name
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        String adminName = prefs.getString(Constants.PREF_USER_NAME, "Admin");
        textWelcome.setText("Welcome, " + adminName);

        // Set up Doctors RecyclerView
        recyclerViewDoctors.setLayoutManager(new LinearLayoutManager(this));
        doctorAdapter = new DoctorProfileAdapter(doctorList, profile -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminDoctorQueueActivity.class);
            intent.putExtra(Constants.EXTRA_DOCTOR_NAME, profile.getName());
            intent.putExtra(Constants.EXTRA_DEPARTMENT, profile.getDepartment());
            intent.putExtra(Constants.EXTRA_DOCTOR_EMAIL, profile.getEmail());
            startActivity(intent);
        });
        recyclerViewDoctors.setAdapter(doctorAdapter);

        // Button Clicks
        buttonLogout.setOnClickListener(v -> logout());

        buttonAddAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, BookAppointmentActivity.class);
            startActivity(intent);
        });

        cardAllAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AllAppointmentsActivity.class);
            startActivity(intent);
        });

        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminManageUsersActivity.class);
                startActivity(intent);
            });
        }

        // Load initial data
        loadStatistics();
        loadDoctorProfiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
        loadDoctorProfiles();
    }

    private void loadStatistics() {
        firebaseHelper.getSystemStatistics(new FirebaseHelper.StatsCallback() {
            @Override
            public void onStatsLoaded(int totalUsers, int totalAppointments) {
                textTotalUsers.setText(String.valueOf(totalUsers));
                textTotalAppointments.setText(String.valueOf(totalAppointments));
            }

            @Override
            public void onFailure(String errorMessage) {
                // Silently fallback to keep UI clean
            }
        });
    }

    private void loadDoctorProfiles() {
        progressDoctors.setVisibility(View.VISIBLE);
        firebaseHelper.getDoctorProfiles(new FirebaseHelper.DoctorProfilesCallback() {
            @Override
            public void onProfilesLoaded(List<DoctorProfile> profiles) {
                progressDoctors.setVisibility(View.GONE);
                doctorList = profiles;
                doctorAdapter.setDoctorList(doctorList);

                if (doctorList.isEmpty()) {
                    textNoDoctors.setVisibility(View.VISIBLE);
                    recyclerViewDoctors.setVisibility(View.GONE);
                } else {
                    textNoDoctors.setVisibility(View.GONE);
                    recyclerViewDoctors.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDoctors.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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

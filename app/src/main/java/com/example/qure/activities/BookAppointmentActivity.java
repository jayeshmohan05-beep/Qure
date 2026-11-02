package com.example.qure.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qure.R;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * BookAppointmentActivity.java — Book Appointment Screen
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   Handles the logic for the patient booking form.
 *   - Shows dropdown menus for Departments and dummy Doctors.
 *   - Shows a DatePicker popup calendar for selecting the date.
 *   - Validates all fields to ensure they aren't empty.
 *   - Uses FirebaseHelper to save the new appointment to Firestore.
 *
 * WHERE THIS FILE IS:
 *   app/src/main/java/com/example/qure/activities/BookAppointmentActivity.java
 * -------------------------------------------------------
 */
public class BookAppointmentActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout layoutPatientName, layoutDepartment, layoutDoctor, layoutDate, layoutSymptoms;
    private AutoCompleteTextView spinnerDepartment, spinnerDoctor;
    private TextInputEditText editPatientName, editDate, editSymptoms;
    private MaterialButton buttonBookAppointment, buttonCancel;

    private FirebaseHelper firebaseHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        firebaseHelper = new FirebaseHelper();

        // 1. Connect UI elements
        layoutPatientName = findViewById(R.id.layoutPatientName);
        layoutDepartment  = findViewById(R.id.layoutDepartment);
        layoutDoctor      = findViewById(R.id.layoutDoctor);
        layoutDate        = findViewById(R.id.layoutDate);
        layoutSymptoms    = findViewById(R.id.layoutSymptoms);

        editPatientName   = findViewById(R.id.editPatientName);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerDoctor     = findViewById(R.id.spinnerDoctor);
        editDate          = findViewById(R.id.editDate);
        editSymptoms      = findViewById(R.id.editSymptoms);

        buttonBookAppointment = findViewById(R.id.buttonBookAppointment);
        buttonCancel          = findViewById(R.id.buttonCancel);

        // Pre-fill patient name if logged in as Patient
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        String currentRole = prefs.getString(Constants.PREF_USER_ROLE, "");
        String currentUserName = prefs.getString(Constants.PREF_USER_NAME, "");
        if (Constants.ROLE_PATIENT.equalsIgnoreCase(currentRole) && !currentUserName.isEmpty()) {
            editPatientName.setText(currentUserName);
        }

        // 2. Set up loading dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Booking appointment...");
        progressDialog.setCancelable(false);

        // 3. Populate Dropdown Menus
        setupDropdowns();

        // Check for pre-filled doctor and department from intent
        String prefilledDoctor = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_NAME);
        String prefilledDept   = getIntent().getStringExtra(Constants.EXTRA_DEPARTMENT);
        if (prefilledDept != null && !prefilledDept.trim().isEmpty()) {
            spinnerDepartment.setText(prefilledDept, false);
            loadDoctorsForDepartment(prefilledDept);
        }
        if (prefilledDoctor != null && !prefilledDoctor.trim().isEmpty()) {
            spinnerDoctor.setText(prefilledDoctor, false);
        }

        // 4. Set up Date Picker
        editDate.setOnClickListener(v -> showDatePicker());

        // 5. Button Clicks
        buttonCancel.setOnClickListener(v -> finish()); // Just close the screen

        buttonBookAppointment.setOnClickListener(v -> handleBooking());
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentDept = spinnerDepartment.getText().toString().trim();
        loadDoctorsForDepartment(currentDept.isEmpty() ? null : currentDept);
    }

    private void setupDropdowns() {
        // Setup Department dropdown using constants
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, Constants.DEPARTMENTS);
        spinnerDepartment.setAdapter(deptAdapter);
        spinnerDepartment.setOnClickListener(v -> spinnerDepartment.showDropDown());

        // When a department is selected, filter doctors strictly to that department!
        spinnerDepartment.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDept = (String) parent.getItemAtPosition(position);
            spinnerDoctor.setText("", false); // Clear previous selection
            loadDoctorsForDepartment(selectedDept);
        });

        // Prompt patient to select department if they tap doctor first
        spinnerDoctor.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                String currentDept = spinnerDepartment.getText().toString().trim();
                if (currentDept.isEmpty()) {
                    Toast.makeText(BookAppointmentActivity.this, "Please select a Department first", Toast.LENGTH_SHORT).show();
                    spinnerDepartment.showDropDown();
                    return true;
                }
            }
            return false;
        });

        spinnerDoctor.setOnClickListener(v -> {
            String currentDept = spinnerDepartment.getText().toString().trim();
            if (currentDept.isEmpty()) {
                Toast.makeText(BookAppointmentActivity.this, "Please select a Department first", Toast.LENGTH_SHORT).show();
                spinnerDepartment.showDropDown();
            } else {
                spinnerDoctor.showDropDown();
            }
        });

        loadDoctorsForDepartment(null);
    }

    private void loadDoctorsForDepartment(String department) {
        // Dynamically load registered Doctors for the chosen department from SQLite & Cloud
        firebaseHelper.getDoctorsListByDepartment(department, new FirebaseHelper.DoctorListCallback() {
            @Override
            public void onDoctorsLoaded(List<String> doctors) {
                runOnUiThread(() -> {
                    ArrayAdapter<String> docAdapter = new ArrayAdapter<>(
                            BookAppointmentActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            doctors);
                    spinnerDoctor.setAdapter(docAdapter);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    List<String> fallback = new ArrayList<>();
                    if (department != null && !department.isEmpty()) {
                        fallback.add("Dr. Smith (" + department + ")");
                    } else {
                        fallback.add("Dr. Smith");
                    }
                    ArrayAdapter<String> docAdapter = new ArrayAdapter<>(
                            BookAppointmentActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            fallback);
                    spinnerDoctor.setAdapter(docAdapter);
                });
            }
        });
    }

    private void showDatePicker() {
        // Get current date to start the calendar at today
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show the date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Month is 0-indexed, so add 1
                    String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                            selectedYear, selectedMonth + 1, selectedDay);
                    editDate.setText(dateString);
                    layoutDate.setError(null); // clear any previous errors
                },
                year, month, day
        );
        
        // Optional: Prevent booking in the past
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        
        datePickerDialog.show();
    }

    private void handleBooking() {
        // Get input text
        String patientNameInput = editPatientName.getText().toString().trim();
        String department       = spinnerDepartment.getText().toString().trim();
        String doctor           = spinnerDoctor.getText().toString().trim();
        String date             = editDate.getText().toString().trim();
        String symptoms         = editSymptoms.getText().toString().trim();

        // Validate
        if (!validateInputs(patientNameInput, department, doctor, date, symptoms)) {
            return;
        }

        progressDialog.show();
        buttonBookAppointment.setEnabled(false);

        // Get current logged-in details safely
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        String patientName = patientNameInput.isEmpty()
                ? prefs.getString(Constants.PREF_USER_NAME, "Patient")
                : patientNameInput;

        String patientId = prefs.getString(Constants.PREF_USER_ID, null);
        if (patientId == null || patientId.isEmpty() || Constants.ROLE_ADMIN.equalsIgnoreCase(prefs.getString(Constants.PREF_USER_ROLE, ""))) {
            patientId = "pat_" + System.currentTimeMillis();
        }

        // Save appointment (SQLite instant save + Cloud background sync)
        firebaseHelper.bookAppointment(patientId, patientName, doctor, department, date, symptoms,
                new FirebaseHelper.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        
                        // 🔥 TRIGGER THE PUSH NOTIFICATION HERE 🔥
                        com.example.qure.utils.NotificationHelper.showNotification(
                                BookAppointmentActivity.this,
                                "Appointment Confirmed!",
                                "You have booked an appointment with " + doctor + " on " + date + ".",
                                Constants.NOTIFICATION_ID_APPOINTMENT
                        );

                        Toast.makeText(BookAppointmentActivity.this, 
                                "Appointment booked successfully!", Toast.LENGTH_LONG).show();
                        
                        // Close this screen and return to Dashboard
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressDialog.dismiss();
                        buttonBookAppointment.setEnabled(true);
                        Toast.makeText(BookAppointmentActivity.this, 
                                errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String patientName, String dept, String doc, String date, String symp) {
        layoutPatientName.setError(null);
        layoutDepartment.setError(null);
        layoutDoctor.setError(null);
        layoutDate.setError(null);
        layoutSymptoms.setError(null);

        if (TextUtils.isEmpty(patientName)) {
            layoutPatientName.setError("Please enter patient name");
            return false;
        }
        if (TextUtils.isEmpty(dept)) {
            layoutDepartment.setError("Please select a department");
            return false;
        }
        if (TextUtils.isEmpty(doc)) {
            layoutDoctor.setError("Please select a doctor");
            return false;
        }
        if (TextUtils.isEmpty(date)) {
            layoutDate.setError("Please select an appointment date");
            return false;
        }
        if (TextUtils.isEmpty(symp)) {
            layoutSymptoms.setError("Please describe symptoms");
            return false;
        }
        return true;
    }
}

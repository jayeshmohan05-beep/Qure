package com.example.qure.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.example.qure.models.Appointment;
import com.example.qure.models.DoctorProfile;
import com.example.qure.models.UserAccount;
import com.example.qure.queue.QueueManager;
import com.example.qure.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FirebaseHelper.java — Unified Cloud & Local SQLite Database Bridge
 * ------------------------------------------------------------------
 * Handles all database operations for QURE with dual-engine persistence:
 *   1. Local SQLite Database (qure.db) -> Instant UI responses (< 50ms), guaranteed local persistence.
 *   2. Cloud Firestore & Firebase Auth -> Cloud sync in the background without blocking the UI.
 *
 * Can be inspected live in Android Studio:
 *   View -> Tool Windows -> App Inspection -> Database Inspector -> qure.db
 */
public class FirebaseHelper {

    private static Context sContext;
    private DatabaseHelper mDbHelper;

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;

    // -------------------------------------------------------
    // Initialize application context for SQLite
    // -------------------------------------------------------
    public static void init(Context context) {
        if (context != null) {
            sContext = context.getApplicationContext();
            try {
                // Instantly initializes and creates the qure.db file on disk
                new DatabaseHelper(sContext).getWritableDatabase();
            } catch (Exception ignored) {}
        }
    }

    // -------------------------------------------------------
    // CALLBACK INTERFACES
    // -------------------------------------------------------
    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface RoleCallback {
        void onRoleReceived(String role, String name);
        void onFailure(String errorMessage);
    }

    public interface QueueCallback {
        void onQueueLoaded(List<Appointment> queue);
        void onFailure(String errorMessage);
    }

    public interface AppointmentCallback {
        void onAppointmentLoaded(Appointment appointment);
        void onFailure(String errorMessage);
    }

    public interface StatsCallback {
        void onStatsLoaded(int totalUsers, int totalAppointments);
        void onFailure(String errorMessage);
    }

    public interface DoctorListCallback {
        void onDoctorsLoaded(List<String> doctors);
        void onFailure(String errorMessage);
    }

    public interface DoctorProfilesCallback {
        void onProfilesLoaded(List<DoctorProfile> profiles);
        void onFailure(String errorMessage);
    }

    public interface EmailCallback {
        void onEmailLoaded(String email);
    }

    public interface UsersListCallback {
        void onUsersLoaded(List<UserAccount> users);
        void onFailure(String errorMessage);
    }

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------
    public FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        if (sContext != null) {
            mDbHelper = new DatabaseHelper(sContext);
        }
    }

    public FirebaseHelper(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        if (context != null) {
            sContext = context.getApplicationContext();
            mDbHelper = new DatabaseHelper(sContext);
        }
    }

    public DatabaseHelper getDb() {
        if (mDbHelper == null && sContext != null) {
            mDbHelper = new DatabaseHelper(sContext);
        }
        return mDbHelper;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void logoutUser() {
        try {
            mAuth.signOut();
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------
    // 1. User Registration (Saves to SQLite instantly + Cloud background sync)
    // -------------------------------------------------------
    public void registerUser(final String name, final String email,
                             final String password, final String role,
                             final AuthCallback callback) {
        registerUser(name, email, password, role, "", callback);
    }

    public void registerUser(final String name, final String email,
                             final String password, final String role,
                             final String department,
                             final AuthCallback callback) {

        final String localUserId = "user_" + UUID.randomUUID().toString().substring(0, 8);

        // 1. Save immediately to local SQLite
        if (getDb() != null) {
            getDb().insertUser(localUserId, name, email, password, role, department);
        }

        // 2. Persist in SharedPreferences so all screens immediately recognize the user
        if (sContext != null) {
            sContext.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(Constants.PREF_USER_ID, localUserId)
                    .putString(Constants.PREF_USER_NAME, name)
                    .putString(Constants.PREF_USER_EMAIL, email)
                    .putString(Constants.PREF_USER_ROLE, role)
                    .putString(Constants.FIELD_DEPARTMENT, department)
                    .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                    .apply();
        }

        // 3. Immediately notify UI of success! (Zero waiting, no frozen progress dialog)
        callback.onSuccess();

        // 4. Asynchronously create account in Firebase Cloud in the background
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String cloudUserId = (firebaseUser != null) ? firebaseUser.getUid() : localUserId;

                        // Update SQLite with Cloud UID
                        if (getDb() != null) {
                            getDb().insertUser(cloudUserId, name, email, password, role, department);
                        }

                        if (sContext != null) {
                            sContext.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
                                    .edit()
                                    .putString(Constants.PREF_USER_ID, cloudUserId)
                                    .apply();
                        }

                        Map<String, Object> userData = new HashMap<>();
                        userData.put(Constants.FIELD_USER_ID, cloudUserId);
                        userData.put(Constants.FIELD_NAME, name);
                        userData.put(Constants.FIELD_EMAIL, email);
                        userData.put(Constants.FIELD_ROLE, role);
                        userData.put(Constants.FIELD_DEPARTMENT, department);

                        mFirestore.collection(Constants.COLLECTION_USERS)
                                .document(cloudUserId)
                                .set(userData);
                    })
                    .addOnFailureListener(e -> {
                        // Offline or rules issue: silently ignore since local SQLite registration is already active
                    });
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------
    // 2. User Login (Authenticates via SQLite first, then Cloud)
    // -------------------------------------------------------
    public void loginUser(final String email, final String password,
                          final RoleCallback callback) {

        // Check local SQLite database first! Instant response (< 5ms)
        if (getDb() != null) {
            Cursor cursor = getDb().getUserByEmailPassword(email, password);
            if (cursor != null && cursor.moveToFirst()) {
                String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                int deptIndex = cursor.getColumnIndex(DatabaseHelper.COL_DEPARTMENT);
                String dept = (deptIndex != -1 && !cursor.isNull(deptIndex)) ? cursor.getString(deptIndex) : "";
                int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COL_PHONE);
                String phone = (phoneIndex != -1 && !cursor.isNull(phoneIndex)) ? cursor.getString(phoneIndex) : "";
                cursor.close();

                // Save to SharedPreferences
                if (sContext != null) {
                    sContext.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(Constants.PREF_USER_ID, userId)
                            .putString(Constants.PREF_USER_NAME, name)
                            .putString(Constants.PREF_USER_EMAIL, email)
                            .putString(Constants.PREF_USER_ROLE, role)
                            .putString(Constants.FIELD_DEPARTMENT, dept)
                            .putString(Constants.PREF_USER_PHONE, phone)
                            .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                            .apply();
                }

                callback.onRoleReceived(role, name);

                // Sign in Firebase in background if online
                try {
                    mAuth.signInWithEmailAndPassword(email, password);
                } catch (Exception ignored) {}
                return;
            }
            if (cursor != null) cursor.close();
        }

        // If not found in SQLite, attempt Firebase Cloud login
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            callback.onFailure("Login error. Please try again.");
                            return;
                        }
                        String userId = firebaseUser.getUid();
                        getUserData(userId, email, password, callback);
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(getFriendlyErrorMessage(e.getMessage()));
                    });
        } catch (Exception e) {
            callback.onFailure("Login failed: " + e.getMessage());
        }
    }

    public void getUserData(final String userId, final String email, final String password, final RoleCallback callback) {
        // Check SQLite
        if (getDb() != null) {
            Cursor cursor = getDb().getUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                int deptIndex = cursor.getColumnIndex(DatabaseHelper.COL_DEPARTMENT);
                String dept = (deptIndex != -1 && !cursor.isNull(deptIndex)) ? cursor.getString(deptIndex) : "";
                int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COL_PHONE);
                String phone = (phoneIndex != -1 && !cursor.isNull(phoneIndex)) ? cursor.getString(phoneIndex) : "";
                cursor.close();

                if (sContext != null) {
                    sContext.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(Constants.PREF_USER_ID, userId)
                            .putString(Constants.PREF_USER_NAME, name)
                            .putString(Constants.PREF_USER_EMAIL, email)
                            .putString(Constants.PREF_USER_ROLE, role)
                            .putString(Constants.FIELD_DEPARTMENT, dept)
                            .putString(Constants.PREF_USER_PHONE, phone)
                            .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                            .apply();
                }

                callback.onRoleReceived(role, name);
                return;
            }
            if (cursor != null) cursor.close();
        }

        // Query Firestore
        mFirestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString(Constants.FIELD_ROLE);
                        String name = document.getString(Constants.FIELD_NAME);
                        String dept = document.getString(Constants.FIELD_DEPARTMENT);
                        String phone = document.getString(Constants.FIELD_PHONE);
                        if (role != null && !role.isEmpty()) {
                            String displayName = (name != null) ? name : "User";
                            String doctorDept = (dept != null) ? dept : "";
                            String userPhone = (phone != null) ? phone : "";
                            // Cache in local SQLite
                            if (getDb() != null) {
                                getDb().insertUser(userId, displayName, email, password, role, doctorDept);
                                if (!userPhone.isEmpty()) {
                                    getDb().updateUserPhone(userId, userPhone);
                                }
                            }
                            if (sContext != null) {
                                sContext.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
                                        .edit()
                                        .putString(Constants.PREF_USER_ID, userId)
                                        .putString(Constants.PREF_USER_NAME, displayName)
                                        .putString(Constants.PREF_USER_EMAIL, email)
                                        .putString(Constants.PREF_USER_ROLE, role)
                                        .putString(Constants.FIELD_DEPARTMENT, doctorDept)
                                        .putString(Constants.PREF_USER_PHONE, userPhone)
                                        .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                                        .apply();
                            }
                            callback.onRoleReceived(role, displayName);
                        } else {
                            callback.onFailure("User role not found.");
                        }
                    } else {
                        callback.onFailure("User data not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Failed to load user data: " + e.getMessage()));
    }

    public void getUserData(final String userId, final RoleCallback callback) {
        getUserData(userId, "", "", callback);
    }

    public void updateUserPhone(final String userId, final String phone, final AuthCallback callback) {
        if (getDb() != null) {
            getDb().updateUserPhone(userId, phone);
        }
        if (sContext != null) {
            sContext.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(Constants.PREF_USER_PHONE, phone)
                    .apply();
        }
        callback.onSuccess();

        try {
            mFirestore.collection(Constants.COLLECTION_USERS)
                    .document(userId)
                    .update(Constants.FIELD_PHONE, phone);
        } catch (Exception ignored) {}
    }

    public void deleteUserAccount(final AuthCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "";

        if (sContext != null) {
            SharedPreferences prefs = sContext.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
            if (userId.isEmpty()) {
                userId = prefs.getString(Constants.PREF_USER_ID, "");
            }
            prefs.edit().clear().apply();
        }

        if (getDb() != null && !userId.isEmpty()) {
            getDb().deleteUser(userId);
        }

        if (!userId.isEmpty()) {
            try {
                mFirestore.collection(Constants.COLLECTION_USERS).document(userId).delete();
            } catch (Exception ignored) {}
        }

        if (currentUser != null) {
            currentUser.delete().addOnCompleteListener(task -> {
                callback.onSuccess();
            });
        } else {
            callback.onSuccess();
        }
    }

    // -------------------------------------------------------
    // 3. Book Appointment (Saves instantly to SQLite + Cloud sync)
    // -------------------------------------------------------
    public void bookAppointment(String patientId, String patientName, String doctorName,
                                String department, String date, String symptoms,
                                final AuthCallback callback) {

        String appointmentId = "appt_" + System.currentTimeMillis();
        int tokenNumber = (int) (Math.random() * 99) + 1;
        int estimatedTime = (int) (Math.random() * 15) + 5;

        Appointment appt = new Appointment(
                appointmentId, patientId, patientName, doctorName, department, date, symptoms, tokenNumber);
        appt.setStatus(Constants.STATUS_REGISTERED);
        appt.setPriority(Constants.PRIORITY_NORMAL);
        appt.setEstimatedConsultationTime(estimatedTime);

        // 1. Save directly into local SQLite IMMEDIATELY
        if (getDb() != null) {
            getDb().insertAppointment(appt);
        }

        // 2. Immediately notify UI! (< 10ms, no spinning wheel, no freeze)
        callback.onSuccess();

        // 3. Sync to Firestore in background
        try {
            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put(Constants.FIELD_APPOINTMENT_ID, appointmentId);
            appointmentData.put(Constants.FIELD_PATIENT_ID, patientId);
            appointmentData.put(Constants.FIELD_PATIENT_NAME, patientName);
            appointmentData.put(Constants.FIELD_DOCTOR_NAME, doctorName);
            appointmentData.put(Constants.FIELD_DEPARTMENT, department);
            appointmentData.put(Constants.FIELD_APPOINTMENT_DATE, date);
            appointmentData.put(Constants.FIELD_SYMPTOMS, symptoms);
            appointmentData.put(Constants.FIELD_TOKEN_NUMBER, tokenNumber);
            appointmentData.put(Constants.FIELD_STATUS, Constants.STATUS_REGISTERED);
            appointmentData.put(Constants.FIELD_PRIORITY, Constants.PRIORITY_NORMAL);
            appointmentData.put(Constants.FIELD_ARRIVAL_TIME, "");
            appointmentData.put(Constants.FIELD_ESTIMATED_CONSULTATION_TIME, estimatedTime);

            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .document(appointmentId)
                    .set(appointmentData);
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------
    // 4. Get Active Appointment (SQLite first, then Cloud)
    // -------------------------------------------------------
    public void getActiveAppointment(String patientId, String patientName, final AppointmentCallback callback) {
        // Check SQLite first
        if (getDb() != null) {
            Appointment localAppt = getDb().getActiveAppointment(patientId, patientName);
            if (localAppt != null) {
                callback.onAppointmentLoaded(localAppt);
                return;
            }
        }

        // Query Firestore if not found locally
        try {
            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Appointment appointment = doc.toObject(Appointment.class);
                            if (appointment != null && getDb() != null) {
                                getDb().insertAppointment(appointment);
                            }
                        }
                        if (getDb() != null) {
                            callback.onAppointmentLoaded(getDb().getActiveAppointment(patientId, patientName));
                        } else {
                            callback.onAppointmentLoaded(null);
                        }
                    })
                    .addOnFailureListener(e -> callback.onAppointmentLoaded(null));
        } catch (Exception e) {
            callback.onAppointmentLoaded(null);
        }
    }

    public void getActiveAppointment(String patientId, final AppointmentCallback callback) {
        getActiveAppointment(patientId, null, callback);
    }

    // -------------------------------------------------------
    // 5. Check-In Appointment (Updates SQLite + Cloud)
    // -------------------------------------------------------
    public void checkInAppointment(String appointmentId, final AuthCallback callback) {
        String arrivalTime = String.valueOf(System.currentTimeMillis());

        if (getDb() != null) {
            getDb().checkInAppointment(appointmentId, arrivalTime);
        }

        // Complete instantly
        callback.onSuccess();

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.FIELD_STATUS, Constants.STATUS_WAITING);
            updates.put(Constants.FIELD_ARRIVAL_TIME, arrivalTime);

            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .document(appointmentId)
                    .update(updates);
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------
    // 6. Doctor Queue (SQLite first, Firestore sync)
    // -------------------------------------------------------
    public void setAppointmentEmergency(final String appointmentId, final boolean isEmergency, final AuthCallback callback) {
        if (getDb() != null) {
            getDb().setAppointmentEmergency(appointmentId, isEmergency);
        }
        callback.onSuccess();

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.FIELD_PRIORITY, isEmergency ? Constants.PRIORITY_EMERGENCY : Constants.PRIORITY_NORMAL);
            updates.put(Constants.FIELD_STATUS, isEmergency ? Constants.STATUS_EMERGENCY : Constants.STATUS_WAITING);

            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .document(appointmentId)
                    .update(updates);
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------
    // 6. Doctor Queue (SQLite first, Firestore sync with FCFS + Emergency sort)
    // -------------------------------------------------------
    public void getDoctorQueue(String doctorName, final QueueCallback callback) {
        if (getDb() != null) {
            List<Appointment> localQueue = getDb().getDoctorWaitingQueue(doctorName);
            if (!localQueue.isEmpty()) {
                callback.onQueueLoaded(QueueManager.sortQueue(new ArrayList<>(localQueue)));
            }
        }

        try {
            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Appointment appt = doc.toObject(Appointment.class);
                            if (appt != null && getDb() != null) {
                                getDb().insertAppointment(appt);
                            }
                        }
                        List<Appointment> updatedQueue = (getDb() != null)
                                ? getDb().getDoctorWaitingQueue(doctorName)
                                : new ArrayList<>();
                        callback.onQueueLoaded(QueueManager.sortQueue(updatedQueue));
                    })
                    .addOnFailureListener(e -> {
                        if (getDb() != null) {
                            callback.onQueueLoaded(QueueManager.sortQueue(getDb().getDoctorWaitingQueue(doctorName)));
                        } else {
                            callback.onFailure("Failed to load queue: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            if (getDb() != null) {
                callback.onQueueLoaded(QueueManager.sortQueue(getDb().getDoctorWaitingQueue(doctorName)));
            } else {
                callback.onFailure("Failed to load queue.");
            }
        }
    }

    public void getDoctorAppointments(String doctorName, final QueueCallback callback) {
        if (getDb() != null) {
            List<Appointment> localList = getDb().getDoctorAllActiveAppointments(doctorName);
            if (!localList.isEmpty()) {
                callback.onQueueLoaded(QueueManager.sortQueue(new ArrayList<>(localList)));
            }
        }

        try {
            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Appointment appt = doc.toObject(Appointment.class);
                            if (appt != null && getDb() != null) {
                                getDb().insertAppointment(appt);
                            }
                        }
                        List<Appointment> updatedList = (getDb() != null)
                                ? getDb().getDoctorAllActiveAppointments(doctorName)
                                : new ArrayList<>();
                        callback.onQueueLoaded(QueueManager.sortQueue(updatedList));
                    })
                    .addOnFailureListener(e -> {
                        if (getDb() != null) {
                            callback.onQueueLoaded(QueueManager.sortQueue(getDb().getDoctorAllActiveAppointments(doctorName)));
                        } else {
                            callback.onFailure("Failed to load doctor appointments.");
                        }
                    });
        } catch (Exception e) {
            if (getDb() != null) {
                callback.onQueueLoaded(QueueManager.sortQueue(getDb().getDoctorAllActiveAppointments(doctorName)));
            } else {
                callback.onFailure("Failed to load doctor appointments.");
            }
        }
    }

    public void getDoctorProfiles(final DoctorProfilesCallback callback) {
        if (getDb() != null) {
            List<DoctorProfile> localProfiles = getDb().getAllDoctorProfiles();
            if (!localProfiles.isEmpty()) {
                callback.onProfilesLoaded(new ArrayList<>(localProfiles));
            }
        }

        try {
            mFirestore.collection(Constants.COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String role = doc.getString(Constants.FIELD_ROLE);
                            String name = doc.getString(Constants.FIELD_NAME);
                            String dept = doc.getString(Constants.FIELD_DEPARTMENT);
                            String email = doc.getString(Constants.FIELD_EMAIL);

                            boolean isDoctor = (role != null && role.equalsIgnoreCase(Constants.ROLE_DOCTOR)) ||
                                    (name != null && (name.toLowerCase().startsWith("dr.") || name.toLowerCase().startsWith("dr ")));

                            if (isDoctor && name != null && !name.trim().isEmpty()) {
                                name = name.trim();
                                if (!name.startsWith("Dr. ") && !name.startsWith("Dr.")) {
                                    name = "Dr. " + name;
                                }
                                if (getDb() != null) {
                                    getDb().insertUser(doc.getId(), name, email != null ? email : "", "", Constants.ROLE_DOCTOR, dept != null ? dept : "General Medicine");
                                }
                            }
                        }

                        mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                                .get()
                                .addOnSuccessListener(apptSnaps -> {
                                    for (DocumentSnapshot adoc : apptSnaps) {
                                        Appointment appt = adoc.toObject(Appointment.class);
                                        if (appt != null && getDb() != null) {
                                            getDb().insertAppointment(appt);
                                        }
                                    }
                                    List<DoctorProfile> updated = (getDb() != null) ? getDb().getAllDoctorProfiles() : new ArrayList<>();
                                    callback.onProfilesLoaded(updated);
                                })
                                .addOnFailureListener(e -> {
                                    List<DoctorProfile> updated = (getDb() != null) ? getDb().getAllDoctorProfiles() : new ArrayList<>();
                                    callback.onProfilesLoaded(updated);
                                });
                    })
                    .addOnFailureListener(e -> {
                        if (getDb() != null) {
                            callback.onProfilesLoaded(getDb().getAllDoctorProfiles());
                        } else {
                            callback.onFailure("Failed to load doctor profiles: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            if (getDb() != null) {
                callback.onProfilesLoaded(getDb().getAllDoctorProfiles());
            } else {
                callback.onFailure("Failed to load doctor profiles.");
            }
        }
    }

    public void getDoctorEmail(String doctorName, final EmailCallback callback) {
        if (doctorName == null || doctorName.trim().isEmpty()) {
            callback.onEmailLoaded("");
            return;
        }

        // 1. Check local SQLite first
        if (getDb() != null) {
            String localEmail = getDb().getDoctorEmailByName(doctorName);
            if (localEmail != null && !localEmail.trim().isEmpty()) {
                callback.onEmailLoaded(localEmail.trim());
                return;
            }
        }

        // 2. Query Firestore users collection
        try {
            final String cleanName = doctorName.replace("Dr. ", "").replace("Dr.", "").trim();
            mFirestore.collection(Constants.COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String name = doc.getString(Constants.FIELD_NAME);
                            String email = doc.getString(Constants.FIELD_EMAIL);
                            if (name != null && email != null && !email.trim().isEmpty()) {
                                String cleanDocName = name.replace("Dr. ", "").replace("Dr.", "").trim();
                                if (cleanDocName.equalsIgnoreCase(cleanName) || name.equalsIgnoreCase(doctorName)) {
                                    if (getDb() != null) {
                                        getDb().insertUser(doc.getId(), name, email, "", Constants.ROLE_DOCTOR, doc.getString(Constants.FIELD_DEPARTMENT));
                                    }
                                    callback.onEmailLoaded(email.trim());
                                    return;
                                }
                            }
                        }
                        callback.onEmailLoaded("");
                    })
                    .addOnFailureListener(e -> callback.onEmailLoaded(""));
        } catch (Exception e) {
            callback.onEmailLoaded("");
        }
    }

    public void getAllUsers(final UsersListCallback callback) {
        if (getDb() != null) {
            List<UserAccount> localList = getDb().getAllUsers();
            if (!localList.isEmpty()) {
                callback.onUsersLoaded(new ArrayList<>(localList));
            }
        }

        try {
            mFirestore.collection(Constants.COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String uId = doc.getId();
                            String name = doc.getString(Constants.FIELD_NAME);
                            String email = doc.getString(Constants.FIELD_EMAIL);
                            String role = doc.getString(Constants.FIELD_ROLE);
                            String dept = doc.getString(Constants.FIELD_DEPARTMENT);
                            String phone = doc.getString(Constants.FIELD_PHONE);

                            if (name != null && getDb() != null) {
                                getDb().insertUser(uId, name, email != null ? email : "", "", role != null ? role : Constants.ROLE_PATIENT, dept != null ? dept : "");
                                if (phone != null && !phone.isEmpty()) {
                                    getDb().updateUserPhone(uId, phone);
                                }
                            }
                        }
                        if (getDb() != null) {
                            callback.onUsersLoaded(getDb().getAllUsers());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getDb() != null) {
                            callback.onUsersLoaded(getDb().getAllUsers());
                        } else {
                            callback.onFailure("Failed to load users: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            if (getDb() != null) {
                callback.onUsersLoaded(getDb().getAllUsers());
            } else {
                callback.onFailure("Failed to load users.");
            }
        }
    }

    public void deleteUserByAdmin(final UserAccount user, final AuthCallback callback) {
        if (user == null) {
            callback.onFailure("Invalid user");
            return;
        }

        // 1. Delete from SQLite immediately
        if (getDb() != null) {
            getDb().deleteUserAccountByAdmin(user.getUserId(), user.getName(), user.getEmail(), user.getRole());
        }

        // 2. Return success to UI instantly
        callback.onSuccess();

        // 3. Delete in background from Firestore
        try {
            if (user.getUserId() != null && !user.getUserId().isEmpty()) {
                mFirestore.collection(Constants.COLLECTION_USERS).document(user.getUserId()).delete();
            }

            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                mFirestore.collection(Constants.COLLECTION_USERS)
                        .whereEqualTo(Constants.FIELD_EMAIL, user.getEmail().trim())
                        .get()
                        .addOnSuccessListener(snaps -> {
                            for (DocumentSnapshot d : snaps) {
                                d.getReference().delete();
                            }
                        });
            }

            if (Constants.ROLE_DOCTOR.equalsIgnoreCase(user.getRole()) || (user.getName() != null && user.getName().toLowerCase().startsWith("dr"))) {
                mFirestore.collection(Constants.COLLECTION_DOCTORS)
                        .whereEqualTo(Constants.FIELD_NAME, user.getName())
                        .get()
                        .addOnSuccessListener(snaps -> {
                            for (DocumentSnapshot d : snaps) {
                                d.getReference().delete();
                            }
                        });

                mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                        .whereEqualTo(Constants.FIELD_DOCTOR_NAME, user.getName())
                        .get()
                        .addOnSuccessListener(snaps -> {
                            for (DocumentSnapshot d : snaps) {
                                d.getReference().delete();
                            }
                        });
            } else {
                if (user.getUserId() != null && !user.getUserId().isEmpty()) {
                    mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                            .whereEqualTo(Constants.FIELD_PATIENT_ID, user.getUserId())
                            .get()
                            .addOnSuccessListener(snaps -> {
                                for (DocumentSnapshot d : snaps) {
                                    d.getReference().delete();
                                }
                            });
                }
            }
        } catch (Exception ignored) {}
    }

    public void deleteDoctorByAdmin(final String doctorName, final String doctorEmail, final AuthCallback callback) {
        UserAccount doctorAccount = new UserAccount("", doctorName, doctorEmail, "", Constants.ROLE_DOCTOR, "");
        deleteUserByAdmin(doctorAccount, callback);
    }

    // -------------------------------------------------------
    // 7. Doctor Current Patient (SQLite first, then Cloud)
    // -------------------------------------------------------
    public void getDoctorCurrentPatient(String doctorName, final AppointmentCallback callback) {
        if (getDb() != null) {
            Appointment localAppt = getDb().getDoctorCurrentPatient(doctorName);
            if (localAppt != null) {
                callback.onAppointmentLoaded(localAppt);
                return;
            }
        }

        try {
            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .whereEqualTo(Constants.FIELD_STATUS, Constants.STATUS_IN_CONSULTATION)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Appointment appt = doc.toObject(Appointment.class);
                            if (appt != null && getDb() != null) {
                                getDb().insertAppointment(appt);
                            }
                        }
                        if (getDb() != null) {
                            callback.onAppointmentLoaded(getDb().getDoctorCurrentPatient(doctorName));
                        } else if (!queryDocumentSnapshots.isEmpty()) {
                            callback.onAppointmentLoaded(queryDocumentSnapshots.getDocuments().get(0).toObject(Appointment.class));
                        } else {
                            callback.onAppointmentLoaded(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getDb() != null) {
                            callback.onAppointmentLoaded(getDb().getDoctorCurrentPatient(doctorName));
                        } else {
                            callback.onFailure("Failed to check patient: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            if (getDb() != null) {
                callback.onAppointmentLoaded(getDb().getDoctorCurrentPatient(doctorName));
            } else {
                callback.onAppointmentLoaded(null);
            }
        }
    }

    // -------------------------------------------------------
    // 8. Start / Complete Consultation (Updates SQLite + Cloud)
    // -------------------------------------------------------
    public void startConsultation(String appointmentId, final AuthCallback callback) {
        if (getDb() != null) {
            getDb().updateAppointmentStatus(appointmentId, Constants.STATUS_IN_CONSULTATION);
        }
        callback.onSuccess();

        try {
            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .document(appointmentId)
                    .update(Constants.FIELD_STATUS, Constants.STATUS_IN_CONSULTATION);
        } catch (Exception ignored) {}
    }

    public void completeConsultation(String appointmentId, final AuthCallback callback) {
        if (getDb() != null) {
            getDb().updateAppointmentStatus(appointmentId, Constants.STATUS_COMPLETED);
        }
        callback.onSuccess();

        try {
            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .document(appointmentId)
                    .update(Constants.FIELD_STATUS, Constants.STATUS_COMPLETED);
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------
    // 9. Admin Statistics (SQLite first, then Cloud)
    // -------------------------------------------------------
    public void getSystemStatistics(final StatsCallback callback) {
        if (getDb() != null) {
            int userCount = getDb().getUserCount();
            int apptCount = getDb().getAppointmentCount();
            if (userCount > 0 || apptCount > 0) {
                callback.onStatsLoaded(userCount, apptCount);
                return;
            }
        }

        try {
            mFirestore.collection(Constants.COLLECTION_USERS).get().addOnSuccessListener(userSnap -> {
                int userCount = userSnap.size();
                mFirestore.collection(Constants.COLLECTION_APPOINTMENTS).get().addOnSuccessListener(apptSnap -> {
                    int apptCount = apptSnap.size();
                    callback.onStatsLoaded(userCount, apptCount);
                }).addOnFailureListener(e -> {
                    if (getDb() != null) {
                        callback.onStatsLoaded(getDb().getUserCount(), getDb().getAppointmentCount());
                    } else {
                        callback.onFailure("Failed to load appointment stats.");
                    }
                });
            }).addOnFailureListener(e -> {
                if (getDb() != null) {
                    callback.onStatsLoaded(getDb().getUserCount(), getDb().getAppointmentCount());
                } else {
                    callback.onFailure("Failed to load user stats.");
                }
            });
        } catch (Exception e) {
            if (getDb() != null) {
                callback.onStatsLoaded(getDb().getUserCount(), getDb().getAppointmentCount());
            } else {
                callback.onFailure("Failed to load stats.");
            }
        }
    }

    // -------------------------------------------------------
    // 10. Master Database View (SQLite first, then Cloud)
    // -------------------------------------------------------
    public void getAllAppointments(final QueueCallback callback) {
        if (getDb() != null) {
            List<Appointment> list = getDb().getAllAppointments();
            if (!list.isEmpty()) {
                callback.onQueueLoaded(list);
                return;
            }
        }

        try {
            mFirestore.collection(Constants.COLLECTION_APPOINTMENTS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Appointment> list = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Appointment appt = doc.toObject(Appointment.class);
                            if (appt != null) {
                                list.add(appt);
                                if (getDb() != null) {
                                    getDb().insertAppointment(appt);
                                }
                            }
                        }
                        if (list.isEmpty() && getDb() != null) {
                            list = getDb().getAllAppointments();
                        }
                        callback.onQueueLoaded(list);
                    })
                    .addOnFailureListener(e -> {
                        if (getDb() != null) {
                            callback.onQueueLoaded(getDb().getAllAppointments());
                        } else {
                            callback.onFailure("Failed to load database: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            if (getDb() != null) {
                callback.onQueueLoaded(getDb().getAllAppointments());
            } else {
                callback.onFailure("Failed to load database.");
            }
        }
    }

    // -------------------------------------------------------
    // 11. Dynamic Doctor List by Department (SQLite + Cloud)
    // -------------------------------------------------------
    public void getDoctorsList(final DoctorListCallback callback) {
        getDoctorsListByDepartment(null, callback);
    }

    public void getDoctorsListByDepartment(final String department, final DoctorListCallback callback) {
        final List<String> doctorList = new ArrayList<>();

        // 1. First get local SQLite doctors filtered by department (instant response)
        if (getDb() != null) {
            doctorList.addAll(getDb().getDoctorNamesByDepartment(department));
        }

        // Emit local doctors immediately so the UI is never blank
        if (!doctorList.isEmpty()) {
            callback.onDoctorsLoaded(new ArrayList<>(doctorList));
        }

        // 2. In parallel, fetch from Cloud Firestore to discover any doctors
        try {
            mFirestore.collection(Constants.COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String role = doc.getString(Constants.FIELD_ROLE);
                            String name = doc.getString(Constants.FIELD_NAME);
                            String docDept = doc.getString(Constants.FIELD_DEPARTMENT);
                            String email = doc.getString(Constants.FIELD_EMAIL);

                            boolean isDoctor = (role != null && role.equalsIgnoreCase(Constants.ROLE_DOCTOR)) ||
                                    (name != null && (name.toLowerCase().startsWith("dr.") || name.toLowerCase().startsWith("dr ")));

                            if (isDoctor && name != null && !name.trim().isEmpty()) {
                                name = name.trim();
                                if (!name.startsWith("Dr. ") && !name.startsWith("Dr.")) {
                                    name = "Dr. " + name;
                                }

                                boolean matchesDept = (department == null || department.trim().isEmpty() ||
                                        department.equalsIgnoreCase("All") ||
                                        docDept == null || docDept.trim().isEmpty() ||
                                        docDept.equalsIgnoreCase(department));

                                if (matchesDept && !doctorList.contains(name)) {
                                    doctorList.add(name);
                                }

                                // Cache in local SQLite for future offline use
                                if (getDb() != null) {
                                    getDb().insertUser(doc.getId(), name, email != null ? email : "", "", Constants.ROLE_DOCTOR, docDept != null ? docDept : "");
                                }
                            }
                        }

                        // Also check doctors collection
                        mFirestore.collection(Constants.COLLECTION_DOCTORS)
                                .get()
                                .addOnSuccessListener(docSnaps -> {
                                    for (DocumentSnapshot d : docSnaps) {
                                        String dName = d.getString(Constants.FIELD_NAME);
                                        if (dName == null) dName = d.getString("doctorName");
                                        String dDept = d.getString(Constants.FIELD_DEPARTMENT);

                                        boolean matchesDept = (department == null || department.trim().isEmpty() ||
                                                department.equalsIgnoreCase("All") ||
                                                dDept == null || dDept.trim().isEmpty() ||
                                                dDept.equalsIgnoreCase(department));

                                        if (dName != null && !dName.trim().isEmpty() && matchesDept) {
                                            dName = dName.trim();
                                            if (!dName.startsWith("Dr. ") && !dName.startsWith("Dr.")) {
                                                dName = "Dr. " + dName;
                                            }
                                            if (!doctorList.contains(dName)) {
                                                doctorList.add(dName);
                                            }
                                        }
                                    }

                                    if (doctorList.isEmpty()) {
                                        if (department != null && !department.isEmpty()) {
                                            doctorList.add("Dr. Smith (" + department + ")");
                                        } else {
                                            doctorList.add("Dr. Smith");
                                        }
                                    }
                                    callback.onDoctorsLoaded(new ArrayList<>(doctorList));
                                })
                                .addOnFailureListener(e -> {
                                    if (doctorList.isEmpty()) {
                                        doctorList.add("Dr. Smith");
                                    }
                                    callback.onDoctorsLoaded(new ArrayList<>(doctorList));
                                });
                    })
                    .addOnFailureListener(e -> {
                        if (doctorList.isEmpty()) {
                            doctorList.add("Dr. Smith");
                            callback.onDoctorsLoaded(new ArrayList<>(doctorList));
                        }
                    });
        } catch (Exception e) {
            if (doctorList.isEmpty()) {
                doctorList.add("Dr. Smith");
                callback.onDoctorsLoaded(new ArrayList<>(doctorList));
            }
        }
    }

    private String getFriendlyErrorMessage(String firebaseError) {
        if (firebaseError == null) return "Something went wrong. Please try again.";
        if (firebaseError.contains("email address is already in use")) return "This email is already registered.";
        if (firebaseError.contains("badly formatted")) return "Please enter a valid email address.";
        if (firebaseError.contains("password is invalid") || firebaseError.contains("no user record")) return "Incorrect email or password.";
        if (firebaseError.contains("network error") || firebaseError.contains("network")) return "Network error. Please check your connection.";
        return "Error: " + firebaseError;
    }
}

package com.example.qure.utils;

/**
 * Constants.java
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   This file stores all constant values used throughout
 *   the QURE app. Instead of typing "patients" or "Waiting"
 *   everywhere, we define them once here and reuse them.
 *   This prevents spelling mistakes and makes changes easy.
 *
 * WHERE THIS FILE IS:
 *   app/src/main/java/com/example/qure/utils/Constants.java
 *
 * CONNECTS TO:
 *   Every other Java file in the project uses these constants.
 * -------------------------------------------------------
 */
public class Constants {

    // =====================================================
    // FIRESTORE COLLECTION NAMES
    // These are the names of the main database collections
    // in Firebase Firestore.
    // =====================================================
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_PATIENTS = "patients";
    public static final String COLLECTION_DOCTORS = "doctors";
    public static final String COLLECTION_APPOINTMENTS = "appointments";
    public static final String COLLECTION_REPORTS = "reports";

    // =====================================================
    // FIRESTORE FIELD NAMES - USER
    // These are the field names inside each user document.
    // =====================================================
    public static final String FIELD_NAME = "name";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_PHONE = "phone";

    // =====================================================
    // FIRESTORE FIELD NAMES - APPOINTMENT
    // =====================================================
    public static final String FIELD_APPOINTMENT_ID = "appointmentId";
    public static final String FIELD_PATIENT_ID = "patientId";
    public static final String FIELD_DOCTOR_ID = "doctorId";
    public static final String FIELD_DEPARTMENT = "department";
    public static final String FIELD_APPOINTMENT_DATE = "appointmentDate";
    public static final String FIELD_SYMPTOMS = "symptoms";
    public static final String FIELD_TOKEN_NUMBER = "tokenNumber";
    public static final String FIELD_ARRIVAL_TIME = "arrivalTime";
    public static final String FIELD_ESTIMATED_CONSULTATION_TIME = "estimatedConsultationTime";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_PATIENT_NAME = "patientName";
    public static final String FIELD_DOCTOR_NAME = "doctorName";

    // =====================================================
    // FIRESTORE FIELD NAMES - REPORT
    // =====================================================
    public static final String FIELD_REPORT_ID = "reportId";
    public static final String FIELD_FILE_NAME = "fileName";
    public static final String FIELD_FILE_URL = "fileUrl";
    public static final String FIELD_UPLOAD_DATE = "uploadDate";

    // =====================================================
    // USER ROLES
    // These are the three roles a user can have.
    // Must match exactly what is stored in Firestore.
    // =====================================================
    public static final String ROLE_PATIENT = "Patient";
    public static final String ROLE_DOCTOR = "Doctor";
    public static final String ROLE_ADMIN = "Admin";

    // =====================================================
    // APPOINTMENT / QUEUE STATUS VALUES
    // These are the possible statuses for an appointment.
    // Must match exactly what is stored in Firestore.
    // =====================================================
    public static final String STATUS_REGISTERED = "Registered";
    public static final String STATUS_WAITING = "Waiting";
    public static final String STATUS_IN_CONSULTATION = "In Consultation";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_EMERGENCY = "Emergency";

    // =====================================================
    // PRIORITY LEVELS
    // Lower number = higher priority (emergency first)
    // 1 = Emergency, 2 = Critical, 3 = Urgent, 4 = Normal
    // =====================================================
    public static final int PRIORITY_EMERGENCY = 1;
    public static final int PRIORITY_CRITICAL = 2;
    public static final int PRIORITY_URGENT = 3;
    public static final int PRIORITY_NORMAL = 4;

    // =====================================================
    // DEPARTMENT NAMES
    // List of hospital departments for the appointment form.
    // =====================================================
    public static final String[] DEPARTMENTS = {
            "General Medicine",
            "Cardiology",
            "Orthopedics",
            "Pediatrics",
            "Neurology",
            "Gynecology",
            "Dermatology",
            "Ophthalmology",
            "ENT",
            "Psychiatry",
            "Emergency"
    };

    // =====================================================
    // SHARED PREFERENCES KEYS
    // These are keys used to store simple data locally
    // on the device using SharedPreferences.
    // =====================================================
    public static final String PREF_FILE_NAME = "qure_prefs";
    public static final String PREF_USER_ID = "pref_user_id";
    public static final String PREF_USER_NAME = "pref_user_name";
    public static final String PREF_USER_EMAIL = "pref_user_email";
    public static final String PREF_USER_ROLE = "pref_user_role";
    public static final String PREF_DEPARTMENT = "department";
    public static final String PREF_USER_PHONE = "pref_user_phone";
    public static final String PREF_IS_LOGGED_IN = "pref_is_logged_in";

    // =====================================================
    // INTENT EXTRA KEYS
    // Used when passing data between activities.
    // Example: intent.putExtra(Constants.EXTRA_USER_ID, userId)
    // =====================================================
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USER_NAME = "extra_user_name";
    public static final String EXTRA_USER_ROLE = "extra_user_role";
    public static final String EXTRA_APPOINTMENT_ID = "extra_appointment_id";
    public static final String EXTRA_PATIENT_ID = "extra_patient_id";
    public static final String EXTRA_DOCTOR_NAME = "extra_doctor_name";
    public static final String EXTRA_DEPARTMENT = "extra_department";
    public static final String EXTRA_DOCTOR_EMAIL = "extra_doctor_email";

    // =====================================================
    // QUEUE SCHEDULING ALGORITHM (FCFS with Emergency Priority)
    // =====================================================
    public static final String ALGO_FCFS = "FCFS";

    // =====================================================
    // NOTIFICATION
    // =====================================================
    public static final String CHANNEL_ID = "qure_notifications";
    public static final String CHANNEL_NAME = "QURE Notifications";
    public static final int NOTIFICATION_ID_APPOINTMENT = 1001;
    public static final int NOTIFICATION_ID_QUEUE = 1002;
    public static final int NOTIFICATION_ID_CHECKIN = 1003;

    // =====================================================
    // FIREBASE STORAGE PATHS
    // =====================================================
    public static final String STORAGE_REPORTS = "medical_reports/";

    // =====================================================
    // ESTIMATED TIME PER CONSULTATION (in minutes)
    // Used to calculate estimated waiting times.
    // =====================================================
    public static final int AVG_CONSULTATION_TIME_MINUTES = 15;
}

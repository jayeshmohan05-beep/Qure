package com.example.qure.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.qure.models.Appointment;
import com.example.qure.models.DoctorProfile;
import com.example.qure.models.Report;
import com.example.qure.models.UserAccount;
import com.example.qure.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper.java — Local SQLite Database for QURE
 * ----------------------------------------------------
 * Stores all Users, Appointments, and Reports locally on the device.
 * Operates offline without needing internet or cloud configuration.
 *
 * Can be inspected live in Android Studio via:
 *   View -> Tool Windows -> App Inspection -> Database Inspector (qure.db)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "qure.db";
    private static final int DATABASE_VERSION = 1;

    // Table: Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_NAME = "name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";
    public static final String COL_DEPARTMENT = "department";
    public static final String COL_PHONE = "phone";

    // Table: Appointments
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String COL_APPT_ID = "appointment_id";
    public static final String COL_PATIENT_ID = "patient_id";
    public static final String COL_PATIENT_NAME = "patient_name";
    public static final String COL_DOCTOR_NAME = "doctor_name";
    public static final String COL_DATE = "appointment_date";
    public static final String COL_SYMPTOMS = "symptoms";
    public static final String COL_TOKEN = "token_number";
    public static final String COL_STATUS = "status";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_ARRIVAL_TIME = "arrival_time";
    public static final String COL_ESTIMATED_TIME = "estimated_consultation_time";

    // Table: Reports
    public static final String TABLE_REPORTS = "reports";
    public static final String COL_REPORT_ID = "report_id";
    public static final String COL_FILE_NAME = "file_name";
    public static final String COL_FILE_URL = "file_url";
    public static final String COL_UPLOAD_DATE = "upload_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_ID + " TEXT UNIQUE, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_ROLE + " TEXT, " +
                COL_DEPARTMENT + " TEXT, " +
                COL_PHONE + " TEXT)";
        db.execSQL(createUsers);

        // Create Appointments Table
        String createAppointments = "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_APPT_ID + " TEXT UNIQUE, " +
                COL_PATIENT_ID + " TEXT, " +
                COL_PATIENT_NAME + " TEXT, " +
                COL_DOCTOR_NAME + " TEXT, " +
                COL_DEPARTMENT + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_SYMPTOMS + " TEXT, " +
                COL_TOKEN + " INTEGER, " +
                COL_STATUS + " TEXT, " +
                COL_PRIORITY + " INTEGER, " +
                COL_ARRIVAL_TIME + " TEXT, " +
                COL_ESTIMATED_TIME + " INTEGER)";
        db.execSQL(createAppointments);

        // Create Reports Table
        String createReports = "CREATE TABLE " + TABLE_REPORTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REPORT_ID + " TEXT UNIQUE, " +
                COL_PATIENT_ID + " TEXT, " +
                COL_FILE_NAME + " TEXT, " +
                COL_FILE_URL + " TEXT, " +
                COL_UPLOAD_DATE + " TEXT)";
        db.execSQL(createReports);

        seedDefaultAccounts(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        try {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_DEPARTMENT + " TEXT");
        } catch (Exception ignored) {}
        try {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PHONE + " TEXT");
        } catch (Exception ignored) {}
        seedDefaultAccounts(db);
    }

    private void seedDefaultAccounts(SQLiteDatabase db) {
        try {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS + " (" + COL_USER_ID + ", " + COL_NAME + ", " + COL_EMAIL + ", " + COL_PASSWORD + ", " + COL_ROLE + ", " + COL_DEPARTMENT + ") " +
                    "VALUES ('doc_smith', 'Dr. Smith', 'doctor@qure.com', '123456', 'Doctor', 'General Medicine')");
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS + " (" + COL_USER_ID + ", " + COL_NAME + ", " + COL_EMAIL + ", " + COL_PASSWORD + ", " + COL_ROLE + ") " +
                    "VALUES ('admin_qure', 'Hospital Admin', 'admin@qure.com', '123456', 'Admin')");
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        onCreate(db);
    }

    // =========================================================
    // USER METHODS
    // =========================================================

    public boolean insertUser(String userId, String name, String email, String password, String role) {
        return insertUser(userId, name, email, password, role, "");
    }

    public boolean insertUser(String userId, String name, String email, String password, String role, String department) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_ID, userId);
        cv.put(COL_NAME, name);
        cv.put(COL_EMAIL, email.toLowerCase().trim());
        cv.put(COL_PASSWORD, password);
        cv.put(COL_ROLE, role);
        cv.put(COL_DEPARTMENT, department != null ? department : "");

        long result = db.insertWithOnConflict(TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public Cursor getUserByEmailPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{email.toLowerCase().trim(), password});
    }

    public Cursor getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + " = ?",
                new String[]{userId});
    }

    public boolean deleteUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_APPOINTMENTS, COL_PATIENT_ID + " = ?", new String[]{userId});
        } catch (Exception ignored) {}
        int rows = db.delete(TABLE_USERS, COL_USER_ID + " = ?", new String[]{userId});
        return rows > 0;
    }

    public List<UserAccount> getAllUsers() {
        List<UserAccount> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + ", " + COL_NAME + ", " + COL_EMAIL + ", " + COL_PHONE + ", " + COL_ROLE + ", " + COL_DEPARTMENT + " FROM " + TABLE_USERS, null);
        while (cursor.moveToNext()) {
            String uId = cursor.getString(0);
            String name = cursor.getString(1);
            String email = cursor.getString(2);
            String phone = cursor.getString(3);
            String role = cursor.getString(4);
            String dept = cursor.getString(5);
            list.add(new UserAccount(
                    uId != null ? uId : "",
                    name != null ? name : "User",
                    email != null ? email : "",
                    phone != null ? phone : "",
                    role != null ? role : Constants.ROLE_PATIENT,
                    dept != null ? dept : ""
            ));
        }
        cursor.close();
        return list;
    }

    public boolean deleteUserAccountByAdmin(String userId, String name, String email, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            if (userId != null && !userId.isEmpty()) {
                db.delete(TABLE_USERS, COL_USER_ID + " = ?", new String[]{userId});
                db.delete(TABLE_APPOINTMENTS, COL_PATIENT_ID + " = ?", new String[]{userId});
            }
            if (email != null && !email.trim().isEmpty()) {
                db.delete(TABLE_USERS, COL_EMAIL + " = ?", new String[]{email.trim()});
            }
            if (name != null && !name.trim().isEmpty()) {
                String cleanName = name.replace("Dr. ", "").replace("Dr.", "").trim();
                String search = "%" + cleanName + "%";
                if (Constants.ROLE_DOCTOR.equalsIgnoreCase(role) || name.toLowerCase().startsWith("dr")) {
                    db.delete(TABLE_USERS, COL_NAME + " = ? OR " + COL_NAME + " LIKE ?", new String[]{name, search});
                    db.delete(TABLE_APPOINTMENTS, COL_DOCTOR_NAME + " = ? OR " + COL_DOCTOR_NAME + " LIKE ?", new String[]{name, search});
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateUserPhone(String userId, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PHONE, phone);
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + " = ?", new String[]{userId});
        return rows > 0;
    }

    public String getUserPhone(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_PHONE + " FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + " = ?",
                new String[]{userId});
        String phone = "";
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            phone = cursor.getString(0);
        }
        cursor.close();
        return phone;
    }

    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<String> getDoctorNames() {
        return getDoctorNamesByDepartment(null);
    }

    public List<String> getDoctorNamesByDepartment(String department) {
        List<String> doctors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        String[] args;

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
            query = "SELECT " + COL_NAME + " FROM " + TABLE_USERS +
                    " WHERE (LOWER(TRIM(" + COL_ROLE + ")) = 'doctor' OR LOWER(TRIM(" + COL_NAME + ")) LIKE 'dr.%' OR LOWER(TRIM(" + COL_NAME + ")) LIKE 'dr %') " +
                    " AND (LOWER(TRIM(" + COL_DEPARTMENT + ")) = ? OR " + COL_DEPARTMENT + " IS NULL OR " + COL_DEPARTMENT + " = '')";
            args = new String[]{department.toLowerCase().trim()};
        } else {
            query = "SELECT " + COL_NAME + " FROM " + TABLE_USERS +
                    " WHERE LOWER(TRIM(" + COL_ROLE + ")) = 'doctor' OR LOWER(TRIM(" + COL_NAME + ")) LIKE 'dr.%' OR LOWER(TRIM(" + COL_NAME + ")) LIKE 'dr %'";
            args = null;
        }

        Cursor cursor = db.rawQuery(query, args);
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            if (name != null && !name.trim().isEmpty()) {
                name = name.trim();
                if (!name.startsWith("Dr. ") && !name.startsWith("Dr.")) {
                    name = "Dr. " + name;
                }
                if (!doctors.contains(name)) {
                    doctors.add(name);
                }
            }
        }
        cursor.close();
        return doctors;
    }

    // =========================================================
    // APPOINTMENT METHODS
    // =========================================================

    public boolean insertAppointment(Appointment appt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_APPT_ID, appt.getAppointmentId());
        cv.put(COL_PATIENT_ID, appt.getPatientId());
        cv.put(COL_PATIENT_NAME, appt.getPatientName());
        cv.put(COL_DOCTOR_NAME, appt.getDoctorName());
        cv.put(COL_DEPARTMENT, appt.getDepartment());
        cv.put(COL_DATE, appt.getAppointmentDate());
        cv.put(COL_SYMPTOMS, appt.getSymptoms());
        cv.put(COL_TOKEN, appt.getTokenNumber());
        cv.put(COL_STATUS, appt.getStatus());
        cv.put(COL_PRIORITY, appt.getPriority());
        cv.put(COL_ARRIVAL_TIME, appt.getArrivalTime());
        cv.put(COL_ESTIMATED_TIME, appt.getEstimatedConsultationTime());

        long result = db.insertWithOnConflict(TABLE_APPOINTMENTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean updateAppointmentStatus(String appointmentId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, newStatus);
        int rows = db.update(TABLE_APPOINTMENTS, cv, COL_APPT_ID + " = ?", new String[]{appointmentId});
        return rows > 0;
    }

    public boolean setAppointmentEmergency(String appointmentId, boolean isEmergency) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (isEmergency) {
            cv.put(COL_PRIORITY, Constants.PRIORITY_EMERGENCY);
            cv.put(COL_STATUS, Constants.STATUS_EMERGENCY);
        } else {
            cv.put(COL_PRIORITY, Constants.PRIORITY_NORMAL);
            cv.put(COL_STATUS, Constants.STATUS_WAITING);
        }
        int rows = db.update(TABLE_APPOINTMENTS, cv, COL_APPT_ID + " = ?", new String[]{appointmentId});
        return rows > 0;
    }

    public boolean checkInAppointment(String appointmentId, String arrivalTimestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, Constants.STATUS_WAITING);
        cv.put(COL_ARRIVAL_TIME, arrivalTimestamp);
        int rows = db.update(TABLE_APPOINTMENTS, cv, COL_APPT_ID + " = ?", new String[]{appointmentId});
        return rows > 0;
    }

    public String getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?",
                new String[]{email.toLowerCase().trim()});
        String userId = null;
        if (cursor.moveToFirst()) {
            userId = cursor.getString(0);
        }
        cursor.close();
        return userId;
    }

    public Appointment getActiveAppointment(String patientId) {
        return getActiveAppointment(patientId, null);
    }

    public Appointment getActiveAppointment(String patientId, String patientName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection;
        List<String> argsList = new ArrayList<>();

        if (patientId != null && !patientId.isEmpty() && patientName != null && !patientName.isEmpty()) {
            selection = "(" + COL_PATIENT_ID + " = ? OR " + COL_PATIENT_NAME + " = ?) AND " + COL_STATUS + " != ?";
            argsList.add(patientId);
            argsList.add(patientName);
            argsList.add(Constants.STATUS_COMPLETED);
        } else if (patientId != null && !patientId.isEmpty()) {
            selection = COL_PATIENT_ID + " = ? AND " + COL_STATUS + " != ?";
            argsList.add(patientId);
            argsList.add(Constants.STATUS_COMPLETED);
        } else if (patientName != null && !patientName.isEmpty()) {
            selection = COL_PATIENT_NAME + " = ? AND " + COL_STATUS + " != ?";
            argsList.add(patientName);
            argsList.add(Constants.STATUS_COMPLETED);
        } else {
            selection = COL_STATUS + " != ?";
            argsList.add(Constants.STATUS_COMPLETED);
        }

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE " + selection + " ORDER BY id DESC LIMIT 1", argsList.toArray(new String[0]));

        Appointment appt = null;
        if (cursor.moveToFirst()) {
            appt = cursorToAppointment(cursor);
        }
        cursor.close();
        return appt;
    }

    public List<Appointment> getDoctorWaitingQueue(String doctorName) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanName = doctorName.replace("Dr. ", "").replace("Dr.", "").trim();
        String search = "%" + cleanName + "%";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE (" + COL_DOCTOR_NAME + " = ? OR " + COL_DOCTOR_NAME + " LIKE ? OR ? LIKE '%' || " + COL_DOCTOR_NAME + " || '%') AND " +
                COL_STATUS + " != ? AND " + COL_STATUS + " != ? ORDER BY id ASC",
                new String[]{doctorName, search, doctorName, Constants.STATUS_COMPLETED, Constants.STATUS_IN_CONSULTATION});

        while (cursor.moveToNext()) {
            list.add(cursorToAppointment(cursor));
        }
        cursor.close();
        return list;
    }

    public List<Appointment> getDoctorAllActiveAppointments(String doctorName) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanName = doctorName.replace("Dr. ", "").replace("Dr.", "").trim();
        String search = "%" + cleanName + "%";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE (" + COL_DOCTOR_NAME + " = ? OR " + COL_DOCTOR_NAME + " LIKE ? OR ? LIKE '%' || " + COL_DOCTOR_NAME + " || '%') AND " +
                COL_STATUS + " != ? ORDER BY id DESC",
                new String[]{doctorName, search, doctorName, Constants.STATUS_COMPLETED});

        while (cursor.moveToNext()) {
            list.add(cursorToAppointment(cursor));
        }
        cursor.close();
        return list;
    }

    public int getDoctorActiveQueueCount(String doctorName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanName = doctorName.replace("Dr. ", "").replace("Dr.", "").trim();
        String search = "%" + cleanName + "%";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS +
                " WHERE (" + COL_DOCTOR_NAME + " = ? OR " + COL_DOCTOR_NAME + " LIKE ? OR ? LIKE '%' || " + COL_DOCTOR_NAME + " || '%') AND " +
                COL_STATUS + " != ?",
                new String[]{doctorName, search, doctorName, Constants.STATUS_COMPLETED});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<DoctorProfile> getAllDoctorProfiles() {
        List<DoctorProfile> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + ", " + COL_NAME + ", " + COL_DEPARTMENT + ", " + COL_EMAIL + " FROM " + TABLE_USERS +
                " WHERE LOWER(TRIM(" + COL_ROLE + ")) = 'doctor' OR LOWER(TRIM(" + COL_NAME + ")) LIKE 'dr.%' OR LOWER(TRIM(" + COL_NAME + ")) LIKE 'dr %'", null);

        while (cursor.moveToNext()) {
            String uId = cursor.getString(0);
            String name = cursor.getString(1);
            String dept = cursor.getString(2);
            String email = cursor.getString(3);

            if (name != null && !name.trim().isEmpty()) {
                name = name.trim();
                if (!name.startsWith("Dr. ") && !name.startsWith("Dr.")) {
                    name = "Dr. " + name;
                }
                int count = getDoctorActiveQueueCount(name);
                list.add(new DoctorProfile(uId != null ? uId : "", name, (dept != null && !dept.isEmpty()) ? dept : "General Medicine", email != null ? email : "", count));
            }
        }
        cursor.close();
        return list;
    }

    public String getDoctorEmailByName(String doctorName) {
        if (doctorName == null || doctorName.trim().isEmpty()) return "";
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanName = doctorName.replace("Dr. ", "").replace("Dr.", "").trim();
        String search = "%" + cleanName + "%";
        Cursor cursor = db.rawQuery("SELECT " + COL_EMAIL + " FROM " + TABLE_USERS +
                " WHERE (" + COL_NAME + " = ? OR " + COL_NAME + " LIKE ?) AND " + COL_EMAIL + " IS NOT NULL AND " + COL_EMAIL + " != '' LIMIT 1",
                new String[]{doctorName, search});
        String email = "";
        if (cursor.moveToFirst()) {
            email = cursor.getString(0);
        }
        cursor.close();
        return email != null ? email : "";
    }

    public Appointment getDoctorCurrentPatient(String doctorName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanName = doctorName.replace("Dr. ", "").replace("Dr.", "").trim();
        String search = "%" + cleanName + "%";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE (" + COL_DOCTOR_NAME + " = ? OR " + COL_DOCTOR_NAME + " LIKE ? OR ? LIKE '%' || " + COL_DOCTOR_NAME + " || '%') AND " + COL_STATUS + " = ? LIMIT 1",
                new String[]{doctorName, search, doctorName, Constants.STATUS_IN_CONSULTATION});

        Appointment appt = null;
        if (cursor.moveToFirst()) {
            appt = cursorToAppointment(cursor);
        }
        cursor.close();
        return appt;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS + " ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            list.add(cursorToAppointment(cursor));
        }
        cursor.close();
        return list;
    }

    public int getAppointmentCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private Appointment cursorToAppointment(Cursor c) {
        String apptId = c.getString(c.getColumnIndexOrThrow(COL_APPT_ID));
        String pId = c.getString(c.getColumnIndexOrThrow(COL_PATIENT_ID));
        String pName = c.getString(c.getColumnIndexOrThrow(COL_PATIENT_NAME));
        String docName = c.getString(c.getColumnIndexOrThrow(COL_DOCTOR_NAME));
        String dept = c.getString(c.getColumnIndexOrThrow(COL_DEPARTMENT));
        String date = c.getString(c.getColumnIndexOrThrow(COL_DATE));
        String symp = c.getString(c.getColumnIndexOrThrow(COL_SYMPTOMS));
        int token = c.getInt(c.getColumnIndexOrThrow(COL_TOKEN));
        String status = c.getString(c.getColumnIndexOrThrow(COL_STATUS));
        int priority = c.getInt(c.getColumnIndexOrThrow(COL_PRIORITY));
        String arrival = c.getString(c.getColumnIndexOrThrow(COL_ARRIVAL_TIME));
        int estTime = c.getInt(c.getColumnIndexOrThrow(COL_ESTIMATED_TIME));

        Appointment a = new Appointment(apptId, pId, pName, docName, dept, date, symp, token);
        a.setStatus(status);
        a.setPriority(priority);
        a.setArrivalTime(arrival);
        a.setEstimatedConsultationTime(estTime);
        return a;
    }

    // =========================================================
    // REPORT METHODS
    // =========================================================

    public boolean insertReport(Report report) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REPORT_ID, report.getReportId());
        cv.put(COL_PATIENT_ID, report.getPatientId());
        cv.put(COL_FILE_NAME, report.getFileName());
        cv.put(COL_FILE_URL, report.getFileUrl());
        cv.put(COL_UPLOAD_DATE, report.getUploadDate());

        long result = db.insertWithOnConflict(TABLE_REPORTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public List<Report> getReportsByPatient(String patientId) {
        List<Report> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REPORTS +
                " WHERE " + COL_PATIENT_ID + " = ? ORDER BY id DESC", new String[]{patientId});

        while (cursor.moveToNext()) {
            String rId = cursor.getString(cursor.getColumnIndexOrThrow(COL_REPORT_ID));
            String pId = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATIENT_ID));
            String fName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME));
            String fUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_URL));
            String uDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_UPLOAD_DATE));
            list.add(new Report(rId, pId, fName, fUrl, uDate));
        }
        cursor.close();
        return list;
    }
}

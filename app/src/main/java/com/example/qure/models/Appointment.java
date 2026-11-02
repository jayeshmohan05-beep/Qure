package com.example.qure.models;

/**
 * Appointment.java — Model Class
 * -------------------------------------------------------
 * WHAT THIS FILE DOES:
 *   This is a plain Java object (POJO) that represents an 
 *   Appointment in our Firestore database. It makes it much
 *   easier to pass appointment data between screens.
 * -------------------------------------------------------
 */
public class Appointment {
    private String appointmentId;
    private String patientId;
    private String patientName;
    private String doctorName;
    private String department;
    private String appointmentDate;
    private String symptoms;
    private String status;
    private int tokenNumber;
    private int priority;
    
    // NEW FOR PHASE 9: OS Scheduling Fields
    private String arrivalTime;
    private int estimatedConsultationTime;
    
    // Empty constructor required for Firebase serialization
    public Appointment() {
    }

    // Overloaded constructor for easy instantiation
    public Appointment(String appointmentId, String patientId, String patientName,
                       String doctorName, String department, String appointmentDate,
                       String symptoms, int tokenNumber) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.department = department;
        this.appointmentDate = appointmentDate;
        this.symptoms = symptoms;
        this.tokenNumber = tokenNumber;
    }

    // Getters
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getDepartment() { return department; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getSymptoms() { return symptoms; }
    public String getStatus() { return status; }
    public int getTokenNumber() { return tokenNumber; }
    public int getPriority() { return priority; }
    public String getArrivalTime() { return arrivalTime; }
    public int getEstimatedConsultationTime() { return estimatedConsultationTime; }

    // Setters
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setDepartment(String department) { this.department = department; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public void setStatus(String status) { this.status = status; }
    public void setTokenNumber(int tokenNumber) { this.tokenNumber = tokenNumber; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setEstimatedConsultationTime(int estimatedConsultationTime) { this.estimatedConsultationTime = estimatedConsultationTime; }
}

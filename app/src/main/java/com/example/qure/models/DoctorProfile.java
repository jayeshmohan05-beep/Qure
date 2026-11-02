package com.example.qure.models;

/**
 * DoctorProfile.java — Model for Doctor Profiles in Admin Dashboard
 * -----------------------------------------------------------------
 * Represents an available doctor with their department, email,
 * and current active queue size.
 */
public class DoctorProfile {

    private String doctorId;
    private String name;
    private String department;
    private String email;
    private int activeQueueCount;

    public DoctorProfile() {}

    public DoctorProfile(String name, String department, String email, int activeQueueCount) {
        this.name = name;
        this.department = department;
        this.email = email;
        this.activeQueueCount = activeQueueCount;
    }

    public DoctorProfile(String doctorId, String name, String department, String email, int activeQueueCount) {
        this.doctorId = doctorId;
        this.name = name;
        this.department = department;
        this.email = email;
        this.activeQueueCount = activeQueueCount;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getActiveQueueCount() {
        return activeQueueCount;
    }

    public void setActiveQueueCount(int activeQueueCount) {
        this.activeQueueCount = activeQueueCount;
    }
}

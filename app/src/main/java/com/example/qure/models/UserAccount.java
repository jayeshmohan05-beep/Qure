package com.example.qure.models;

/**
 * UserAccount.java — Model for User Accounts in Admin Management
 * -------------------------------------------------------------
 * Represents any registered user (Patient, Doctor, Admin)
 * with their credentials and contact information.
 */
public class UserAccount {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String department;

    public UserAccount() {}

    public UserAccount(String userId, String name, String email, String phone, String role, String department) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.department = department;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}

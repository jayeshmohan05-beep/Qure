package com.example.qure.models;

/**
 * Report.java
 * -------------------------------------------------------
 * Represents a medical report uploaded by the patient.
 * -------------------------------------------------------
 */
public class Report {
    private String reportId;
    private String patientId;
    private String fileName;
    private String fileUrl;
    private String uploadDate;

    // Empty constructor for Firebase
    public Report() {
    }

    public Report(String reportId, String patientId, String fileName, String fileUrl, String uploadDate) {
        this.reportId = reportId;
        this.patientId = patientId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadDate = uploadDate;
    }

    public String getReportId() { return reportId; }
    public String getPatientId() { return patientId; }
    public String getFileName() { return fileName; }
    public String getFileUrl() { return fileUrl; }
    public String getUploadDate() { return uploadDate; }
}

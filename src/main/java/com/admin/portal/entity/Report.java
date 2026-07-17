package com.admin.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long candidateId;

    private Long jobId;

    private String candidateName;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status = "PENDING";

    private LocalDateTime reportedAt = LocalDateTime.now();

    // Default Constructor
    public Report() {
    }

    // Parameterized Constructor
    public Report(Long candidateId, Long jobId, String candidateName,
            String email, String message, String status,
            LocalDateTime reportedAt) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.candidateName = candidateName;
        this.email = email;
        this.message = message;
        this.status = status;
        this.reportedAt = reportedAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", candidateId=" + candidateId +
                ", jobId=" + jobId +
                ", candidateName='" + candidateName + '\'' +
                ", email='" + email + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", reportedAt=" + reportedAt +
                '}';
    }
}
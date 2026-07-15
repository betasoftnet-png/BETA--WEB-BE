package com.admin.portal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "liked_jobs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email", "jobId"})
})
public class LikedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private Long jobId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
}

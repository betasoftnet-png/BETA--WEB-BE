package com.admin.portal.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jobid")
    private Long jobId;

    @Column(name = "fullname")
    private String fullName;

    private String email;

    private String phone;

    private String experience;

    private String resume;

    private String coverLetter;

    private LocalDate appliedDate;

    // New Fields
    private LocalDate interviewDate;

    private String interviewTime;

    private String status;

    @Column(name = "assessment_attempts")
    private Integer assessmentAttempts = 0;

    @Column(name = "assessment_submitted")
    private Boolean assessmentSubmitted = false;

    public JobApplication() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(String interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAssessmentAttempts() {
        return assessmentAttempts != null ? assessmentAttempts : 0;
    }

    public void setAssessmentAttempts(Integer assessmentAttempts) {
        this.assessmentAttempts = assessmentAttempts;
    }

    public Boolean getAssessmentSubmitted() {
        return assessmentSubmitted != null ? assessmentSubmitted : false;
    }

    public void setAssessmentSubmitted(Boolean assessmentSubmitted) {
        this.assessmentSubmitted = assessmentSubmitted;
    }
}
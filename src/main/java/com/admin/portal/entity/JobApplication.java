package com.admin.portal.entity;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime appliedTime;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "job_department")
    private String jobDepartment;

    @Column(name = "job_location")
    private String jobLocation;

    @Column(name = "github_link")
    private String githubLink;

    @Transient
    private Boolean taskAssigned;

    // New Fields
    private LocalDate interviewDate;

    private String interviewTime;

    private LocalDate hrInterviewDate;

    private String hrInterviewTime;

    @Column(name = "hr_interview_location")
    private String hrInterviewLocation;

    @Column(name = "interview_link")
    private String interviewLink;

    private String status;

    @Column(name = "assessment_attempts")
    private Integer assessmentAttempts = 0;

    @Column(name = "assessment_submitted")
    private Boolean assessmentSubmitted = false;

    @Column(name = "aptitude_score")
    private Integer aptitudeScore;

    @Column(name = "aptitude_status")
    private String aptitudeStatus = "Pending";

    @Column(name = "assessment_start_time")
    private java.time.LocalDateTime assessmentStartTime;

    @Column(name = "assessment_end_time")
    private java.time.LocalDateTime assessmentEndTime;

    @Column(name = "assessment_time_taken")
    private String assessmentTimeTaken;

    @Column(name = "assessment_sent_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime assessmentSentTime;

    @Column(name = "assessment_expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime assessmentExpiryTime;

    @Column(name = "assessment_token", unique = true)
    private String assessmentToken;

    @PrePersist
    public void generateAssessmentToken() {
        if (this.assessmentToken == null || this.assessmentToken.trim().isEmpty()) {
            this.assessmentToken = java.util.UUID.randomUUID().toString();
        }
    }

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

    public java.time.LocalDateTime getAppliedTime() {
        return appliedTime;
    }

    public void setAppliedTime(java.time.LocalDateTime appliedTime) {
        this.appliedTime = appliedTime;
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

    public Integer getAptitudeScore() {
        return aptitudeScore;
    }

    public void setAptitudeScore(Integer aptitudeScore) {
        this.aptitudeScore = aptitudeScore;
    }

    public String getInterviewLink() {
        return interviewLink;
    }

    public void setInterviewLink(String interviewLink) {
        this.interviewLink = interviewLink;
    }

    public String getAptitudeStatus() {
        return aptitudeStatus;
    }

    public void setAptitudeStatus(String aptitudeStatus) {
        this.aptitudeStatus = aptitudeStatus;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobDepartment() {
        return jobDepartment;
    }

    public void setJobDepartment(String jobDepartment) {
        this.jobDepartment = jobDepartment;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public void setGithubLink(String githubLink) {
        this.githubLink = githubLink;
    }

    public Boolean getTaskAssigned() {
        return taskAssigned != null ? taskAssigned : false;
    }

    public void setTaskAssigned(Boolean taskAssigned) {
        this.taskAssigned = taskAssigned;
    }

    public java.time.LocalDateTime getAssessmentStartTime() {
        return assessmentStartTime;
    }

    public void setAssessmentStartTime(java.time.LocalDateTime assessmentStartTime) {
        this.assessmentStartTime = assessmentStartTime;
    }

    public java.time.LocalDateTime getAssessmentEndTime() {
        return assessmentEndTime;
    }

    public void setAssessmentEndTime(java.time.LocalDateTime assessmentEndTime) {
        this.assessmentEndTime = assessmentEndTime;
    }

    public String getAssessmentTimeTaken() {
        return assessmentTimeTaken;
    }

    public void setAssessmentTimeTaken(String assessmentTimeTaken) {
        this.assessmentTimeTaken = assessmentTimeTaken;
    }

    public LocalDate getHrInterviewDate() {
        return hrInterviewDate;
    }

    public void setHrInterviewDate(LocalDate hrInterviewDate) {
        this.hrInterviewDate = hrInterviewDate;
    }

    public String getHrInterviewTime() {
        return hrInterviewTime;
    }

    public void setHrInterviewTime(String hrInterviewTime) {
        this.hrInterviewTime = hrInterviewTime;
    }

    public String getHrInterviewLocation() {
        return hrInterviewLocation;
    }

    public void setHrInterviewLocation(String hrInterviewLocation) {
        this.hrInterviewLocation = hrInterviewLocation;
    }

    public java.time.LocalDateTime getAssessmentSentTime() {
        return assessmentSentTime;
    }

    public void setAssessmentSentTime(java.time.LocalDateTime assessmentSentTime) {
        this.assessmentSentTime = assessmentSentTime;
    }

    public java.time.LocalDateTime getAssessmentExpiryTime() {
        return assessmentExpiryTime;
    }

    public void setAssessmentExpiryTime(java.time.LocalDateTime assessmentExpiryTime) {
        this.assessmentExpiryTime = assessmentExpiryTime;
    }

    public String getAssessmentToken() {
        if (this.assessmentToken == null || this.assessmentToken.trim().isEmpty()) {
            this.assessmentToken = java.util.UUID.randomUUID().toString();
        }
        return assessmentToken;
    }

    public void setAssessmentToken(String assessmentToken) {
        this.assessmentToken = assessmentToken;
    }

    public int getPipelineStage() {
        // Stage 0: Application - always completed on submission.
        // Stage 1: Assessment - completed when assessmentSubmitted is true or
        // aptitudeStatus is "Completed"
        boolean stage1Completed = Boolean.TRUE.equals(getAssessmentSubmitted())
                || "Completed".equalsIgnoreCase(getAptitudeStatus());
        if (!stage1Completed) {
            return 1;
        }

        // Stage 2: Technical Interview - completed when status is REVIEWED (Interview
        // Completed) or later, OR when a task has been assigned.
        String s = getStatus();
        String statusLower = s != null ? s.toLowerCase().trim() : "";
        boolean stage2Completed = "reviewed".equalsIgnoreCase(statusLower)
                || "accepted".equalsIgnoreCase(statusLower)
                || "joined".equalsIgnoreCase(statusLower)
                || "selected".equalsIgnoreCase(statusLower)
                || "approved".equalsIgnoreCase(statusLower)
                || "offer sent".equalsIgnoreCase(statusLower)
                || Boolean.TRUE.equals(getTaskAssigned());
        if (!stage2Completed) {
            return 2;
        }

        // Stage 3: Task Assessment - completed when githubLink is present
        boolean stage3Completed = getGithubLink() != null && !getGithubLink().trim().isEmpty();
        if (!stage3Completed) {
            return 3;
        }

        // Stage 4: HR Interview - completed when status is ACCEPTED or JOINED or
        // selected/approved/offer sent.
        boolean stage4Completed = "accepted".equalsIgnoreCase(statusLower)
                || "joined".equalsIgnoreCase(statusLower)
                || "selected".equalsIgnoreCase(statusLower)
                || "approved".equalsIgnoreCase(statusLower)
                || "offer sent".equalsIgnoreCase(statusLower);
        if (!stage4Completed) {
            return 4;
        }

        // Stage 5: Offer
        return 5;
    }
}
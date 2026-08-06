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

    @Column(columnDefinition = "TEXT")
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

    @Column(name = "task_assessment_sent_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime taskAssessmentSentTime;

    @Column(name = "technical_interview_sent_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime technicalInterviewSentTime;

    @Column(name = "hr_interview_sent_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime hrInterviewSentTime;

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

    public java.time.LocalDateTime getTaskAssessmentSentTime() {
        return taskAssessmentSentTime;
    }

    public void setTaskAssessmentSentTime(java.time.LocalDateTime taskAssessmentSentTime) {
        this.taskAssessmentSentTime = taskAssessmentSentTime;
    }

    public java.time.LocalDateTime getTechnicalInterviewSentTime() {
        return technicalInterviewSentTime;
    }

    public void setTechnicalInterviewSentTime(java.time.LocalDateTime technicalInterviewSentTime) {
        this.technicalInterviewSentTime = technicalInterviewSentTime;
    }

    public java.time.LocalDateTime getHrInterviewSentTime() {
        return hrInterviewSentTime;
    }

    public void setHrInterviewSentTime(java.time.LocalDateTime hrInterviewSentTime) {
        this.hrInterviewSentTime = hrInterviewSentTime;
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
        String s = getStatus();
        String statusLower = s != null ? s.toLowerCase().trim() : "";

        // Evaluate stage 5: Offer
        boolean stage5Completed = "joined".equalsIgnoreCase(statusLower);
        boolean stage5Active = "accepted".equalsIgnoreCase(statusLower)
                || "selected".equalsIgnoreCase(statusLower)
                || "approved".equalsIgnoreCase(statusLower)
                || "offer sent".equalsIgnoreCase(statusLower);
        if (stage5Completed || stage5Active) {
            return 5;
        }

        // Evaluate stage 4: HR Interview
        boolean stage4Completed = "accepted".equalsIgnoreCase(statusLower)
                || "joined".equalsIgnoreCase(statusLower)
                || "selected".equalsIgnoreCase(statusLower)
                || "approved".equalsIgnoreCase(statusLower)
                || "offer sent".equalsIgnoreCase(statusLower);
        boolean stage4Active = getHrInterviewDate() != null
                || getHrInterviewTime() != null
                || getHrInterviewLocation() != null
                || "hr interview".equalsIgnoreCase(statusLower)
                || "hr scheduled".equalsIgnoreCase(statusLower)
                || "hr round".equalsIgnoreCase(statusLower)
                || "hr".equalsIgnoreCase(statusLower);
        if (stage4Completed || stage4Active) {
            return 4;
        }

        // Evaluate stage 3: Task Assessment
        boolean stage3Completed = getGithubLink() != null && !getGithubLink().trim().isEmpty();
        boolean stage3Active = Boolean.TRUE.equals(getTaskAssigned())
                || "task assessment".equalsIgnoreCase(statusLower)
                || "task assigned".equalsIgnoreCase(statusLower)
                || "task".equalsIgnoreCase(statusLower);
        if (stage3Completed || stage3Active) {
            return 3;
        }

        // Evaluate stage 2: Technical Interview
        boolean stage2Completed = "reviewed".equalsIgnoreCase(statusLower)
                || "accepted".equalsIgnoreCase(statusLower)
                || "joined".equalsIgnoreCase(statusLower)
                || "selected".equalsIgnoreCase(statusLower)
                || "approved".equalsIgnoreCase(statusLower)
                || "offer sent".equalsIgnoreCase(statusLower)
                || Boolean.TRUE.equals(getTaskAssigned())
                || (getGithubLink() != null && !getGithubLink().trim().isEmpty())
                || getHrInterviewDate() != null
                || getHrInterviewTime() != null
                || getHrInterviewLocation() != null;
        boolean stage2Active = getInterviewDate() != null
                || getInterviewTime() != null
                || getInterviewLink() != null
                || "technical interview".equalsIgnoreCase(statusLower)
                || "interview scheduled".equalsIgnoreCase(statusLower)
                || "scheduled".equalsIgnoreCase(statusLower)
                || "technical".equalsIgnoreCase(statusLower)
                || "round 2 technical".equalsIgnoreCase(statusLower)
                || "technical assessment".equalsIgnoreCase(statusLower);
        if (stage2Completed || stage2Active) {
            return 2;
        }

        // Evaluate stage 1: Assessment
        boolean stage1Completed = Boolean.TRUE.equals(getAssessmentSubmitted())
                || "Completed".equalsIgnoreCase(getAptitudeStatus());
        return 1;
    }

    public String getFormattedAppliedTime() {
        if (this.appliedTime == null) {
            return "";
        }
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
            return this.appliedTime.format(formatter);
        } catch (Exception e) {
            return "";
        }
    }
}
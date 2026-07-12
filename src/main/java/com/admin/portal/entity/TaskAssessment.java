package com.admin.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_assessment")
public class TaskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "candidate_id", referencedColumnName = "id")
    private JobApplication candidate;

    @Column(columnDefinition = "TEXT")
    private String taskDescription;

    @Column(nullable = false)
    private String status;

    private LocalDateTime assignedAt;

    public TaskAssessment() {
        this.assignedAt = LocalDateTime.now();
        this.status = "ASSIGNED";
    }

    public Long getId() {
        return id;
    }

    public JobApplication getCandidate() {
        return candidate;
    }

    public void setCandidate(JobApplication candidate) {
        this.candidate = candidate;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
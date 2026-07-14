package com.admin.portal.service;

import com.admin.portal.dto.request.TaskRequest;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.entity.TaskAssessment;
import com.admin.portal.repository.JobApplicationRepository;
import com.admin.portal.repository.TaskAssessmentRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskAssessmentService {

    private final TaskAssessmentRepository taskRepository;
    private final JobApplicationRepository jobRepository;
    private final EmailService emailService;

    public TaskAssessmentService(TaskAssessmentRepository taskRepository,
            JobApplicationRepository jobRepository,
            EmailService emailService) {
        this.taskRepository = taskRepository;
        this.jobRepository = jobRepository;
        this.emailService = emailService;
    }

    public TaskAssessment assignTask(Long candidateId, TaskRequest request) {

        JobApplication candidate = jobRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        TaskAssessment task = taskRepository.findByCandidate_Id(candidateId)
                .orElseGet(() -> {
                    TaskAssessment t = new TaskAssessment();
                    t.setCandidate(candidate);
                    return t;
                });
        task.setTaskDescription(request.getTaskDescription());
        task.setStatus("ASSIGNED");
        task.setAssignedAt(java.time.LocalDateTime.now());
        task.setSubmittedAt(null);

        candidate.setGithubLink(null);
        jobRepository.save(candidate);

        TaskAssessment savedTask = taskRepository.save(task);

        // Send email
        emailService.sendTaskAssessmentEmail(candidate, request.getTaskDescription());

        return savedTask;
    }

    public TaskAssessment getTask(Long candidateId) {

        return taskRepository.findByCandidate_Id(candidateId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public TaskAssessment submitTask(Long candidateId, String githubLink) {
        TaskAssessment task = taskRepository.findByCandidate_Id(candidateId)
                .orElseThrow(() -> new RuntimeException("Task assessment not found for candidate"));

        if ("SUBMITTED".equalsIgnoreCase(task.getStatus())) {
            throw new RuntimeException("Task has already been submitted");
        }

        JobApplication candidate = task.getCandidate();
        candidate.setGithubLink(githubLink);
        jobRepository.save(candidate);

        task.setStatus("SUBMITTED");
        task.setSubmittedAt(java.time.LocalDateTime.now());
        return taskRepository.save(task);
    }
}
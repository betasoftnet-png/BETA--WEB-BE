package com.admin.portal.service;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.admin.portal.repository.TaskAssessmentRepository;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository repository;

    @Autowired
    private JobService jobService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TaskAssessmentRepository taskRepository;

    public JobApplication save(JobApplication application) {

        // Applied date is today
        LocalDate appliedDate = LocalDate.now();

        // Set applied date
        application.setAppliedDate(appliedDate);

        // Validate interview date (if provided)
        if (application.getInterviewDate() != null) {

            long days = ChronoUnit.DAYS.between(
                    appliedDate,
                    application.getInterviewDate());

            if (days < 0 || days > 3) {
                throw new RuntimeException(
                        "Interview date must be within 3 days of applying.");
            }
        }

        // Default status
        application.setStatus("PENDING");

        JobApplication savedApp = repository.save(application);

        // Retrieve job title
        String jobTitle = null;
        if (savedApp.getJobId() != null) {
            jobTitle = jobService.getJobById(savedApp.getJobId())
                    .map(job -> job.getTitle())
                    .orElse(null);
        }

        // Send acknowledgment email to candidate
        try {
            emailService.sendAcknowledgementEmail(savedApp, jobTitle);
        } catch (Exception e) {
            // Log and swallow so application submission isn't blocked by mail errors
            System.err.println("Error sending job application acknowledgment email: " + e.getMessage());
        }

        return savedApp;
    }

    public boolean hasAlreadyApplied(Long jobId, String email) {
        if (jobId == null || email == null) return false;
        return repository.existsByJobIdAndEmailIgnoreCase(jobId, email.trim());
    }

    public List<JobApplication> getApplicationsByEmail(String email) {
        List<JobApplication> apps = repository.findByEmailIgnoreCase(email);
        for (JobApplication app : apps) {
            populateJobDetails(app);
        }
        return apps;
    }

    public void populateJobDetails(JobApplication app) {
        if (app.getJobId() != null) {
            jobService.getJobById(app.getJobId()).ifPresent(job -> {
                app.setJobTitle(job.getTitle());
                app.setJobDepartment(job.getDepartment());
                app.setJobLocation(job.getLocation());
            });
        }
        boolean assigned = taskRepository.findByCandidate_Id(app.getId()).isPresent();
        app.setTaskAssigned(assigned);
    }

    public JobApplication submitGithubLink(Long id, String githubLink) {
        JobApplication app = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setGithubLink(githubLink);

        taskRepository.findByCandidate_Id(id).ifPresent(task -> {
            task.setStatus("SUBMITTED");
            taskRepository.save(task);
        });

        return repository.save(app);
    }
}
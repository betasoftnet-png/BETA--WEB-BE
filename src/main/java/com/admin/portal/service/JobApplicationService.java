package com.admin.portal.service;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository repository;

    @Autowired
    private JobService jobService;

    @Autowired
    private EmailService emailService;

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
}
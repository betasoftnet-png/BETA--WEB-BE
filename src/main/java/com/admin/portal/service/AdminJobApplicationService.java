package com.admin.portal.service;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.repository.AdminJobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminJobApplicationService {

    @Autowired
    private AdminJobApplicationRepository repository;

    @Autowired
    private JobService jobService;

    @Autowired
    private EmailService emailService;

    public List<JobApplication> getAllApplications() {
        return repository.findAll();
    }

    public JobApplication updateStatus(Long id, String status) {
        JobApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        
        JobApplication savedApp = repository.save(app);

        // Retrieve job title if jobId is available
        String jobTitle = null;
        if (savedApp.getJobId() != null) {
            jobTitle = jobService.getJobById(savedApp.getJobId())
                    .map(job -> job.getTitle())
                    .orElse(null);
        }

        // Send status update notification email asynchronously or synchronously
        if ("ACCEPTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {
            try {
                emailService.sendApplicationStatusEmail(savedApp, jobTitle);
            } catch (Exception e) {
                // Log the exception but do not fail the database transaction
                System.err.println("Error sending status email notification: " + e.getMessage());
            }
        }

        return savedApp;
    }
}
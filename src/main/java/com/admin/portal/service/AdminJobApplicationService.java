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

    @Autowired
    private NotificationService notificationService;

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

        // Create notification
        try {
            String friendly = getFriendlyStatus(status);
            String title = "Application Status Update";
            String message = "Your job application status has been updated to: " + friendly + ".";
            
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                title = "Application Accepted";
                message = "Congratulations! Your application has been accepted.";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                title = "Application Rejected";
                message = "We regret to inform you that we are moving forward with other candidates.";
            } else if ("SHORTLISTED".equalsIgnoreCase(status)) {
                title = "Application Shortlisted";
                message = "Great news! Your profile has been shortlisted for the next stage.";
            } else if ("SCHEDULED".equalsIgnoreCase(status)) {
                title = "Interview Scheduled";
                message = "Your interview has been scheduled. Please check your dashboard for details.";
            } else if ("REVIEWED".equalsIgnoreCase(status)) {
                title = "Interview Completed";
                message = "Your interview has been completed and is under review.";
            } else if ("JOINED".equalsIgnoreCase(status)) {
                title = "Onboarding Started";
                message = "Welcome to the team! Your onboarding process has started.";
            }
            
            notificationService.createNotification(savedApp.getId(), title, message);
        } catch (Exception e) {
            System.err.println("Failed to create status update notification: " + e.getMessage());
        }

        return savedApp;
    }

    public JobApplication scheduleInterview(Long id, String dateStr, String timeStr, String linkStr) {
        JobApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            app.setInterviewDate(java.time.LocalDate.parse(dateStr.trim()));
        } else {
            app.setInterviewDate(null);
        }
        
        app.setInterviewTime(timeStr != null ? timeStr.trim() : null);
        app.setInterviewLink(linkStr != null ? linkStr.trim() : null);
        
        JobApplication savedApp = repository.save(app);

        // Send email to candidate
        try {
            String jobTitle = null;
            if (savedApp.getJobId() != null) {
                jobTitle = jobService.getJobById(savedApp.getJobId())
                        .map(job -> job.getTitle())
                        .orElse("the Applied Position");
            } else {
                jobTitle = "the Applied Position";
            }
            
            String subject = "Interview Scheduled - Beta Softnet";
            
            // Build highly professional HTML email body
            String emailBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;\">" +
                    "<h2 style=\"color: #004AAD;\">Interview Scheduled</h2>" +
                    "<p>Dear <strong>" + savedApp.getFullName() + "</strong>,</p>" +
                    "<p>We are pleased to invite you for an interview for the <strong>" + jobTitle + "</strong> position.</p>" +
                    "<div style=\"background-color: #f8fafc; padding: 15px; border-radius: 8px; border-left: 4px solid #004AAD; margin: 20px 0;\">" +
                    "<p style=\"margin: 5px 0;\"><strong>Date:</strong> " + savedApp.getInterviewDate() + "</p>" +
                    "<p style=\"margin: 5px 0;\"><strong>Time:</strong> " + savedApp.getInterviewTime() + "</p>" +
                    "<p style=\"margin: 5px 0;\"><strong>Meeting Link:</strong> <a href=\"" + savedApp.getInterviewLink() + "\" style=\"color: #004AAD; text-decoration: underline;\">" + savedApp.getInterviewLink() + "</a></p>" +
                    "</div>" +
                    "<p>Please click the meeting link above at the scheduled time to join the interview. If you have any questions, please feel free to reach out.</p>" +
                    "<br/>" +
                    "<p>Best regards,</p>" +
                    "<p><strong>Beta Softnet Recruitment Team</strong></p>" +
                    "</div>";
            
            emailService.sendEmail(savedApp.getEmail(), subject, emailBody, true);
        } catch (Exception e) {
            System.err.println("Error sending interview scheduling email: " + e.getMessage());
        }

        // Create notification
        try {
            String linkInfo = (savedApp.getInterviewLink() != null) ? " Meeting Link: " + savedApp.getInterviewLink() : "";
            notificationService.createNotification(
                savedApp.getId(), 
                "Interview Scheduled", 
                "Your interview has been scheduled for " + savedApp.getInterviewDate() + " at " + savedApp.getInterviewTime() + "." + linkInfo
            );
        } catch (Exception e) {
            System.err.println("Failed to create interview scheduled notification: " + e.getMessage());
        }

        return savedApp;
    }

    private String getFriendlyStatus(String status) {
        if (status == null) return "Updated";
        switch (status.toUpperCase()) {
            case "ACCEPTED": return "Accepted";
            case "REJECTED": return "Rejected";
            case "SHORTLISTED": return "Shortlisted";
            case "SCHEDULED": return "Interview Scheduled";
            case "REVIEWED": return "Interview Completed";
            case "JOINED": return "Joined";
            case "PENDING": return "Candidates";
            default: return status;
        }
    }
}
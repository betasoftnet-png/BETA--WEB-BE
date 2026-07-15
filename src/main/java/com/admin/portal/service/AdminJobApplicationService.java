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
        List<JobApplication> apps = repository.findAll();
        for (JobApplication app : apps) {
            if (app.getJobId() != null) {
                jobService.getJobById(app.getJobId()).ifPresent(job -> {
                    app.setJobTitle(job.getTitle());
                    app.setJobDepartment(job.getDepartment());
                    app.setJobLocation(job.getLocation());
                });
            }
        }
        return apps;
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
        if ("REJECTED".equalsIgnoreCase(status)) {
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

        if (savedApp.getJobId() != null) {
            jobService.getJobById(savedApp.getJobId()).ifPresent(job -> {
                savedApp.setJobTitle(job.getTitle());
                savedApp.setJobDepartment(job.getDepartment());
                savedApp.setJobLocation(job.getLocation());
            });
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
            
            String dateFormatted = "";
            if (savedApp.getInterviewDate() != null) {
                dateFormatted = savedApp.getInterviewDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            String timeFormatted = savedApp.getInterviewTime() != null ? savedApp.getInterviewTime() : "";
            if (timeFormatted != null && timeFormatted.contains(":")) {
                try {
                    java.time.LocalTime timeObj = java.time.LocalTime.parse(timeFormatted.trim());
                    timeFormatted = timeObj.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
                } catch (Exception ex) {
                    // Fallback to raw string
                }
            }

            String meetingLink = savedApp.getInterviewLink() != null ? savedApp.getInterviewLink() : "";

            String candidateName = savedApp.getFullName() != null ? savedApp.getFullName() : "Candidate";
            String subject = "BETA | Selected for the Next Round";

            int year = java.time.LocalDate.now().getYear();
            String emailBody = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "  <meta charset=\"utf-8\">\n" +
                    "  <title>" + subject + "</title>\n" +
                    "</head>\n" +
                    "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                    "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n" +
                    "    <tr>\n" +
                    "      <td align=\"center\">\n" +
                    "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 48px;\">\n" +
                    "          <!-- Centered Logo -->\n" +
                    "          <tr>\n" +
                    "            <td align=\"center\" style=\"padding-bottom: 12px;\">\n" +
                    "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n" +
                    "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 10px; font-family: 'Roboto', Arial, sans-serif;\">" + escapeHtml(savedApp.getEmail()) + "</span>\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Divider -->\n" +
                    "          <tr>\n" +
                    "            <td style=\"padding: 12px 0 24px 0;\">\n" +
                    "              <hr style=\"border: 0; border-top: 1px solid #dadce0; margin: 0;\" />\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Content Body -->\n" +
                    "          <tr>\n" +
                    "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.8; text-align: left;\">\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">Dear <strong>" + escapeHtml(candidateName) + "</strong>,</p>\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">Congratulations!</p>\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">We are pleased to inform you that you have successfully cleared the <strong>Test Round</strong> for the position of <strong>" + escapeHtml(jobTitle) + "</strong> and have progressed to the second stage of our recruitment process.</p>\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">We are delighted to invite you to attend the <strong>Technical Interview</strong>. Please find your interview details below.</p>\n" +
                    "              <div style=\"background-color: #f8fafc; padding: 20px; border-radius: 8px; border: 1px solid #e2e8f0; margin: 24px 0;\">\n" +
                    "                <p style=\"margin: 8px 0;\"><strong>Interview Date:</strong> " + dateFormatted + "</p>\n" +
                    "                <p style=\"margin: 8px 0;\"><strong>Interview Time:</strong> " + timeFormatted + "</p>\n" +
                    "                <p style=\"margin: 8px 0;\"><strong>Platform:</strong> Google Meet</p>\n" +
                    "                <p style=\"margin: 8px 0;\"><strong>Meeting Link:</strong> <a href=\"" + meetingLink + "\" style=\"color: #004AAD; text-decoration: underline;\">" + meetingLink + "</a></p>\n" +
                    "              </div>\n" +
                    "              <p style=\"margin: 24px 0 12px 0;\"><strong>Important Instructions:</strong></p>\n" +
                    "              <ul style=\"margin: 0 0 24px 0; padding-left: 20px;\">\n" +
                    "                <li style=\"margin-bottom: 8px;\">Join the meeting <strong>10 minutes before</strong> the scheduled time.</li>\n" +
                    "                <li style=\"margin-bottom: 8px;\">Ensure you have a stable internet connection, a working microphone, and a camera.</li>\n" +
                    "                <li style=\"margin-bottom: 8px;\">If you are unable to attend at the scheduled time, please inform us in advance by replying to this email.</li>\n" +
                    "              </ul>\n" +
                    "              <p style=\"margin: 0 0 36px 0;\">We appreciate your effort and wish you continued success in the upcoming stage of the recruitment process.</p>\n" +
                    "              <p style=\"margin: 0 0 8px 0;\">Best Regards,<br><br>\n" +
                    "                <strong>The BETA Team</strong>\n" +
                    "              </p>\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Footer Divider -->\n" +
                    "          <tr>\n" +
                    "            <td style=\"padding: 36px 0 20px 0;\">\n" +
                    "              <hr style=\"border: 0; border-top: 1px solid #dadce0; margin: 0;\" />\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Footer -->\n" +
                    "          <tr>\n" +
                    "            <td align=\"center\" style=\"color: #70757a; font-family: 'Roboto', Arial, sans-serif; font-size: 12px; line-height: 1.5; text-align: center;\">\n" +
                    "              This is an automated notification. Please do not reply directly to this email.<br>\n" +
                    "              &copy; " + year + " BETA. All rights reserved.\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "        </table>\n" +
                    "      </td>\n" +
                    "    </tr>\n" +
                    "  </table>\n" +
                    "</body>\n" +
                    "</html>";

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

        if (savedApp.getJobId() != null) {
            jobService.getJobById(savedApp.getJobId()).ifPresent(job -> {
                savedApp.setJobTitle(job.getTitle());
                savedApp.setJobDepartment(job.getDepartment());
                savedApp.setJobLocation(job.getLocation());
            });
        }
        return savedApp;
    }

    public JobApplication saveHrInterview(Long id, String dateStr, String timeStr, String locationStr) {
        JobApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            app.setHrInterviewDate(java.time.LocalDate.parse(dateStr.trim()));
        } else {
            app.setHrInterviewDate(null);
        }
        
        app.setHrInterviewTime(timeStr != null ? timeStr.trim() : null);
        
        String finalLocation = locationStr;
        if (finalLocation == null || finalLocation.trim().isEmpty() || "BETA Office".equalsIgnoreCase(finalLocation.trim())) {
            finalLocation = "Beta Towers, No. 12, Main Road, Tiruvallur, Tamil Nadu 602001, India";
        }
        app.setHrInterviewLocation(finalLocation.trim());
        
        JobApplication savedApp = repository.save(app);
        
        // Format date/time and send HR interview email
        try {
            String jobTitle = "the Position";
            if (savedApp.getJobId() != null && jobService != null) {
                jobTitle = jobService.getJobById(savedApp.getJobId())
                        .map(job -> job.getTitle())
                        .orElse("the Position");
            }

            String dateFormatted = "";
            if (savedApp.getHrInterviewDate() != null) {
                dateFormatted = savedApp.getHrInterviewDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            String timeFormatted = savedApp.getHrInterviewTime() != null ? savedApp.getHrInterviewTime() : "";
            if (timeFormatted != null && timeFormatted.contains(":")) {
                try {
                    java.time.LocalTime timeObj = java.time.LocalTime.parse(timeFormatted.trim());
                    timeFormatted = timeObj.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
                } catch (Exception ex) {
                    // Fallback to raw string
                }
            }

            String locationFormatted = savedApp.getHrInterviewLocation();
            if (locationFormatted == null || locationFormatted.trim().isEmpty() || "BETA Office".equalsIgnoreCase(locationFormatted.trim())) {
                locationFormatted = "Beta Towers, No. 12, Main Road, Tiruvallur, Tamil Nadu 602001, India";
            }
            String mapsUrl = "https://maps.google.com/?q=";
            try {
                mapsUrl += java.net.URLEncoder.encode(locationFormatted, "UTF-8");
            } catch (Exception encodeEx) {
                mapsUrl += locationFormatted;
            }

            String candidateName = savedApp.getFullName() != null ? savedApp.getFullName() : "Candidate";
            String subject = "BETA – HR Round Interview Invitation";

            int year = java.time.LocalDate.now().getYear();
            String emailBody = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "  <meta charset=\"utf-8\">\n" +
                    "  <title>" + subject + "</title>\n" +
                    "</head>\n" +
                    "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                    "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n" +
                    "    <tr>\n" +
                    "      <td align=\"center\">\n" +
                    "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 48px;\">\n" +
                    "          <!-- Centered Logo -->\n" +
                    "          <tr>\n" +
                    "            <td align=\"center\" style=\"padding-bottom: 12px;\">\n" +
                    "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n" +
                    "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 10px; font-family: 'Roboto', Arial, sans-serif;\">" + escapeHtml(savedApp.getEmail()) + "</span>\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Divider -->\n" +
                    "          <tr>\n" +
                    "            <td style=\"padding: 12px 0 24px 0;\">\n" +
                    "              <hr style=\"border: 0; border-top: 1px solid #dadce0; margin: 0;\" />\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Content Body -->\n" +
                    "          <tr>\n" +
                    "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.8; text-align: left;\">\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">Dear <strong>" + escapeHtml(candidateName) + "</strong>,</p>\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">Congratulations!</p>\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">We are pleased to inform you that we have reviewed your <strong>Task Assessment</strong> for the position of <strong>" + escapeHtml(jobTitle) + "</strong>. Based on your submission, you have been shortlisted for the <em>final stage (HR Round)</em> of our recruitment process.</p>\n" +
                    "              <p style=\"margin: 0 0 24px 0;\">The <em>HR Round</em> will be conducted as an in-person interview at our office.</p>\n" +
                    "              <div style=\"background-color: #f8fafc; padding: 20px; border-radius: 8px; border: 1px solid #e2e8f0; margin: 24px 0;\">\n" +
                    "                <p style=\"margin: 8px 0;\"><strong>Interview Date:</strong> " + dateFormatted + "</p>\n" +
                    "                <p style=\"margin: 8px 0;\"><strong>Interview Time:</strong> " + timeFormatted + "</p>\n" +
                    "                <p style=\"margin: 8px 0;\"><strong>Venue:</strong> <a href=\"" + mapsUrl + "\" style=\"color: #004AAD; text-decoration: underline;\">" + locationFormatted + "</a></p>\n" +
                    "              </div>\n" +
                    "              <p style=\"margin: 24px 0 12px 0;\"><strong>Important Instructions :</strong></p>\n" +
                    "              <ul style=\"margin: 0 0 24px 0; padding-left: 20px;\">\n" +
                    "                <li style=\"margin-bottom: 8px;\">Please report to the venue <strong>10 minutes before</strong> the scheduled interview time.</li>\n" +
                    "                <li style=\"margin-bottom: 8px;\">Carry your laptop for the interview.</li>\n" +
                    "                <li style=\"margin-bottom: 8px;\">Bring an updated copy of your resume.</li>\n" +
                    "              </ul>\n" +
                    "              <p style=\"margin: 0 0 36px 0;\">We wish you success in the HR Round and look forward to meeting you in person.</p>\n" +
                    "              <p style=\"margin: 0 0 8px 0;\">Best Regards,<br><br>\n" +
                    "                <strong>The BETA Team</strong>\n" +
                    "              </p>\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Footer Divider -->\n" +
                    "          <tr>\n" +
                    "            <td style=\"padding: 36px 0 20px 0;\">\n" +
                    "              <hr style=\"border: 0; border-top: 1px solid #dadce0; margin: 0;\" />\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "          <!-- Footer -->\n" +
                    "          <tr>\n" +
                    "            <td align=\"center\" style=\"color: #70757a; font-family: 'Roboto', Arial, sans-serif; font-size: 12px; line-height: 1.5; text-align: center;\">\n" +
                    "              This is an automated notification. Please do not reply directly to this email.<br>\n" +
                    "              &copy; " + year + " BETA. All rights reserved.\n" +
                    "            </td>\n" +
                    "          </tr>\n" +
                    "        </table>\n" +
                    "      </td>\n" +
                    "    </tr>\n" +
                    "  </table>\n" +
                    "</body>\n" +
                    "</html>";

            emailService.sendEmail(savedApp.getEmail(), subject, emailBody, true);
        } catch (Exception e) {
            System.err.println("Error sending HR interview email: " + e.getMessage());
        }
        
        if (savedApp.getJobId() != null) {
            jobService.getJobById(savedApp.getJobId()).ifPresent(job -> {
                savedApp.setJobTitle(job.getTitle());
                savedApp.setJobDepartment(job.getDepartment());
                savedApp.setJobLocation(job.getLocation());
            });
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

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
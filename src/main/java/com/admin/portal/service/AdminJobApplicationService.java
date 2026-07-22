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
        // JobTitleMigration (@PostConstruct) ensures job_title is backfilled in the DB
        // at startup, so we can simply return the persisted values directly.
        return repository.findAll();
    }

    public JobApplication getApplicationById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
    }

    /**
     * Allows admin to manually set/correct the job title for an application
     * whose referenced job was permanently hard-deleted from the database.
     */
    public JobApplication updateJobTitle(Long id, String jobTitle) {
        JobApplication app = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setJobTitle(jobTitle != null ? jobTitle.trim() : null);
        return repository.save(app);
    }

    public JobApplication updateExperience(Long id, String experience) {
        JobApplication app = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setExperience(experience != null ? experience.trim() : null);
        return repository.save(app);
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
                dateFormatted = savedApp.getInterviewDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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
                    "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "  <title>" + subject + "</title>\n" +
                    "  <style>\n" +
                    "    @media only screen and (max-width: 620px) {\n" +
                    "      .email-content-cell {\n" +
                    "        padding: 32px 20px !important;\n" +
                    "      }\n" +
                    "      .email-wrapper-cell {\n" +
                    "        padding: 24px 12px !important;\n" +
                    "      }\n" +
                    "    }\n" +
                    "  </style>\n" +
                    "</head>\n" +
                    "<body style=\"margin: 0; padding: 0; background-color: #f8fafc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                    "  <table class=\"email-wrapper\" role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f8fafc;\">\n" +
                    "    <tr>\n" +
                    "      <td align=\"center\" class=\"email-wrapper-cell\" style=\"padding: 40px 16px;\">\n" +
                    "        <table class=\"email-container\" role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.045); width: 100%; max-width: 580px;\">\n" +
                    "          <tr>\n" +
                    "            <td class=\"email-content-cell\" style=\"padding: 40px 44px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">\n" +
                    "              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                    "                <!-- Centered Logo -->\n" +
                    "                <tr>\n" +
                    "                  <td align=\"center\" style=\"padding-bottom: 8px;\">\n" +
                    "                    <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 56px; width: auto; display: block; margin: 0 auto;\" />\n" +
                    "                    <span style=\"font-size: 13px; color: #64748b; font-weight: 500; display: block; margin-top: 8px; font-family: inherit;\">" + escapeHtml(savedApp.getEmail()) + "</span>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Divider -->\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding: 16px 0 24px 0;\">\n" +
                    "                    <hr style=\"border: 0; border-top: 1px solid #e2e8f0; margin: 0;\" />\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Content Body -->\n" +
                    "                <tr>\n" +
                    "                  <td style=\"color: #334155; font-family: inherit; font-size: 15px; line-height: 1.6; text-align: left;\">\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">Dear <strong>" + escapeHtml(candidateName) + "</strong>,</p>\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">Congratulations!</p>\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">We are pleased to inform you that you have successfully cleared the <strong>Test Round</strong> for the position of <strong>" + escapeHtml(jobTitle) + "</strong> and have progressed to the second stage of our recruitment process.</p>\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">We are delighted to invite you to attend the <strong>Technical Interview</strong>. Please find your interview details below.</p>\n" +
                    "                    <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"width: 100%;\">\n" +
                    "                      <tr>\n" +
                    "                        <td style=\"padding: 16px 0 20px 0;\">\n" +
                    "                          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; width: 100%;\">\n" +
                    "                            <tr>\n" +
                    "                              <td style=\"padding: 20px 24px; font-family: inherit;\">\n" +
                    "                                <p style=\"margin: 0 0 10px 0; font-size: 14px; color: #334155; line-height: 1.6;\"><strong style=\"color: #0f172a; font-weight: 600;\">Interview Date:</strong> " + dateFormatted + "</p>\n" +
                    "                                <p style=\"margin: 0 0 10px 0; font-size: 14px; color: #334155; line-height: 1.6;\"><strong style=\"color: #0f172a; font-weight: 600;\">Interview Time:</strong> " + timeFormatted + "</p>\n" +
                    "                                <p style=\"margin: 0 0 10px 0; font-size: 14px; color: #334155; line-height: 1.6;\"><strong style=\"color: #0f172a; font-weight: 600;\">Platform:</strong> Google Meet</p>\n" +
                    "                                <p style=\"margin: 0; font-size: 14px; color: #334155; line-height: 1.6;\"><strong style=\"color: #0f172a; font-weight: 600;\">Meeting Link:</strong> <a href=\"" + meetingLink + "\" style=\"color: #0284c7; text-decoration: underline; font-weight: 600;\">" + meetingLink + "</a></p>\n" +
                    "                              </td>\n" +
                    "                            </tr>\n" +
                    "                          </table>\n" +
                    "                        </td>\n" +
                    "                      </tr>\n" +
                    "                    </table>\n" +
                    "                    <p style=\"margin: 0 0 12px 0; font-size: 15px; color: #0f172a; font-weight: 600;\">Important Instructions:</p>\n" +
                    "                    <ul style=\"margin: 0 0 24px 0; padding-left: 0; list-style-type: none; color: #334155; font-size: 15px; line-height: 1.6;\">\n" +
                    "                      <li style=\"margin-bottom: 10px;\">Join the meeting <strong>10 minutes before</strong> the scheduled time.</li>\n" +
                    "                      <li style=\"margin-bottom: 10px;\">Ensure you have a stable internet connection, a working microphone, and a camera.</li>\n" +
                    "                    </ul>\n" +
                    "                    <p style=\"margin: 0 0 24px 0;\">We appreciate your effort and wish you continued success in the upcoming stage of the recruitment process.</p>\n" +
                    "                    <p style=\"margin: 0;\">Best Regards,<br><br>\n" +
                    "                      <strong style=\"color: #0f172a;\">The BETA Team</strong>\n" +
                    "                    </p>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Footer Divider -->\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding: 32px 0 20px 0;\">\n" +
                    "                    <hr style=\"border: 0; border-top: 1px solid #e2e8f0; margin: 0;\" />\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Footer -->\n" +
                    "                <tr>\n" +
                    "                  <td align=\"center\" style=\"color: #64748b; font-family: inherit; font-size: 12px; line-height: 1.6; text-align: center;\">\n" +
                    "                    This is an automated notification. Please do not reply directly to this email.<br>\n" +
                    "                    &copy; " + year + " BETA. All rights reserved.\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "              </table>\n" +
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
            String linkInfo = (savedApp.getInterviewLink() != null) ? " Meeting Link: " + savedApp.getInterviewLink()
                    : "";
            notificationService.createNotification(
                    savedApp.getId(),
                    "Interview Scheduled",
                    "Your interview has been scheduled for " + savedApp.getInterviewDate() + " at "
                            + savedApp.getInterviewTime() + "." + linkInfo);
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
        if (finalLocation == null || finalLocation.trim().isEmpty()
                || "BETA Office".equalsIgnoreCase(finalLocation.trim())) {
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
                dateFormatted = savedApp.getHrInterviewDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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
            if (locationFormatted == null || locationFormatted.trim().isEmpty()
                    || "BETA Office".equalsIgnoreCase(locationFormatted.trim())) {
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
                    "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "  <title>" + subject + "</title>\n" +
                    "  <style>\n" +
                    "    @media only screen and (max-width: 620px) {\n" +
                    "      .email-content-cell {\n" +
                    "        padding: 32px 20px !important;\n" +
                    "      }\n" +
                    "      .email-wrapper-cell {\n" +
                    "        padding: 24px 12px !important;\n" +
                    "      }\n" +
                    "    }\n" +
                    "  </style>\n" +
                    "</head>\n" +
                    "<body style=\"margin: 0; padding: 0; background-color: #f8fafc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                    "  <table class=\"email-wrapper\" role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f8fafc;\">\n" +
                    "    <tr>\n" +
                    "      <td align=\"center\" class=\"email-wrapper-cell\" style=\"padding: 40px 16px;\">\n" +
                    "        <table class=\"email-container\" role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.045); width: 100%; max-width: 580px;\">\n" +
                    "          <tr>\n" +
                    "            <td class=\"email-content-cell\" style=\"padding: 40px 44px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">\n" +
                    "              <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                    "                <!-- Centered Logo -->\n" +
                    "                <tr>\n" +
                    "                  <td align=\"center\" style=\"padding-bottom: 8px;\">\n" +
                    "                    <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 56px; width: auto; display: block; margin: 0 auto;\" />\n" +
                    "                    <span style=\"font-size: 13px; color: #64748b; font-weight: 500; display: block; margin-top: 8px; font-family: inherit;\">" + escapeHtml(savedApp.getEmail()) + "</span>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Divider -->\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding: 16px 0 24px 0;\">\n" +
                    "                    <hr style=\"border: 0; border-top: 1px solid #e2e8f0; margin: 0;\" />\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Content Body -->\n" +
                    "                <tr>\n" +
                    "                  <td style=\"color: #334155; font-family: inherit; font-size: 15px; line-height: 1.6; text-align: left;\">\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">Dear <strong>" + escapeHtml(candidateName) + "</strong>,</p>\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">Congratulations!</p>\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">We are pleased to inform you that we have reviewed your <strong>Task Assessment</strong> for the position of <strong>" + escapeHtml(jobTitle) + "</strong>. Based on your submission, you have been shortlisted for the <em>final stage (HR Round)</em> of our recruitment process.</p>\n" +
                    "                    <p style=\"margin: 0 0 16px 0;\">The <em>HR Round</em> will be conducted as an in-person interview at our office.</p>\n" +
                    "                    <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"width: 100%;\">\n" +
                    "                      <tr>\n" +
                    "                        <td style=\"padding: 16px 0 20px 0;\">\n" +
                    "                          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; width: 100%;\">\n" +
                    "                            <tr>\n" +
                    "                              <td style=\"padding: 20px 24px; font-family: inherit;\">\n" +
                    "                                <p style=\"margin: 0 0 10px 0; font-size: 14px; color: #334155; line-height: 1.6;\"><strong style=\"color: #0f172a; font-weight: 600;\">Interview Date:</strong> " + dateFormatted + "</p>\n" +
                    "                                <p style=\"margin: 0 0 10px 0; font-size: 14px; color: #334155; line-height: 1.6;\"><strong style=\"color: #0f172a; font-weight: 600;\">Interview Time:</strong> " + timeFormatted + "</p>\n" +
                    "                                <p style=\"margin: 0; font-size: 14px; color: #334155; line-height: 1.6;\"><strong style=\"color: #0f172a; font-weight: 600;\">Venue:</strong> <a href=\"" + mapsUrl + "\" style=\"color: #0284c7; text-decoration: underline; font-weight: 600;\">" + locationFormatted + "</a></p>\n" +
                    "                              </td>\n" +
                    "                            </tr>\n" +
                    "                          </table>\n" +
                    "                        </td>\n" +
                    "                      </tr>\n" +
                    "                    </table>\n" +
                    "                    <p style=\"margin: 0 0 12px 0; font-size: 15px; color: #0f172a; font-weight: 600;\">Important Instructions:</p>\n" +
                    "                    <ul style=\"margin: 0 0 24px 0; padding-left: 0; list-style-type: none; color: #334155; font-size: 15px; line-height: 1.6;\">\n" +
                    "                      <li style=\"margin-bottom: 10px;\">Please report to the venue <strong>10 minutes before</strong> the scheduled interview time.</li>\n" +
                    "                      <li style=\"margin-bottom: 10px;\">Carry your laptop for the interview.</li>\n" +
                    "                      <li style=\"margin-bottom: 10px;\">Bring an updated copy of your resume.</li>\n" +
                    "                    </ul>\n" +
                    "                    <p style=\"margin: 0 0 24px 0;\">We wish you success in the HR Round and look forward to meeting you in person.</p>\n" +
                    "                    <p style=\"margin: 0;\">Best Regards,<br><br>\n" +
                    "                      <strong style=\"color: #0f172a;\">The BETA Team</strong>\n" +
                    "                    </p>\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Footer Divider -->\n" +
                    "                <tr>\n" +
                    "                  <td style=\"padding: 32px 0 20px 0;\">\n" +
                    "                    <hr style=\"border: 0; border-top: 1px solid #e2e8f0; margin: 0;\" />\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "                <!-- Footer -->\n" +
                    "                <tr>\n" +
                    "                  <td align=\"center\" style=\"color: #64748b; font-family: inherit; font-size: 12px; line-height: 1.6; text-align: center;\">\n" +
                    "                    This is an automated notification. Please do not reply directly to this email.<br>\n" +
                    "                    &copy; " + year + " BETA. All rights reserved.\n" +
                    "                  </td>\n" +
                    "                </tr>\n" +
                    "              </table>\n" +
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
        if (status == null)
            return "Updated";
        switch (status.toUpperCase()) {
            case "ACCEPTED":
                return "Accepted";
            case "REJECTED":
                return "Rejected";
            case "SHORTLISTED":
                return "Shortlisted";
            case "SCHEDULED":
                return "Interview Scheduled";
            case "REVIEWED":
                return "Interview Completed";
            case "JOINED":
                return "Joined";
            case "PENDING":
                return "Candidates";
            default:
                return status;
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

package com.admin.portal.service;

import com.admin.portal.entity.JobApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mail.api.token:YOUR_PUBLIC_MAIL_TOKEN}")
    private String mailToken;

    @Value("${mail.from.name:Beta Softnet}")
    private String fromName;

    @Autowired
    @Lazy
    private JobService jobService;

    private static final String EMAIL_API_URL = "https://api.bnxmail.com/api/mail/public/send";

    /**
     * Sends a generic email using the external syper.com public mail API.
     */
    public boolean sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Public-Mail-Token", mailToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", to);
            requestBody.put("subject", subject);
            requestBody.put("body", body);
            requestBody.put("isHtml", isHtml);
            requestBody.put("html", isHtml);
            requestBody.put("fromName", fromName);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            LOGGER.info("Sending email to: " + to + " | Subject: " + subject);
            ResponseEntity<String> response = restTemplate.postForEntity(EMAIL_API_URL, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("Email sent successfully to " + to);
                return true;
            } else {
                LOGGER.warning("Failed to send email. Status code: " + response.getStatusCode() + ", Response: "
                        + response.getBody());
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occurred while sending email to " + to, e);
            return false;
        }
    }

    /**
     * Prepares and sends an application status notification email.
     */
    public void sendApplicationStatusEmail(JobApplication app, String jobTitle) {
        String status = app.getStatus();
        if (status == null) {
            return;
        }

        String candidateName = app.getFullName() != null ? app.getFullName() : "Candidate";
        String displayJobTitle = jobTitle != null ? jobTitle : "Position";
        String subject = "Job Application Status Update - " + displayJobTitle;
        String body = "";

        if ("ACCEPTED".equalsIgnoreCase(status)) {
            subject = "BETA Recruitment – Test Round Invitation";
            body = getAcceptanceEmailTemplate(candidateName, app.getEmail(), displayJobTitle);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            subject = "BETA – Thank You for Your Interest";
            body = getRejectionEmailTemplate(candidateName, app.getEmail(), displayJobTitle);
        } else {
            // No email is sent for other/intermediate statuses like PENDING
            return;
        }

        sendEmail(app.getEmail(), subject, body, true);
    }

    /**
     * Sends an acknowledgment email immediately after the candidate applies.
     */
    public void sendAcknowledgementEmail(JobApplication app, String jobTitle) {
        String candidateName = app.getFullName() != null ? app.getFullName() : "Candidate";
        String displayJobTitle = jobTitle != null ? jobTitle : "Position";
        String subject = "Acknowledgement of Your Job Application";
        String body = getAcknowledgementEmailTemplate(candidateName, app.getEmail(), displayJobTitle);

        sendEmail(app.getEmail(), subject, body, true);
    }

    private String getAcknowledgementEmailTemplate(String candidateName, String candidateEmail, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>Acknowledgement of Your Job Application</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n"
                +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n"
                +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 48px;\">\n"
                +
                "          <!-- Centered Logo -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding-bottom: 12px;\">\n" +
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n"
                +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 10px; font-family: 'Roboto', Arial, sans-serif;\">"
                + escapeHtml(candidateEmail) + "</span>\n" +
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
                "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.8; text-align: left;\">\n"
                +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Thank you for your interest in pursuing a career with <strong>BETA</strong> and for applying for the position of <strong>" + escapeHtml(jobTitle) + "</strong>.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                We have successfully received your application. Our hiring team will review your profile, and any further updates will be shared with you via <strong>BNXmail</strong> soon.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 36px 0;\">\n" +
                "                Thank you once again for considering <strong>BETA</strong> as part of your professional journey. We wish you every success and look forward to staying in touch.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 8px 0;\">\n" +
                "                Best Regards,<br><br>\n" +
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
                "            <td align=\"center\" style=\"color: #70757a; font-family: 'Roboto', Arial, sans-serif; font-size: 12px; line-height: 1.5; text-align: center;\">\n"
                +
                "              This is an automated notification. Please do not reply directly to this email.<br>\n" +
                "              &copy; " + year + " " + escapeHtml(fromName) + ". All rights reserved.\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private String getAcceptanceEmailTemplate(String candidateName, String candidateEmail, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>BETA Recruitment – Test Round Invitation</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n"
                +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n"
                +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 48px;\">\n"
                +
                "          <!-- Centered Logo -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding-bottom: 12px;\">\n" +
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n"
                +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 10px; font-family: 'Roboto', Arial, sans-serif;\">"
                + escapeHtml(candidateEmail) + "</span>\n" +
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
                "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.8; text-align: left;\">\n"
                +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                We are delighted to have received your application.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                We are pleased to inform you that your profile has been reviewed and shortlisted for the next stage of our recruitment process.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 12px 0;\">\n" +
                "                Our hiring process consists of the following four stages:\n" +
                "              </p>\n" +
                "              <ol style=\"margin: 0 0 24px 0; padding-left: 20px;\">\n" +
                "                <li style=\"margin-bottom: 8px;\">Test Round</li>\n" +
                "                <li style=\"margin-bottom: 8px;\">Technical Interview</li>\n" +
                "                <li style=\"margin-bottom: 8px;\">Task Assessment</li>\n" +
                "                <li style=\"margin-bottom: 8px;\">HR Interview</li>\n" +
                "              </ol>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                The Test Round details and assessment link will be shared with you soon via <strong>BNXmail</strong>.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 36px 0;\">\n" +
                "                We appreciate your interest in <strong>BETA</strong> and wish you every success.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 8px 0;\">\n" +
                "                Best Regards,<br><br>\n" +
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
                "            <td align=\"center\" style=\"color: #70757a; font-family: 'Roboto', Arial, sans-serif; font-size: 12px; line-height: 1.5; text-align: center;\">\n"
                +
                "              This is an automated notification. Please do not reply directly to this email.<br>\n" +
                "              &copy; " + year + " " + escapeHtml(fromName) + ". All rights reserved.\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private String getRejectionEmailTemplate(String candidateName, String candidateEmail, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>BETA – Thank You for Your Interest</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n"
                +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n"
                +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 48px;\">\n"
                +
                "          <!-- Centered Logo -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding-bottom: 12px;\">\n" +
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n"
                +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 10px; font-family: 'Roboto', Arial, sans-serif;\">"
                + escapeHtml(candidateEmail) + "</span>\n" +
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
                "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.8; text-align: left;\">\n"
                +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Dear " + escapeHtml(candidateName) + ",\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Thank you for your interest in <strong>BETA</strong> and for taking the time to participate in our recruitment process.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                After careful consideration, we regret to inform you that you have not been selected to proceed further in the recruitment process.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                We sincerely appreciate the time, effort, and interest you have shown in joining our organization. While we are unable to move forward with your application at this time, we encourage you to apply for future opportunities at <strong>BETA</strong> that match your skills and experience.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 36px 0;\">\n" +
                "                We wish you every success in your future career and thank you once again for considering <strong>BETA</strong>.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 8px 0;\">\n" +
                "                Best Regards,<br><br>\n" +
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
                "            <td align=\"center\" style=\"color: #70757a; font-family: 'Roboto', Arial, sans-serif; font-size: 12px; line-height: 1.5; text-align: center;\">\n"
                +
                "              This is an automated notification. Please do not reply directly to this email.<br>\n" +
                "              &copy; " + year + " " + escapeHtml(fromName) + ". All rights reserved.\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }

    public void sendAssessmentEmail(JobApplication app) {
        String candidateName = app.getFullName() != null ? app.getFullName() : "Candidate";
        String candidateEmail = app.getEmail();
        String assessmentLink = "https://www.beta-softnet.com/careers/assessment?id=" + app.getId();
        
        String jobTitle = "the Position";
        if (app.getJobId() != null && jobService != null) {
            jobTitle = jobService.getJobById(app.getJobId())
                    .map(job -> job.getTitle())
                    .orElse("the Position");
        }

        String subject = "BETA – Test Round Invitation";

        String body = getAssessmentEmailTemplate(candidateName, candidateEmail, assessmentLink, jobTitle);

        sendEmail(candidateEmail, subject, body, true);
    }

    private String getAssessmentEmailTemplate(String candidateName, String candidateEmail, String assessmentLink, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>BETA Recruitment – Test Round Invitation</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n"
                +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n"
                +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 48px;\">\n"
                +
                "          <!-- Centered Logo -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding-bottom: 12px;\">\n" +
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n"
                +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 10px; font-family: 'Roboto', Arial, sans-serif;\">"
                + escapeHtml(candidateEmail) + "</span>\n" +
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
                "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.8; text-align: left;\">\n"
                +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                We are delighted to have received your application.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                We are pleased to inform you that your application for the position of <strong>" + escapeHtml(jobTitle) + "</strong> has been reviewed, and you have been shortlisted for the first stage of our recruitment process.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 12px 0;\">\n" +
                "                Our hiring process consists of the following four stages:\n" +
                "              </p>\n" +
                "              <ol style=\"margin: 0 0 24px 0; padding-left: 20px;\">\n" +
                "                <li style=\"margin-bottom: 8px;\">Test Round</li>\n" +
                "                <li style=\"margin-bottom: 8px;\">Technical Interview</li>\n" +
                "                <li style=\"margin-bottom: 8px;\">Task Assessment</li>\n" +
                "                <li style=\"margin-bottom: 8px;\">HR Interview</li>\n" +
                "              </ol>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Please complete the Test Round using the assessment link provided below.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                <strong>Assessment Link :</strong> <a href=\"" + assessmentLink
                + "\" style=\"color: #004AAD; text-decoration: underline;\">" + assessmentLink + "</a>\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 12px 0;\">\n" +
                "                <strong>Important Instructions :</strong>\n" +
                "              </p>\n" +
                "              <ul style=\"margin: 0 0 24px 0; padding-left: 20px;\">\n" +
                "                <li style=\"margin-bottom: 8px;\">This assessment link is valid for 24 hours from the time this email is sent. Once the 24-hour period expires, the link will be automatically deactivated and will no longer be accessible.</li>\n"
                +
                "                <li style=\"margin-bottom: 8px;\">You are allowed a maximum of two attempts to complete this assessment.</li>\n"
                +
                "                <li style=\"margin-bottom: 8px;\">Please ensure you submit your final attempt carefully, as no additional attempts will be provided.</li>\n"
                +
                "                <li style=\"margin-bottom: 8px;\">Complete the assessment within the specified time limit.</li>\n"
                +
                "              </ul>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Candidates who successfully complete the Test Round will receive further instructions via <strong>BNXmail</strong>.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 36px 0;\">\n" +
                "                We wish you the very best for the Test Round.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 8px 0;\">\n" +
                "                Best Regards,<br><br>\n" +
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
                "            <td align=\"center\" style=\"color: #70757a; font-family: 'Roboto', Arial, sans-serif; font-size: 12px; line-height: 1.5; text-align: center;\">\n"
                +
                "              This is an automated notification. Please do not reply directly to this email.<br>\n" +
                "              &copy; " + year + " " + escapeHtml(fromName) + ". All rights reserved.\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
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

    public void sendTaskAssessmentEmail(JobApplication app, String taskDescription) {
        String candidateName = app.getFullName() != null ? app.getFullName() : "Candidate";
        String subject = "BETA - Successfully Cleared the Technical Interview";
        int year = java.time.LocalDate.now().getYear();
        String taskLink = "https://www.beta-softnet.com/careers/task-assessment?id=" + app.getId();

        String jobTitle = "the Position";
        if (app.getJobId() != null && jobService != null) {
            jobTitle = jobService.getJobById(app.getJobId())
                    .map(job -> job.getTitle())
                    .orElse("the Position");
        }

        String body = "<!DOCTYPE html>\n" +
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
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\">\n" +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 10px; font-family: 'Roboto', Arial, sans-serif;\">" + escapeHtml(app.getEmail()) + "</span>\n" +
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
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Congratulations !\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                We are pleased to inform you that you have successfully cleared the <strong>Technical Interview</strong> for the position of <strong>" + escapeHtml(jobTitle) + "</strong> and have progressed to the third stage of our recruitment process.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                You have been shortlisted for the <strong>Task Assessment</strong> .\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Please complete the Task provided below.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 8px 0;\">\n" +
                "                <strong>Task Assessment :</strong>\n" +
                "              </p>\n" +
                "              <div style=\"background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px; margin-bottom: 24px; font-family: inherit; font-size: 14px; white-space: pre-line; color: #333; line-height: 1.6;\">\n" +
                "                " + escapeHtml(taskDescription) + "\n" +
                "              </div>\n" +
                "              <p style=\"margin: 0 0 8px 0;\">\n" +
                "                <strong>Paste GitHub Repository Link :</strong>\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                <a href=\"" + taskLink + "\" style=\"color: #004AAD; text-decoration: underline;\">" + taskLink + "</a>\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0;\">\n" +
                "                Please ensure that your GitHub repository is accessible and contains all the required project files before submitting.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 24px 0; color: #d93025; font-weight: bold;\">\n" +
                "                Important : You are required to complete the Task and submit your GitHub repository link within 48 hours of receiving this email. Please note that submissions received after the deadline may not be considered.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 36px 0;\">\n" +
                "                We wish you the very best for the Task Assessment.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 8px 0;\">\n" +
                "                Best Regards,<br><br>\n" +
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
                "              &copy; " + year + " " + escapeHtml(fromName) + ". All rights reserved.\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";

        sendEmail(app.getEmail(), subject, body, true);
    }
}

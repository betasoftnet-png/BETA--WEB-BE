package com.admin.portal.service;

import com.admin.portal.entity.JobApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
            subject = "BETA Recruitment – Task Round Invitation";
            body = getAcceptanceEmailTemplate(candidateName, app.getEmail(), displayJobTitle);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
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
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 40px;\">\n" +
                "          <!-- Centered Logo -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding-bottom: 10px;\">\n" +
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n" +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 8px; font-family: 'Roboto', Arial, sans-serif;\">" + escapeHtml(candidateEmail) + "</span>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 10px 0 20px 0;\">\n" +
                "              <hr style=\"border: 0; border-top: 1px solid #dadce0; margin: 0;\" />\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Content Body -->\n" +
                "          <tr>\n" +
                "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.6; text-align: left;\">\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                Thank you for your interest in pursuing a career with <strong>BETA</strong>.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                We have successfully received your application. Our hiring team will review your profile, and any further updates will be shared with you via <strong>BNXmail</strong> soon.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 30px 0;\">\n" +
                "                Thank you once again for considering <strong>BETA</strong> as part of your professional journey. We wish you every success and look forward to staying in touch.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0;\">\n" +
                "                Best Regards,<br><br>\n" +
                "                <strong>The BETA Team</strong>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Footer Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 30px 0 15px 0;\">\n" +
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
    }

    private String getAcceptanceEmailTemplate(String candidateName, String candidateEmail, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>BETA Recruitment – Task Round Invitation</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 40px;\">\n" +
                "          <!-- Centered Logo -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding-bottom: 10px;\">\n" +
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n" +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 8px; font-family: 'Roboto', Arial, sans-serif;\">" + escapeHtml(candidateEmail) + "</span>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 10px 0 20px 0;\">\n" +
                "              <hr style=\"border: 0; border-top: 1px solid #dadce0; margin: 0;\" />\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Content Body -->\n" +
                "          <tr>\n" +
                "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.6; text-align: left;\">\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                We are delighted to have received your application.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                We are pleased to inform you that your profile has been reviewed and shortlisted for the next stage of our recruitment process.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 10px 0;\">\n" +
                "                Our hiring process consists of the following four stages:\n" +
                "              </p>\n" +
                "              <ol style=\"margin: 0 0 20px 0; padding-left: 20px;\">\n" +
                "                <li style=\"margin-bottom: 5px;\">Task Round</li>\n" +
                "                <li style=\"margin-bottom: 5px;\">Online Round</li>\n" +
                "                <li style=\"margin-bottom: 5px;\">Technical Interview</li>\n" +
                "                <li style=\"margin-bottom: 5px;\">HR Interview</li>\n" +
                "              </ol>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                The Task Round details and assessment link will be shared with you soon via <strong>BNXmail</strong>.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 30px 0;\">\n" +
                "                We appreciate your interest in <strong>BETA</strong> and wish you every success.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0;\">\n" +
                "                Best Regards,<br><br>\n" +
                "                <strong>The BETA Team</strong>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Footer Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 30px 0 15px 0;\">\n" +
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
    }

    private String getRejectionEmailTemplate(String candidateName, String candidateEmail, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>Application Status Update</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Roboto', Arial, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"580\" style=\"background-color: #ffffff; border: 1px solid #dadce0; border-radius: 8px; overflow: hidden; padding: 40px;\">\n" +
                "          <!-- Centered Logo -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding-bottom: 10px;\">\n" +
                "              <img src=\"https://beta-softnet.com/logo.png\" alt=\"BETA Logo\" style=\"height: 60px; width: auto; display: block; margin: 0 auto;\" />\n" +
                "              <span style=\"font-size: 13px; color: #5f6368; display: block; margin-top: 8px; font-family: 'Roboto', Arial, sans-serif;\">" + escapeHtml(candidateEmail) + "</span>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 10px 0 20px 0;\">\n" +
                "              <hr style=\"border: 0; border-top: 1px solid #dadce0; margin: 0;\" />\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Content Body -->\n" +
                "          <tr>\n" +
                "            <td style=\"color: #202124; font-family: 'Roboto', Arial, sans-serif; font-size: 15px; line-height: 1.6; text-align: left;\">\n" +
                "              <h2 style=\"margin: 0 0 20px 0; color: #374151; font-size: 20px; font-weight: 600;\">Application Update</h2>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                Thank you for your interest in the <strong>" + escapeHtml(jobTitle) + "</strong> position and for taking the time to connect with us. We truly appreciate the opportunity to learn more about your skills and experience.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                After careful consideration, we regret to inform you that we have decided to move forward with other candidates whose experience more closely aligns with our current needs for this role.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 30px 0;\">\n" +
                "                We will keep your resume on file and reach out if any future opportunities matching your background open up. We wish you the very best in your job search and your future professional endeavors.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0;\">\n" +
                "                Best regards,<br><br>\n" +
                "                <strong>The Hiring Team</strong>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Footer Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 30px 0 15px 0;\">\n" +
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

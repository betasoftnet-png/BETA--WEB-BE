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
            subject = "BETA Recruitment – Test Round Invitation";
            body = getAcceptanceEmailTemplate(candidateName, app.getEmail(), displayJobTitle);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            subject = "Update on Your Job Application – BETA Recruitment";
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
                "  <title>BETA Recruitment – Test Round Invitation</title>\n" +
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
                "                <li style=\"margin-bottom: 5px;\">Test Round</li>\n" +
                "                <li style=\"margin-bottom: 5px;\">Online Round</li>\n" +
                "                <li style=\"margin-bottom: 5px;\">Technical Interview</li>\n" +
                "                <li style=\"margin-bottom: 5px;\">HR Interview</li>\n" +
                "              </ol>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                The Test Round details and assessment link will be shared with you soon via <strong>BNXmail</strong>.\n" +
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
                "  <title>Update on Your Job Application – BETA Recruitment</title>\n" +
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
                "                After carefully reviewing your profile, we regret to inform you that you have not been shortlisted for the next stage of our recruitment process.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                This decision was made after evaluating your profile against the current requirements of the role. We encourage you to continue developing your skills and wish you success in your future career.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                We appreciate the time and effort you invested in applying to <strong>BETA</strong> and thank you for considering us as part of your professional journey.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 30px 0;\">\n" +
                "                We wish you all the best in your future endeavors.\n" +
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

    public void sendAssessmentEmail(JobApplication app) {
        String candidateName = app.getFullName() != null ? app.getFullName() : "Candidate";
        String candidateEmail = app.getEmail();
        String assessmentLink = "https://apply.beta-softnet.com/careers/assessment?id=" + app.getId();
        String subject = "Test Round – Assessment Link";

        String body = getAssessmentEmailTemplate(candidateName, candidateEmail, assessmentLink);

        sendEmail(candidateEmail, subject, body, true);
    }

    private String getAssessmentEmailTemplate(String candidateName, String candidateEmail, String assessmentLink) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>Test Round – Assessment Link</title>\n" +
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
                "                As the first stage in our recruitment process, please complete the Test Round assessment using the link below.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                <strong>Assessment Link:</strong> <a href=\"" + assessmentLink + "\" style=\"color: #004AAD; text-decoration: underline;\">" + assessmentLink + "</a>\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                <strong>Important:</strong> This assessment link is valid for <strong>24 hours</strong> from the time this email is sent. Once the 24-hour period expires, the link will be automatically deactivated and will no longer be accessible. Please ensure that you complete and submit your assessment within the allotted time.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0;\">\n" +
                "                Candidates who successfully complete the Test Round will receive further instructions via <strong>BNXmail</strong>.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 30px 0;\">\n" +
                "                We wish you the very best for the Test Round.\n" +
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

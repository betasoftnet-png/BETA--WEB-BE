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
            body = getAcceptanceEmailTemplate(candidateName, displayJobTitle);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            body = getRejectionEmailTemplate(candidateName, displayJobTitle);
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
        String body = getAcknowledgementEmailTemplate(candidateName, displayJobTitle);

        sendEmail(app.getEmail(), subject, body, true);
    }

    private String getAcknowledgementEmailTemplate(String candidateName, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>Acknowledgement of Your Job Application</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);\">\n" +
                "          <!-- Header Banner -->\n" +
                "          <tr>\n" +
                "            <td style=\"background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); padding: 45px 30px; text-align: center; color: #ffffff;\">\n" +
                "              <h1 style=\"margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; font-family: sans-serif;\">Acknowledgement of Your Job Application</h1>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Content Body -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 40px 30px; color: #1f2937;\">\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n" +
                "                Thank you for your interest in pursuing a career with <strong>BETA</strong>.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n" +
                "                We have successfully received your application. Our hiring team will review your profile, and any further updates will be shared with you via <strong>BNXmail</strong> soon.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n" +
                "                Thank you once again for considering <strong>BETA</strong> as part of your professional journey. We wish you every success and look forward to staying in touch.\n" +
                "              </p>\n" +
                "              \n" +
                "              <!-- Decorative Line -->\n" +
                "              <hr style=\"border: 0; border-top: 1px solid #e5e7eb; margin: 30px 0;\">\n" +
                "              \n" +
                "              <p style=\"margin: 0; font-size: 15px; line-height: 1.6; color: #4b5563; font-family: sans-serif;\">\n" +
                "                Best Regards,<br><br>\n" +
                "                <strong>The BETA Team</strong>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Footer -->\n" +
                "          <tr>\n" +
                "            <td style=\"background-color: #f9fafb; padding: 20px 30px; text-align: center; border-top: 1px solid #f3f4f6; color: #9ca3af; font-size: 12px; line-height: 1.5; font-family: sans-serif;\">\n" +
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

    private String getAcceptanceEmailTemplate(String candidateName, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>Application Status Update - Accepted</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n" +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);\">\n" +
                "          <!-- Header Banner -->\n" +
                "          <tr>\n" +
                "            <td style=\"background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 45px 30px; text-align: center; color: #ffffff;\">\n" +
                "              <h1 style=\"margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; font-family: sans-serif;\">Congratulations!</h1>\n" +
                "              <p style=\"margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; font-family: sans-serif;\">Your Application Status Update</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Content Body -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 40px 30px; color: #1f2937;\">\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n" +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n" +
                "                We are thrilled to inform you that your application for the <strong>" + escapeHtml(jobTitle) + "</strong> position has been <strong>accepted</strong>! We were highly impressed by your qualifications and interview performance.\n" +
                "              </p>\n" +
                "              \n" +
                "              <p style=\"margin: 30px 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n" +
                "                Our HR team will reach out to you shortly with the official offer letter and next steps regarding the onboarding process.\n" +
                "              </p>\n" +
                "              \n" +
                "              <!-- Decorative Line -->\n" +
                "              <hr style=\"border: 0; border-top: 1px solid #e5e7eb; margin: 30px 0;\">\n" +
                "              \n" +
                "              <p style=\"margin: 0; font-size: 15px; line-height: 1.6; color: #4b5563; font-family: sans-serif;\">\n" +
                "                Best regards,<br>\n" +
                "                <strong>The Hiring Team</strong>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Footer -->\n" +
                "          <tr>\n" +
                "            <td style=\"background-color: #f9fafb; padding: 20px 30px; text-align: center; border-top: 1px solid #f3f4f6; color: #9ca3af; font-size: 12px; line-height: 1.5; font-family: sans-serif;\">\n" +
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

    private String getRejectionEmailTemplate(String candidateName, String jobTitle) {
        int year = LocalDate.now().getYear();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>Application Status Update</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f5f7; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;\">\n"
                +
                "  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f5f7; padding: 40px 0;\">\n"
                +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);\">\n"
                +
                "          <!-- Header Banner -->\n" +
                "          <tr>\n" +
                "            <td style=\"background: linear-gradient(135deg, #4b5563 0%, #1f2937 100%); padding: 45px 30px; text-align: center; color: #ffffff;\">\n"
                +
                "              <h1 style=\"margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; font-family: sans-serif;\">Application Update</h1>\n"
                +
                "              <p style=\"margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; font-family: sans-serif;\">Job Application Status Update</p>\n"
                +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Content Body -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 40px 30px; color: #1f2937;\">\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n"
                +
                "                Dear <strong>" + escapeHtml(candidateName) + "</strong>,\n" +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n"
                +
                "                Thank you for your interest in the <strong>" + escapeHtml(jobTitle)
                + "</strong> position and for taking the time to connect with us. We truly appreciate the opportunity to learn more about your skills and experience.\n"
                +
                "              </p>\n" +
                "              <p style=\"margin: 0 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n"
                +
                "                After careful consideration, we regret to inform you that we have decided to move forward with other candidates whose experience more closely aligns with our current needs for this role.\n"
                +
                "              </p>\n" +
                "              \n" +
                "              <p style=\"margin: 30px 0 20px 0; font-size: 16px; line-height: 1.6; color: #374151; font-family: sans-serif;\">\n"
                +
                "                We will keep your resume on file and reach out if any future opportunities matching your background open up. We wish you the very best in your job search and your future professional endeavors.\n"
                +
                "              </p>\n" +
                "              \n" +
                "              <!-- Decorative Line -->\n" +
                "              <hr style=\"border: 0; border-top: 1px solid #e5e7eb; margin: 30px 0;\">\n" +
                "              \n" +
                "              <p style=\"margin: 0; font-size: 15px; line-height: 1.6; color: #4b5563; font-family: sans-serif;\">\n"
                +
                "                Best regards,<br>\n" +
                "                <strong>The Hiring Team</strong>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <!-- Footer -->\n" +
                "          <tr>\n" +
                "            <td style=\"background-color: #f9fafb; padding: 20px 30px; text-align: center; border-top: 1px solid #f3f4f6; color: #9ca3af; font-size: 12px; line-height: 1.5; font-family: sans-serif;\">\n"
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
}

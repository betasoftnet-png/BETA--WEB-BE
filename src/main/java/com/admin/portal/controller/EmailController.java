package com.admin.portal.controller;

import com.admin.portal.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping({"/send", "/public/send"})
    public ResponseEntity<?> sendMail(@RequestBody EmailRequest request) {
        // Fallback for HTML flags
        boolean isHtml = request.isHtml() || (request.getHtml() != null && request.getHtml());
        
        boolean success = emailService.sendEmail(
                request.getTo(),
                request.getSubject(),
                request.getBody(),
                isHtml
        );

        if (success) {
            return ResponseEntity.ok(java.util.Map.of("message", "Email sent successfully"));
        } else {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Failed to send email"));
        }
    }

    public static class EmailRequest {
        private String to;
        private String subject;
        private String body;
        private boolean isHtml;
        private Boolean html;
        private String fromName;

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public boolean isHtml() {
            return isHtml;
        }

        public void setHtml(boolean isHtml) {
            this.isHtml = isHtml;
        }

        public Boolean getHtml() {
            return html;
        }

        public void setHtml(Boolean html) {
            this.html = html;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }
    }
}

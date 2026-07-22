package com.admin.portal.controller;

import com.admin.portal.dto.request.AssessmentRequest;
import com.admin.portal.dto.request.QuestionDTO;
import com.admin.portal.entity.JobApplication;
import com.admin.portal.entity.Job;
import com.admin.portal.repository.JobApplicationRepository;
import com.admin.portal.service.JobService;
import com.admin.portal.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private JobService jobService;

    // HR sends assessment
    @PostMapping("/send")
    public String sendAssessment(@RequestBody AssessmentRequest request) {
        Integer duration = request.getDuration();
        if (duration == null) {
            duration = 30;
        }
        assessmentService.assignQuestions(
                request.getCandidateId(),
                request.getQuestionIds(),
                duration);

        return "Assessment sent successfully";
    }

    // Admin gets assigned questions for candidate details view
    @GetMapping("/admin/{candidateId}")
    public ResponseEntity<?> getAssignedQuestionsForAdmin(@PathVariable Long candidateId) {
        try {
            List<QuestionDTO> questions = assessmentService.getAssignedQuestionsForAdmin(candidateId);
            return ResponseEntity.ok(questions);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private JobApplication resolveApplication(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String idStr = identifier.trim();
        java.util.Optional<JobApplication> byToken = jobApplicationRepository.findByAssessmentToken(idStr);
        if (byToken.isPresent()) {
            return byToken.get();
        }
        try {
            Long id = Long.parseLong(idStr);
            return jobApplicationRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Candidate gets assigned questions
    @GetMapping("/{identifier}")
    public ResponseEntity<?> getQuestions(
            @PathVariable String identifier,
            @RequestParam(value = "increment", defaultValue = "false") boolean increment) {
        try {
            JobApplication app = resolveApplication(identifier);
            if (app == null) {
                return ResponseEntity.badRequest().body("Invalid or expired assessment token.");
            }
            Long candidateId = app.getId();

            java.time.LocalDateTime expiry = app.getAssessmentExpiryTime();
            if (expiry == null && app.getAssessmentSentTime() != null) {
                expiry = app.getAssessmentSentTime().plusHours(24);
            }
            if (expiry != null && java.time.LocalDateTime.now().isAfter(expiry)) {
                return ResponseEntity.badRequest().body("This assessment link has expired. Please contact the administrator.");
            }

            if (Boolean.TRUE.equals(app.getAssessmentSubmitted())) {
                String fullName = app.getFullName();
                String jobTitle = "BNX Mail Strategist";
                if (app.getJobId() != null) {
                    Job job = jobService.getJobById(app.getJobId()).orElse(null);
                    if (job != null) {
                        jobTitle = job.getTitle();
                    }
                }
                return ResponseEntity.ok(java.util.Map.of(
                        "candidateId", candidateId,
                        "assessmentToken", app.getAssessmentToken() != null ? app.getAssessmentToken() : "",
                        "candidateName", (fullName != null) ? fullName : "Guest Candidate",
                        "jobTitle", jobTitle,
                        "questions", java.util.Collections.emptyList(),
                        "attempts", app.getAssessmentAttempts() != null ? app.getAssessmentAttempts() : 0,
                        "submitted", true,
                        "score", app.getAptitudeScore() != null ? app.getAptitudeScore() : 0));
            }

            List<QuestionDTO> questions = assessmentService.getQuestionsForCandidate(candidateId, increment);

            app = jobApplicationRepository.findById(candidateId).orElse(app);
            String fullName = (app != null) ? app.getFullName() : "Guest Candidate";
            String jobTitle = "BNX Mail Strategist";

            if (app != null && app.getJobId() != null) {
                Job job = jobService.getJobById(app.getJobId()).orElse(null);
                if (job != null) {
                    jobTitle = job.getTitle();
                }
            }

            int attempts = (app != null) ? app.getAssessmentAttempts() : 0;
            return ResponseEntity.ok(java.util.Map.of(
                    "candidateId", candidateId,
                    "assessmentToken", (app != null && app.getAssessmentToken() != null) ? app.getAssessmentToken() : "",
                    "candidateName", fullName,
                    "jobTitle", jobTitle,
                    "questions", questions,
                    "attempts", attempts));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Admin resets candidate attempts / re-opens assessment
    @PostMapping("/{identifier}/reset")
    public ResponseEntity<?> resetAssessment(@PathVariable String identifier) {
        try {
            JobApplication app = resolveApplication(identifier);
            if (app == null) {
                return ResponseEntity.badRequest().body("Invalid assessment token or candidate ID.");
            }
            assessmentService.resetAssessment(app.getId());
            return ResponseEntity.ok(java.util.Map.of("message", "Assessment reset successfully. Candidate can now open and take the test."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Candidate switches tab or navigates away (security violation)
    @PostMapping("/{identifier}/increment-attempt")
    public ResponseEntity<?> incrementAttempt(@PathVariable String identifier) {
        try {
            JobApplication app = resolveApplication(identifier);
            if (app == null) {
                return ResponseEntity.badRequest().body("Invalid or expired assessment token.");
            }
            Integer attempts = assessmentService.incrementAssessmentAttempts(app.getId());
            return ResponseEntity.ok(attempts);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Candidate submits/finalizes assessment
    @PostMapping("/{identifier}/submit")
    public ResponseEntity<?> submitAssessment(
            @PathVariable String identifier,
            @RequestParam(value = "timeTaken", required = false) String timeTaken) {
        try {
            JobApplication app = resolveApplication(identifier);
            if (app == null) {
                return ResponseEntity.badRequest().body("Invalid or expired assessment token.");
            }
            Integer score = assessmentService.submitAssessment(app.getId(), timeTaken);
            return ResponseEntity.ok(java.util.Map.of("message", "Assessment submitted successfully.", "score", score));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
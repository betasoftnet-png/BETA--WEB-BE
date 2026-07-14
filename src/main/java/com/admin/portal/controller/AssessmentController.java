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

    // Candidate gets assigned questions
    @GetMapping("/{candidateId}")
    public ResponseEntity<?> getQuestions(
            @PathVariable Long candidateId,
            @RequestParam(value = "increment", defaultValue = "true") boolean increment) {
        try {
            JobApplication app = jobApplicationRepository.findById(candidateId).orElse(null);
            if (app != null && Boolean.TRUE.equals(app.getAssessmentSubmitted())) {
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
                        "candidateName", (fullName != null) ? fullName : "Guest Candidate",
                        "jobTitle", jobTitle,
                        "questions", java.util.Collections.emptyList(),
                        "attempts", app.getAssessmentAttempts() != null ? app.getAssessmentAttempts() : 0,
                        "submitted", true,
                        "score", app.getAptitudeScore() != null ? app.getAptitudeScore() : 0));
            }

            List<QuestionDTO> questions = assessmentService.getQuestionsForCandidate(candidateId, increment);

            app = jobApplicationRepository.findById(candidateId).orElse(null);
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
                    "candidateName", fullName,
                    "jobTitle", jobTitle,
                    "questions", questions,
                    "attempts", attempts));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Candidate switches tab or navigates away (security violation)
    @PostMapping("/{candidateId}/increment-attempt")
    public ResponseEntity<?> incrementAttempt(@PathVariable Long candidateId) {
        try {
            Integer attempts = assessmentService.incrementAssessmentAttempts(candidateId);
            return ResponseEntity.ok(attempts);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Candidate submits/finalizes assessment
    @PostMapping("/{candidateId}/submit")
    public ResponseEntity<?> submitAssessment(
            @PathVariable Long candidateId,
            @RequestParam(value = "timeTaken", required = false) String timeTaken) {
        try {
            Integer score = assessmentService.submitAssessment(candidateId, timeTaken);
            return ResponseEntity.ok(java.util.Map.of("message", "Assessment submitted successfully.", "score", score));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
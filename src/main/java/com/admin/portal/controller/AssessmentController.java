package com.admin.portal.controller;

import com.admin.portal.dto.request.AssessmentRequest;
import com.admin.portal.dto.request.QuestionDTO;
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
    public ResponseEntity<?> getQuestions(@PathVariable Long candidateId) {
        try {
            List<QuestionDTO> questions = assessmentService.getQuestionsForCandidate(candidateId);
            return ResponseEntity.ok(questions);
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
    public ResponseEntity<?> submitAssessment(@PathVariable Long candidateId) {
        try {
            assessmentService.submitAssessment(candidateId);
            return ResponseEntity.ok("Assessment submitted successfully.");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
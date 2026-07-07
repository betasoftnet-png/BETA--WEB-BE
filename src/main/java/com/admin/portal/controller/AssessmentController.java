package com.admin.portal.controller;

import com.admin.portal.dto.request.AssessmentRequest;
import com.admin.portal.entity.Question;
import com.admin.portal.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    // HR sends assessment
    @PostMapping("/send")
    public String sendAssessment(@RequestBody AssessmentRequest request) {

        assessmentService.assignQuestions(
                request.getCandidateId(),
                request.getQuestionIds());

        return "Assessment sent successfully";
    }

    // Candidate gets assigned questions
    @GetMapping("/{candidateId}")
    public List<Question> getQuestions(@PathVariable Long candidateId) {

        return assessmentService.getQuestionsForCandidate(candidateId);
    }
}
package com.admin.portal.controller;

import com.admin.portal.dto.request.AssessmentRequest;
import com.admin.portal.dto.request.QuestionDTO;
import com.admin.portal.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<QuestionDTO> getQuestions(@PathVariable Long candidateId) {

        return assessmentService.getQuestionsForCandidate(candidateId);
    }
}
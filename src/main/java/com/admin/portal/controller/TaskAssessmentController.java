package com.admin.portal.controller;

import com.admin.portal.dto.request.TaskRequest;
import com.admin.portal.entity.JobApplication;
import com.admin.portal.entity.TaskAssessment;
import com.admin.portal.repository.JobApplicationRepository;
import com.admin.portal.service.TaskAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task-assessment")
public class TaskAssessmentController {

    private final TaskAssessmentService taskService;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    public TaskAssessmentController(TaskAssessmentService taskService) {
        this.taskService = taskService;
    }

    private Long resolveCandidateId(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String s = identifier.trim();
        java.util.Optional<JobApplication> byToken = jobApplicationRepository.findByAssessmentToken(s);
        if (byToken.isPresent()) {
            return byToken.get().getId();
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PostMapping("/{identifier}")
    public ResponseEntity<?> assignTask(
            @PathVariable String identifier,
            @RequestBody TaskRequest request) {
        Long candidateId = resolveCandidateId(identifier);
        if (candidateId == null) {
            return ResponseEntity.badRequest().body("Invalid candidate identifier");
        }
        return ResponseEntity.ok(taskService.assignTask(candidateId, request));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<TaskAssessment> getTask(@PathVariable String identifier) {
        try {
            Long candidateId = resolveCandidateId(identifier);
            if (candidateId == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(taskService.getTask(candidateId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{identifier}/submit")
    public ResponseEntity<?> submitTask(
            @PathVariable String identifier,
            @RequestBody java.util.Map<String, String> payload) {
        Long candidateId = resolveCandidateId(identifier);
        if (candidateId == null) {
            return ResponseEntity.badRequest().body("Invalid candidate identifier");
        }
        String githubLink = payload.get("githubLink");
        return ResponseEntity.ok(taskService.submitTask(candidateId, githubLink));
    }
}
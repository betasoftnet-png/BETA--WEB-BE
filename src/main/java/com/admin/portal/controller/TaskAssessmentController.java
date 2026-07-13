package com.admin.portal.controller;

import com.admin.portal.dto.request.TaskRequest;
import com.admin.portal.entity.TaskAssessment;
import com.admin.portal.service.TaskAssessmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task-assessment")
@CrossOrigin(origins = "*")
public class TaskAssessmentController {

    private final TaskAssessmentService taskService;

    public TaskAssessmentController(TaskAssessmentService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{candidateId}")
    public ResponseEntity<TaskAssessment> assignTask(
            @PathVariable Long candidateId,
            @RequestBody TaskRequest request) {

        return ResponseEntity.ok(
                taskService.assignTask(candidateId, request));
    }

    @GetMapping("/{candidateId}")
    public ResponseEntity<TaskAssessment> getTask(@PathVariable Long candidateId) {
        try {
            return ResponseEntity.ok(taskService.getTask(candidateId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
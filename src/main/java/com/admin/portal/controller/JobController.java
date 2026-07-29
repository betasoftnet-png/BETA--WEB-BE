package com.admin.portal.controller;

import com.admin.portal.entity.Job;
import com.admin.portal.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    // Create Job
    @PostMapping
    public Job createJob(@RequestBody Job job) {
        return jobService.createJob(job);
    }

    // Get All Jobs (with optional status filter)
    @GetMapping
    public List<Job> getAllJobs(@RequestParam(required = false) String status) {
        if (status != null) {
            return jobService.getJobsByStatus(status);
        }
        return jobService.getActiveJobs();
    }

    // Get Job By ID
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update Job
    @PutMapping("/{id}")
    public Job updateJob(@PathVariable Long id, @RequestBody Job job) {
        job.setId(id);
        return jobService.updateJob(job);
    }

    // Delete Job
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }

    // Permanent Delete Job (only if status is DELETED)
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> deleteJobPermanently(@PathVariable Long id) {
        try {
            jobService.deleteJobPermanently(id);
            return ResponseEntity.ok(java.util.Map.of("message", "Job permanently deleted successfully."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
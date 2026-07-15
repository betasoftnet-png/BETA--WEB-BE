package com.admin.portal.controller;

import com.admin.portal.entity.LikedJob;
import com.admin.portal.service.LikedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/liked-jobs")
public class LikedJobController {

    @Autowired
    private LikedJobService likedJobService;

    @GetMapping
    public ResponseEntity<?> getLikedJobs(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        List<LikedJob> list = likedJobService.getLikedJobs(email.trim());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> likeJob(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        Number jobIdNum = (Number) payload.get("jobId");
        if (email == null || email.trim().isEmpty() || jobIdNum == null) {
            return ResponseEntity.badRequest().body("Email and jobId are required.");
        }
        LikedJob likedJob = likedJobService.likeJob(email.trim(), jobIdNum.longValue());
        return ResponseEntity.ok(likedJob);
    }

    @DeleteMapping
    public ResponseEntity<?> unlikeJob(@RequestParam("email") String email, @RequestParam("jobId") Long jobId) {
        if (email == null || email.trim().isEmpty() || jobId == null) {
            return ResponseEntity.badRequest().body("Email and jobId are required.");
        }
        likedJobService.unlikeJob(email.trim(), jobId);
        return ResponseEntity.ok("Job unliked successfully.");
    }
}

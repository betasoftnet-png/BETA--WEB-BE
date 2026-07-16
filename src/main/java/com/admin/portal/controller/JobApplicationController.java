package com.admin.portal.controller;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/jobs")
public class JobApplicationController {

    @Autowired
    private JobApplicationService jobApplicationService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyJob(

            @RequestParam("jobId") Long jobId,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("coverLetter") String coverLetter,
            @RequestParam("interviewDate") String interviewDate,
            @RequestParam("interviewTime") String interviewTime,
            @RequestParam("resume") MultipartFile resume) throws IOException {

        if (jobApplicationService.hasAlreadyApplied(jobId, email)) {
            return ResponseEntity.badRequest()
                    .body("You have already applied for this job using this email address.");
        }

        // Create uploads folder
        String uploadDir = "uploads";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save resume
        String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();

        Files.copy(
                resume.getInputStream(),
                uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        // Applied date = today
        LocalDate appliedDate = LocalDate.now();

        LocalDate selectedInterviewDate = null;
        if (interviewDate != null && !interviewDate.trim().isEmpty() && !"null".equalsIgnoreCase(interviewDate) && !"undefined".equalsIgnoreCase(interviewDate)) {
            // Candidate selected interview date
            selectedInterviewDate = LocalDate.parse(interviewDate);

            // Validate within 3 days
            long days = ChronoUnit.DAYS.between(appliedDate, selectedInterviewDate);

            if (days < 0 || days > 3) {
                return ResponseEntity.badRequest()
                        .body("Interview date must be within 3 days of applying.");
            }
        }

        // Create application object
        JobApplication application = new JobApplication();

        application.setJobId(jobId);
        application.setFullName(fullName);
        application.setEmail(email);
        application.setPhone(phone);
        application.setCoverLetter(coverLetter);
        application.setResume(fileName);

        application.setAppliedDate(appliedDate);
        application.setAppliedTime(java.time.LocalDateTime.now());
        application.setInterviewDate(selectedInterviewDate);
        application.setInterviewTime(interviewTime);

        application.setStatus("PENDING");

        jobApplicationService.save(application);

        return ResponseEntity.ok("Application submitted successfully.");
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        return ResponseEntity.ok(jobApplicationService.getApplicationsByEmail(email.trim()));
    }

    @PutMapping("/applications/{id}/github")
    public ResponseEntity<?> submitGithubLink(
            @PathVariable Long id,
            @RequestParam("githubLink") String githubLink) {
        if (githubLink == null || githubLink.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("GitHub link is required.");
        }
        try {
            JobApplication app = jobApplicationService.submitGithubLink(id, githubLink.trim());
            return ResponseEntity.ok(app);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
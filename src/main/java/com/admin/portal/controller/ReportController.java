package com.admin.portal.controller;

import com.admin.portal.entity.Report;
import com.admin.portal.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Submit a new report
    @PostMapping
    public ResponseEntity<Report> submitReport(@RequestBody Report report) {
        Report savedReport = reportService.saveReport(report);
        return ResponseEntity.ok(savedReport);
    }

    // Get all reports (Admin)
    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    // Get report by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        Optional<Report> report = reportService.getReportById(id);

        if (report.isPresent()) {
            return ResponseEntity.ok(report.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Get reports by candidate ID
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Report>> getReportsByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(reportService.getReportsByCandidate(candidateId));
    }

    // Get reports by job ID
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Report>> getReportsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(reportService.getReportsByJob(jobId));
    }

    // Get reports by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Report>> getReportsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status.toUpperCase()));
    }

    // Update report status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Report updatedReport = reportService.updateStatus(id, status.toUpperCase());

        if (updatedReport != null) {
            return ResponseEntity.ok(updatedReport);
        }

        return ResponseEntity.notFound().build();
    }

    // Delete report
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok("Report deleted successfully.");
    }
}
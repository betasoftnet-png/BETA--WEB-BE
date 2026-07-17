package com.admin.portal.service;

import com.admin.portal.entity.Report;
import com.admin.portal.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    // Save a new report
    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    // Get all reports
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // Get report by ID
    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    // Get reports by status
    public List<Report> getReportsByStatus(String status) {
        return reportRepository.findByStatus(status);
    }

    // Get reports by candidate
    public List<Report> getReportsByCandidate(Long candidateId) {
        return reportRepository.findByCandidateId(candidateId);
    }

    // Get reports by job
    public List<Report> getReportsByJob(Long jobId) {
        return reportRepository.findByJobId(jobId);
    }

    // Update report status
    public Report updateStatus(Long id, String status) {
        Optional<Report> optionalReport = reportRepository.findById(id);

        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            report.setStatus(status);
            return reportRepository.save(report);
        }

        return null;
    }

    // Delete report
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}
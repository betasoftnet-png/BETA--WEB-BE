package com.admin.portal.service;

import com.admin.portal.entity.Job;
import com.admin.portal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    // Create Job
    public Job createJob(Job job) {
        if (job.getStatus() == null) {
            job.setStatus("ACTIVE");
        }
        if (job.getPostedDate() == null) {
            job.setPostedDate(LocalDate.now());
        }
        return jobRepository.save(job);
    }

    // Get All Jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Get Active Jobs (status != DELETED)
    public List<Job> getActiveJobs() {
        return jobRepository.findAll().stream()
                .filter(j -> j.getStatus() == null || !"DELETED".equalsIgnoreCase(j.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Get Jobs by Status
    public List<Job> getJobsByStatus(String status) {
        return jobRepository.findAll().stream()
                .filter(j -> status.equalsIgnoreCase(j.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Get Job By ID
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    // Update Job
    public Job updateJob(Job job) {
        if (job.getStatus() == null) {
            job.setStatus("ACTIVE");
        }
        return jobRepository.save(job);
    }

    // Delete Job (Soft Delete)
    public void deleteJob(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("DELETED");
            jobRepository.save(job);
        });
    }
}
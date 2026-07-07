package com.admin.portal.service;

import com.admin.portal.entity.Job;
import com.admin.portal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    // Create Job
    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    // Get All Jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Get Job By ID
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    // Update Job
    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }

    // Delete Job
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}
package com.admin.portal.service;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.repository.AdminJobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminJobApplicationService {

    @Autowired
    private AdminJobApplicationRepository repository;

    public List<JobApplication> getAllApplications() {
        return repository.findAll();
    }

    public JobApplication updateStatus(Long id, String status) {
        JobApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        return repository.save(app);
    }
}
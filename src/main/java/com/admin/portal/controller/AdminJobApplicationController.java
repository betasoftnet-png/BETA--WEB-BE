package com.admin.portal.controller;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.service.AdminJobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminJobApplicationController {

    @Autowired
    private AdminJobApplicationService service;

    @GetMapping("/admin/applications")
    public List<JobApplication> getAllApplications() {
        return service.getAllApplications();
    }

    @PutMapping("/applications/{id}/status")
    public JobApplication updateApplicationStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String status = payload.get("status");
        return service.updateStatus(id, status);
    }
}
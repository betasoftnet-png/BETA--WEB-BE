package com.admin.portal.controller;

import com.admin.portal.entity.JobApplication;
import com.admin.portal.service.AdminJobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdminJobApplicationController {

    @Autowired
    private AdminJobApplicationService service;

    @GetMapping({"/admin/applications", "/applications"})
    public List<JobApplication> getAllApplications() {
        return service.getAllApplications();
    }

    @GetMapping({"/admin/applications/{id}", "/applications/{id}"})
    public JobApplication getApplicationById(@PathVariable Long id) {
        return service.getApplicationById(id);
    }

    @PutMapping({"/admin/applications/{id}/status", "/applications/{id}/status"})
    public JobApplication updateApplicationStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String status = payload.get("status");
        return service.updateStatus(id, status);
    }

    @PutMapping({"/admin/applications/{id}/schedule", "/applications/{id}/schedule"})
    public JobApplication scheduleInterview(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String date = payload.get("date");
        String time = payload.get("time");
        String link = payload.get("link");
        return service.scheduleInterview(id, date, time, link);
    }

    @PutMapping({"/admin/applications/{id}/hr-interview", "/applications/{id}/hr-interview"})
    public JobApplication saveHrInterview(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String date = payload.get("date");
        String time = payload.get("time");
        String location = payload.get("location");
        return service.saveHrInterview(id, date, time, location);
    }

    /**
     * Allows admin to manually set the job title for an application whose
     * referenced job was permanently deleted from the database.
     */
    @PatchMapping({"/admin/applications/{id}/job-title", "/applications/{id}/job-title"})
    public JobApplication updateJobTitle(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String jobTitle = payload.get("jobTitle");
        return service.updateJobTitle(id, jobTitle);
    }

    @PutMapping({"/admin/applications/{id}/experience", "/applications/{id}/experience"})
    public JobApplication updateExperience(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String experience = payload.get("experience");
        return service.updateExperience(id, experience);
    }
}
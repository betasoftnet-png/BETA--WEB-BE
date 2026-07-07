package com.admin.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String department;
    private String location;
    private String employmentType;
    private String salary;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    private List<String> requiredSkills;

    private LocalDate postedDate;

    private String status;

    // getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public String getLocation() {
        return location;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public String getSalary() {
        return salary;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public String getStatus() {
        return status;
    }
}
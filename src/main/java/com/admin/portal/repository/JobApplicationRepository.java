package com.admin.portal.repository;

import com.admin.portal.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByEmailIgnoreCase(String email);
    boolean existsByJobIdAndEmailIgnoreCase(Long jobId, String email);
}
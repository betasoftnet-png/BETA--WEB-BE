package com.admin.portal.repository;

import com.admin.portal.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByEmailIgnoreCase(String email);
    boolean existsByJobIdAndEmailIgnoreCase(Long jobId, String email);
    Optional<JobApplication> findByAssessmentToken(String assessmentToken);
}
package com.admin.portal.repository;

import com.admin.portal.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatus(String status);

    List<Report> findByCandidateId(Long candidateId);

    List<Report> findByJobId(Long jobId);
}
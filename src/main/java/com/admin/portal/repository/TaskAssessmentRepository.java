package com.admin.portal.repository;

import com.admin.portal.entity.TaskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskAssessmentRepository extends JpaRepository<TaskAssessment, Long> {

    Optional<TaskAssessment> findByCandidate_Id(Long candidateId);

}
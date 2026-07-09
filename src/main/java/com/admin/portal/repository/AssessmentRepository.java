package com.admin.portal.repository;

import com.admin.portal.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByCandidateId(Long candidateId);

    @Modifying
    @Transactional
    void deleteByCandidateId(Long candidateId);

}
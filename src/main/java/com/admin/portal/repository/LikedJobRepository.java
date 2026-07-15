package com.admin.portal.repository;

import com.admin.portal.entity.LikedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikedJobRepository extends JpaRepository<LikedJob, Long> {
    List<LikedJob> findByEmail(String email);
    Optional<LikedJob> findByEmailAndJobId(String email, Long jobId);
    boolean existsByEmailAndJobId(String email, Long jobId);
    void deleteByEmailAndJobId(String email, Long jobId);
}

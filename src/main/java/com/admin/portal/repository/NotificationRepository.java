package com.admin.portal.repository;

import com.admin.portal.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

    long countByCandidateIdAndIsReadFalse(Long candidateId);
}
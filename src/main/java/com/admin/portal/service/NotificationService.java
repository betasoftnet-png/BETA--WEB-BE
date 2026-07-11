package com.admin.portal.service;

import com.admin.portal.entity.Notification;
import com.admin.portal.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public Notification createNotification(Long candidateId,
            String title,
            String message) {

        Notification notification = new Notification(candidateId, title, message);

        return repository.save(notification);
    }

    public List<Notification> getNotifications(Long candidateId) {
        return repository.findByCandidateIdOrderByCreatedAtDesc(candidateId);
    }

    public long getUnreadCount(Long candidateId) {
        return repository.countByCandidateIdAndIsReadFalse(candidateId);
    }

    public void markAsRead(Long notificationId) {

        Notification notification = repository.findById(notificationId).orElseThrow();

        notification.setRead(true);

        repository.save(notification);
    }
}
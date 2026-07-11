package com.admin.portal.controller;

import com.admin.portal.entity.Notification;
import com.admin.portal.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{candidateId}")
    public List<Notification> getNotifications(
            @PathVariable Long candidateId) {

        return notificationService.getNotifications(candidateId);
    }

    @GetMapping("/{candidateId}/unread-count")
    public long getUnreadCount(
            @PathVariable Long candidateId) {

        return notificationService.getUnreadCount(candidateId);
    }

    @PutMapping("/{notificationId}/read")
    public String markAsRead(
            @PathVariable Long notificationId) {

        notificationService.markAsRead(notificationId);

        return "Notification marked as read";
    }
}
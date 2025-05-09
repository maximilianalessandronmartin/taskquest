package org.novize.api.controller;

import org.novize.api.dtos.NotificationDto;
import org.novize.api.model.Notification;
import org.novize.api.model.User;
import org.novize.api.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> getNotifications(@AuthenticationPrincipal User user) {
        return notificationService.getUnreadNotifications(user);
    }

    @PostMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user);
    }
}
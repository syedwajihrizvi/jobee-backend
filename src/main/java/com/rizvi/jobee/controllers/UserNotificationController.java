package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizvi.jobee.services.UserNotificationService;
import com.rizvi.jobee.dtos.notification.NotificationDto;
import com.rizvi.jobee.mappers.NotificationMapper;
import com.rizvi.jobee.principals.CustomPrincipal;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/user-notifications")
public class UserNotificationController {
    private final UserNotificationService userNotificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping()
    public ResponseEntity<List<NotificationDto>> getNotificationsForAuthenticatedUser(
            @AuthenticationPrincipal CustomPrincipal userPrincipal) {
        var userId = userPrincipal.getId();
        var userType = userPrincipal.getRole();
        var notifications = userNotificationService.getNotificationsForUser(userId, userType);
        var notificationDtos = notifications.stream().map(notificationMapper::toNotificationDto).toList();
        return ResponseEntity.ok(notificationDtos);
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<List<NotificationDto>> markAllNotificationsAsReadForAuthenticatedUser(
            @AuthenticationPrincipal CustomPrincipal userPrincipal) {
        var userId = userPrincipal.getId();
        var userType = userPrincipal.getRole();
        var updatedNotifications = userNotificationService.markAllUserNotificationsAsRead(userId, userType);
        var notificationDtos = updatedNotifications.stream().map(notificationMapper::toNotificationDto).toList();
        return ResponseEntity.ok(notificationDtos);
    }

    @PatchMapping("/{id}/mark-read")
    public ResponseEntity<NotificationDto> markNotificationAsRead(
            @AuthenticationPrincipal CustomPrincipal userPrincipal,
            @PathVariable Long id) {
        var notification = userNotificationService.markNotificationAsRead(id);
        return ResponseEntity.ok(notificationMapper.toNotificationDto(notification));
    }

    @DeleteMapping("/delete-read")
    public ResponseEntity<Void> deleteReadNotification(
            @AuthenticationPrincipal CustomPrincipal userPrincipal) {
        var userId = userPrincipal.getId();
        var userType = userPrincipal.getRole();
        userNotificationService.deleteReadNotificationsForUser(userId, userType);
        return ResponseEntity.noContent().build();
    }
}

package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizvi.jobee.services.UserNotificationService;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.principals.CustomPrincipal;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/user-notifications")
public class UserNotificationController {
    private final UserNotificationService userNotificationService;

    @GetMapping()
    public ResponseEntity<List<Notification>> getNotificationsForAuthenticatedUser(
            @AuthenticationPrincipal CustomPrincipal userPrincipal) {
        var userId = userPrincipal.getId();
        var userType = userPrincipal.getRole();
        System.out.println("Fetching notifications for userId: " + userId + ", userType: " + userType);

        var notifications = userNotificationService.getNotificationsForUser(userId, userType);
        return ResponseEntity.ok(notifications);
    }
}

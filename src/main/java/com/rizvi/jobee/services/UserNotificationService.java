package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.repositories.NotificationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserNotificationService {
    private final NotificationRepository notificationRepository;

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId, String userType) {
        MessagerUserType type = userType.equals(Role.BUSINESS.name()) || userType.equals(Role.ADMIN.name())
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        return notificationRepository.findByRecipientIdAndRecipientType(userId, type);
    }
}

package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.notification.CreateNotificationDto;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.exceptions.UserNotificationNotFoundException;
import com.rizvi.jobee.repositories.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserNotificationService {
    private final NotificationRepository notificationRepository;
    private final CompanyService companyService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    public Notification saveNotification(CreateNotificationDto notificationDto) {
        // Depending on notification type, set the foeign key association
        Notification notification = Notification.builder()
                .recipientId(notificationDto.getRecipientId())
                .notificationType(notificationDto.getNotificationType())
                .recipientType(notificationDto.getRecipientType())
                .message(notificationDto.getMessage())
                .read(false)
                .build();
        if (notificationDto.getCompanyId() != null) {
            var company = companyService.findCompanyById(notificationDto.getCompanyId());
            notification.setCompany(company);
        }
        if (notificationDto.getJobId() != null) {
            var job = jobService.getJobById(notificationDto.getJobId());
            notification.setJob(job);
        }
        if (notificationDto.getApplicationId() != null) {
            var application = applicationService.findById(notificationDto.getApplicationId());
            notification.setApplication(application);
        }
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId, String userType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        MessagerUserType type = userType.equals(Role.BUSINESS.name()) || userType.equals(Role.ADMIN.name())
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        return notificationRepository.findByRecipientIdAndRecipientType(userId, type, sort);
    }

    public List<Notification> markAllUserNotificationsAsRead(Long userId, String userType) {
        MessagerUserType type = userType.equals(Role.BUSINESS.name()) || userType.equals(Role.ADMIN.name())
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        var notifications = notificationRepository.findByRecipientIdAndRecipientType(userId, type, sort);
        for (var notification : notifications) {
            notification.setRead(true);
        }
        return notificationRepository.saveAll(notifications);
    }

    public Notification markNotificationAsRead(Long notificationId) {
        var notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            throw new UserNotificationNotFoundException("Notification not found with id: " + notificationId);
        }
        notification.setRead(true);
        return notificationRepository.save(notification);

    }

    @Transactional
    public void deleteReadNotificationsForUser(Long userId, String userType) {
        MessagerUserType type = userType.equals(Role.BUSINESS.name()) || userType.equals(Role.ADMIN.name())
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        notificationRepository.deleteByRecipientIdAndRecipientTypeAndReadIsTrue(userId, type);
    }
}

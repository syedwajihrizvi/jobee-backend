package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.notification.CreateNotificationDto;
import com.rizvi.jobee.dtos.notification.NotificationContext;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.NotificationType;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.exceptions.UserNotificationNotFoundException;
import com.rizvi.jobee.mappers.NotificationMapper;
import com.rizvi.jobee.repositories.InterviewRepository;
import com.rizvi.jobee.repositories.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserNotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final InterviewRepository interviewRepository;
    private final CompanyService companyService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final SimpMessagingTemplate messagingTemplate;

    public static String createNotificationDestination(MessagerUserType recipientType, Long recipientId) {
        return "/topic/notifications/" + recipientType.toString().toLowerCase() + "/" + recipientId;
    }

    public Notification saveNotification(CreateNotificationDto notificationDto) {
        // Depending on notification type, set the foeign key association
        Notification notification = Notification.builder()
                .recipientId(notificationDto.getRecipientId())
                .notificationType(notificationDto.getNotificationType())
                .recipientType(notificationDto.getRecipientType())
                .message(notificationDto.getMessage())
                .read(false)
                .build();
        NotificationContext context = new NotificationContext();
        if (notificationDto.getCompanyId() != null) {
            var company = companyService.findCompanyById(notificationDto.getCompanyId());
            context.setCompanyId(company.getId());
            context.setCompanyName(company.getName());
            context.setCompanyLogoUrl(company.getLogo());
        }
        if (notificationDto.getJobId() != null) {
            var job = jobService.getJobById(notificationDto.getJobId());
            context.setJobId(job.getId());
            context.setJobTitle(job.getTitle());
        }
        if (notificationDto.getApplicationId() != null) {
            var application = applicationService.findById(notificationDto.getApplicationId());
            context.setApplicationId(application.getId());
        }
        if (notificationDto.getInterviewId() != null) {
            var interview = interviewRepository.findById(notificationDto.getInterviewId()).orElse(null);
            if (interview != null) {
                context.setInterviewId(interview.getId());
            }
        }
        notification.setContext(context);
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

    public Notification createInterviewPrepNotificationAndSend(InterviewPreparation interviewPrep) {
        var message = "Your preparation materials your interview for the position of "
                + interviewPrep.getInterview().getJob().getTitle() + " are ready.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(interviewPrep.getInterview().getCandidate().getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_PREP_READY);
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
        return savedNotification;
    }

    public Notification createInterviewScheduledNotificationAndSend(Interview interview) {
        var message = "You have been scheduled for an interview for the position of "
                + interview.getJob().getTitle() + " at " + interview.getCreatedBy().getCompany().getName() + ".";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(interview.getCandidate().getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_SCHEDULED);
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
        return savedNotification;
    }

    public Notification createInterviewRejectionNotificationAndSend(Interview interview) {
        var message = "We regret to inform you that you have not been selected for the position of "
                + interview.getJob().getTitle() + " at " + interview.getCreatedBy().getCompany().getName()
                + ". Please check your interview feedback for more details.";

        CreateNotificationDto notificationDto = notificationMapper.toCreateNotificationDtoFromInterview(interview,
                MessagerUserType.USER, NotificationType.REJECTION, message);
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
        return savedNotification;
    }

    public Notification sendInAppNotificationForInterviewCompletion(Interview interview) {
        var message = "Congratulations! You have successfully completed your interview for the position of "
                + interview.getJob().getTitle() + " at " + interview.getCreatedBy().getCompany().getName()
                + ". We will let you know about the next steps soon.";

        CreateNotificationDto notificationDto = notificationMapper.toCreateNotificationDtoFromInterview(interview,
                MessagerUserType.USER, NotificationType.INTERVIEW_COMPLETED, message);
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
        return savedNotification;
    }

    private void sendInAppNotification(Notification notification) {
        String destination = createNotificationDestination(
                notification.getRecipientType(),
                notification.getRecipientId());
        messagingTemplate.convertAndSend(destination, notification);
    }

    public void sendInAppNotificationForResumeParsingCompletion(Long userId) {
        var message = "Your resume has been successfully parsed and your profile has been updated.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(userId);
        notificationDto.setNotificationType(NotificationType.AI_RESUME_REVIEW_COMPLETE);
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
    }

}

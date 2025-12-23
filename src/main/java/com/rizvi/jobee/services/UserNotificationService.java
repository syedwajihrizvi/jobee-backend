package com.rizvi.jobee.services;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.notification.CreateNotificationDto;
import com.rizvi.jobee.dtos.notification.NotificationContext;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.NotificationType;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.exceptions.UserNotificationNotFoundException;
import com.rizvi.jobee.mappers.NotificationMapper;
import com.rizvi.jobee.repositories.ApplicationRepository;
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
    private final ApplicationRepository applicationRepository;
    private final CompanyService companyService;
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
        if (notificationDto.getJobId() != null && notificationDto.getJobTitle() != null) {
            context.setJobId(notificationDto.getJobId());
            context.setJobTitle(notificationDto.getJobTitle());
        }
        if (notificationDto.getApplicationId() != null) {
            var application = applicationRepository.findById(notificationDto.getApplicationId()).orElse(null);
            if (application != null) {
                context.setApplicationId(application.getId());
            }
        }
        if (notificationDto.getInterviewId() != null) {
            var interview = interviewRepository.findById(notificationDto.getInterviewId()).orElse(null);
            if (interview != null) {
                context.setInterviewId(interview.getId());
            }
        }
        if (notificationDto.getCandidateProfileImageUrl() != null) {
            context.setCandidateProfileImageUrl(notificationDto.getCandidateProfileImageUrl());
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

    public Notification createInterviewPrepNotificationAndSend(InterviewPreparation interviewPrep,
            Interview interview) {
        var message = "Your preparation materials for the position of "
                + interviewPrep.getInterview().getJob().getTitle() + " are ready.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setCompanyId(interview.getJob().getCompany().getId());
        notificationDto.setRecipientId(interviewPrep.getInterview().getCandidate().getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_PREP_READY);
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
        return savedNotification;
    }

    private CreateNotificationDto createDtoForCandidateInterviewSchedule(Interview interview) {
        var message = "You have been scheduled for an interview for the position of "
                + interview.getJob().getTitle() + " at " + interview.getCreatedBy().getCompany().getName() + ".";
        var job = interview.getJob();
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(interview.getCandidate().getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_SCHEDULED);
        notificationDto.setJobId(job.getId());
        notificationDto.setJobTitle(job.getTitle());
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setApplicationId(interview.getApplication().getId());
        notificationDto.setCompanyId(interview.getJob().getCompany().getId());
        return notificationDto;
    }

    private CreateNotificationDto createDtoForCandidateInterviewCancellation(Interview interview) {
        var message = "Your interview for the position of "
                + interview.getJob().getTitle() + " at " + interview.getCreatedBy().getCompany().getName()
                + " has been cancelled. Click to view more details.";
        var job = interview.getJob();
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(interview.getCandidate().getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_CANCELLED);
        notificationDto.setJobId(job.getId());
        notificationDto.setJobTitle(job.getTitle());
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setApplicationId(interview.getApplication().getId());
        notificationDto.setCompanyId(interview.getJob().getCompany().getId());
        return notificationDto;
    }

    private CreateNotificationDto createdDtoForConductorInterviewSchedule(Interview interview,
            BusinessAccount conductor) {
        BusinessAccount creator = interview.getCreatedBy();
        var isCreator = creator.getId().equals(conductor.getId());

        if (isCreator) {
            var message = "You have successfully scheduled an interview for the position of "
                    + interview.getJob().getTitle() + " at " + interview.getCreatedBy().getCompany().getName() + ".";
            CreateNotificationDto notificationDto = new CreateNotificationDto();
            notificationDto.setRecipientType(MessagerUserType.BUSINESS);
            notificationDto.setMessage(message);
            notificationDto.setRecipientId(conductor.getId());
            notificationDto.setJobId(interview.getJob().getId());
            notificationDto.setJobTitle(interview.getJob().getTitle());
            notificationDto.setInterviewId(interview.getId());
            notificationDto.setApplicationId(interview.getApplication().getId());
            notificationDto.setCompanyId(interview.getJob().getCompany().getId());
            notificationDto.setNotificationType(NotificationType.INTERVIEW_CREATED_SUCCESSFULLY);
            return notificationDto;
        }
        var message = "You have been scheduled to conduct an interview for the position of "
                + interview.getJob().getTitle() + ".";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.BUSINESS);
        notificationDto.setMessage(message);
        notificationDto.setJobId(interview.getJob().getId());
        notificationDto.setJobTitle(interview.getJob().getTitle());
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setApplicationId(interview.getApplication().getId());
        notificationDto.setCompanyId(interview.getJob().getCompany().getId());
        notificationDto.setRecipientId(conductor.getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_TO_CONDUCT_SCHEDULED);
        return notificationDto;
    }

    private CreateNotificationDto createDtoForConductorInterviewRemoval(Interview interview,
            BusinessAccount conductor) {
        var message = "You have been removed from conducting the interview for the position of "
                + interview.getJob().getTitle();
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.BUSINESS);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(conductor.getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_CONDUCTOR_REMOVED);
        notificationDto.setJobId(interview.getJob().getId());
        notificationDto.setJobTitle(interview.getJob().getTitle());
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setApplicationId(interview.getApplication().getId());
        notificationDto.setCompanyId(interview.getJob().getBusinessAccount().getCompany().getId());
        return notificationDto;
    }

    private CreateNotificationDto createDtoForConductorInterviewCancellation(Interview interview,
            BusinessAccount conductor) {
        var message = "The interview you were scheduled to conduct for the position of "
                + interview.getJob().getTitle()
                + " has been cancelled.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.BUSINESS);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(conductor.getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_CANCELLED);
        notificationDto.setJobId(interview.getJob().getId());
        notificationDto.setJobTitle(interview.getJob().getTitle());
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setApplicationId(interview.getApplication().getId());
        notificationDto.setCompanyId(interview.getJob().getBusinessAccount().getCompany().getId());
        return notificationDto;
    }

    private CreateNotificationDto createDtoForConductorInterviewUpdate(Interview interview,
            BusinessAccount conductor) {
        var message = "The interview you were scheduled to conduct for the position of "
                + interview.getJob().getTitle() + " has been updated.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.BUSINESS);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(conductor.getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_CONDUCTOR_UPDATED);
        notificationDto.setJobId(interview.getJob().getId());
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setApplicationId(interview.getApplication().getId());
        notificationDto.setCompanyId(interview.getJob().getBusinessAccount().getCompany().getId());
        return notificationDto;
    }

    private CreateNotificationDto createDtoForCandidateInterviewUpdate(Interview interview) {
        var message = "Your interview for the position of "
                + interview.getJob().getTitle() + " at " + interview.getCreatedBy().getCompany().getName()
                + " has been updated. Click to view more details.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(interview.getCandidate().getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_UPDATED);
        var job = interview.getJob();
        notificationDto.setJobId(job.getId());
        notificationDto.setJobTitle(job.getTitle());
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setApplicationId(interview.getApplication().getId());
        notificationDto.setCompanyId(job.getCompany().getId());
        return notificationDto;
    }

    public void createInterviewScheduledNotificationAndSend(Interview interview) {
        CreateNotificationDto candidateNotifyDto = createDtoForCandidateInterviewSchedule(interview);
        var savedCandidateNotification = saveNotification(candidateNotifyDto);
        sendInAppNotification(savedCandidateNotification);
        // Send notification to conductors
        Set<BusinessAccount> conductors = interview.getInterviewers();
        conductors.add(interview.getCreatedBy());
        for (BusinessAccount conductor : conductors) {
            CreateNotificationDto conductorNotifyDto = createdDtoForConductorInterviewSchedule(interview, conductor);
            var savedConductorNotification = saveNotification(conductorNotifyDto);
            sendInAppNotification(savedConductorNotification);
        }
    }

    public void createInterviewRescheduleRequestNotificationAndSend(Interview interview) {
        // Notify interviewers and creator about reschedule request
        BusinessAccount creator = interview.getCreatedBy();
        String candidateName = interview.getCandidate().getFullName();
        String candidateProfileImageUrl = interview.getCandidate().getProfileImageUrl();
        var message = candidateName + " has requested to reschedule the interview for the position of "
                + interview.getJob().getTitle()
                + ". Click to view more details.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.BUSINESS);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(creator.getId());
        notificationDto.setNotificationType(NotificationType.INTERVIEW_RESCHEDULE_REQUESTED);
        notificationDto.setInterviewId(interview.getId());
        notificationDto.setCompanyId(interview.getJob().getBusinessAccount().getCompany().getId());
        notificationDto.setCandidateProfileImageUrl(candidateProfileImageUrl);
        notificationDto.setFullName(candidateName);
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
    }

    public void createInterviewUpdatedNotificationAndSend(Interview interview, Set<BusinessAccount> newInterviewers,
            Set<BusinessAccount> removedInterviewers, Set<BusinessAccount> unchangedInterviewers) {
        CreateNotificationDto candidateNotifyDto = createDtoForCandidateInterviewUpdate(interview);
        var savedCandidateNotification = saveNotification(candidateNotifyDto);
        sendInAppNotification(savedCandidateNotification);
        // Send notification to conductors
        Set<BusinessAccount> conductors = interview.getInterviewers();
        conductors.add(interview.getCreatedBy());
        for (BusinessAccount conductor : newInterviewers) {
            System.out.println("Sending notification to new interviewer: " + conductor.getEmail());
            CreateNotificationDto conductorNotifyDto = createdDtoForConductorInterviewSchedule(interview, conductor);
            var savedConductorNotification = saveNotification(conductorNotifyDto);
            sendInAppNotification(savedConductorNotification);
        }
        for (BusinessAccount conductor : unchangedInterviewers) {
            System.out.println("Sending notification to unchanged interviewer: " + conductor.getEmail());
            CreateNotificationDto conductorNotifyDto = createDtoForConductorInterviewUpdate(interview, conductor);
            var savedConductorNotification = saveNotification(conductorNotifyDto);
            sendInAppNotification(savedConductorNotification);
        }
        for (BusinessAccount conductor : removedInterviewers) {
            System.out.println("Sending notification to removed interviewer: " + conductor.getEmail());
            CreateNotificationDto conductorNotifyDto = createDtoForConductorInterviewRemoval(interview,
                    conductor);
            var savedConductorNotification = saveNotification(conductorNotifyDto);
            sendInAppNotification(savedConductorNotification);
        }

    }

    public void createInterviewCancelledNotificationAndSend(Interview interview) {
        CreateNotificationDto candidateNotifyDto = createDtoForCandidateInterviewCancellation(interview);
        var savedCandidateNotification = saveNotification(candidateNotifyDto);
        sendInAppNotification(savedCandidateNotification);
        // Send notification to conductors
        Set<BusinessAccount> conductors = interview.getInterviewers();
        conductors.add(interview.getCreatedBy());
        for (BusinessAccount conductor : conductors) {
            CreateNotificationDto conductorNotifyDto = createDtoForConductorInterviewCancellation(interview, conductor);
            var savedConductorNotification = saveNotification(conductorNotifyDto);
            sendInAppNotification(savedConductorNotification);
        }
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

    public Notification createApplicationRejectionsNotificationAndSend(Application application) {
        var jobTitle = application.getJob().getTitle();
        var companyName = application.getJob().getCompany().getName();
        var companyId = application.getJob().getCompany().getId();
        var recepientId = application.getUserProfile().getId();
        var message = "We regret to inform you that your application for the position of "
                + jobTitle + " at " + companyName + " has been rejected.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.USER);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(recepientId);
        notificationDto.setNotificationType(NotificationType.REJECTION);
        notificationDto.setCompanyId(companyId);
        notificationDto.setJobId(application.getJob().getId());
        notificationDto.setJobTitle(jobTitle);
        notificationDto.setApplicationId(application.getId());
        var savedNotification = saveNotification(notificationDto);
        sendInAppNotification(savedNotification);
        return savedNotification;
    }

    public Notification createdAddedToHiringTeamNotificationAndSend(BusinessAccount recepient, Job job) {
        var jobTitle = job.getTitle();
        var jobId = job.getId();
        var recepientId = recepient.getId();
        var message = "You have been added to the hiring team for the job: "
                + jobTitle + ". Click to view more details.";
        CreateNotificationDto notificationDto = new CreateNotificationDto();
        notificationDto.setRecipientType(MessagerUserType.BUSINESS);
        notificationDto.setMessage(message);
        notificationDto.setRecipientId(recepientId);
        notificationDto.setNotificationType(NotificationType.ADDED_TO_HIRING_TEAM);
        notificationDto.setJobId(jobId);
        notificationDto.setJobTitle(jobTitle);
        notificationDto.setCompanyId(job.getCompany().getId());
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

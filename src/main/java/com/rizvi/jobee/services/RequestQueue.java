package com.rizvi.jobee.services;

import java.io.File;
import java.util.Set;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;
import com.rizvi.jobee.interfaces.NotificationService;
import com.rizvi.jobee.mappers.NotificationMapper;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RequestQueue {
    private final NotificationService notificationService;
    private final AIService aiService;
    private final S3Service s3Service;
    private final InterviewPreparationRepository interviewPreparationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserNotificationService userNotificationService;
    private final EducationService userEducationService;
    private final ProjectService userProjectService;
    private final UserSkillService userSkillService;
    private final SocialMediaService userSocialMediaService;
    private final ExperienceService userExperienceService;
    private final NotificationMapper notificationMapper;
    private final EmailSender emailSender;

    @Async
    public void processInterviewPrep(PrepareForInterviewRequest prepareForInterviewRequest,
            InterviewPreparation interviewPrep) {
        try {
            PrepareForInterviewResponse response = aiService.generateInterviewPrep(prepareForInterviewRequest);
            interviewPrep.updateViaAIResponse(response);
            var savedInterview = interviewPreparationRepository.save(interviewPrep);
            notificationService.sendNotification("user-device-token", "Interview Prep Ready",
                    "Your interview preparation materials are ready for interview");
            var interviewerId = savedInterview.getInterview().getCandidate().getId();
            String recepientDest = "/topic/notifications/user/" + interviewerId;
            Notification savedNotification = userNotificationService
                    .createInterviewPrepNotificationAndSend(interviewPrep);
            var notificationDto = notificationMapper.toNotificationDto(savedNotification);
            messagingTemplate.convertAndSend(recepientDest, notificationDto);
            emailSender.sendInterviewPrepEmail(interviewPrep);
        } catch (Exception e) {
            // TODO: Handle the exception properly
            System.out.println("Interview prep processing was interrupted");
            System.out.println(e.getMessage());
        }
    }

    @Async
    public void processResumeParsing(MultipartFile resume, UserProfile userProfile, Boolean updateProfessionalSummary) {
        try {
            var details = aiService.extractDetailsFromResume(resume, userProfile);
            var educations = details.getEducations();
            var experiences = details.getExperiences();
            var projects = details.getProjects();
            var socialMediaLinks = details.getSocialMediaLinks();
            var skills = details.getSkills();
            String currentCompany = details.getCurrentCompany();
            String currentPosition = details.getCurrentPosition();
            if (currentCompany != null && !currentCompany.isBlank()) {
                userProfile.setCompany(currentCompany);
            }
            if (currentPosition != null && !currentPosition.isBlank()) {
                userProfile.setTitle(currentPosition);
            }
            userEducationService.createEducationsForUserFromAISchemas(educations, userProfile);
            userSkillService.createUserSkills(skills, userProfile);
            userExperienceService.addExperiencesForUserFromAISchemas(experiences, userProfile);
            userProjectService.createProjectsForUserFromAISchemas(projects, userProfile);
            userSocialMediaService.createSocialMediaLinksForUserFromAISchemas(socialMediaLinks, userProfile);
            userNotificationService.sendInAppNotificationForResumeParsingCompletion(userProfile.getId());
        } catch (Exception e) {
            // Log the error but continue
            System.err.println("Failed to extract details from resume: " + e.getMessage());
        }
    }

    @Async
    public void sendInterviewScheduledEmailsAndNotifications(Interview interview) {
        userNotificationService.createInterviewScheduledNotificationAndSend(interview);
        emailSender.sendScheduledInterviewEmail(interview);
    }

    @Async
    public void sendRejectionAfterInterviewEmailsAndNotifications(Interview interview) {
        userNotificationService.createInterviewRejectionNotificationAndSend(interview);
        emailSender.sendRejectionEmail(interview);
    }

    @Async
    public void sendInterviewCancelledEmailsAndNotifications(Interview interview) {
        userNotificationService.createInterviewCancelledNotificationAndSend(interview);
        // emailSender.sendInterviewCancellationEmail(interview);
    }

    @Async
    public void sendInterviewUpdatedEmailsAndNotifications(
            Interview interview, Set<BusinessAccount> newInterviewers, Set<ConductorDto> newOtherInterviewers,
            Set<BusinessAccount> removedInterviewers, Set<ConductorDto> removedOtherInterviewers) {
        userNotificationService.createInterviewUpdatedNotificationAndSend(interview, newInterviewers,
                removedInterviewers);
        emailSender.sendUpdatedInterviewEmail(interview, newInterviewers,
                newOtherInterviewers,
                removedInterviewers, removedOtherInterviewers);
    }

    @Async
    public void sendInterviewRescheduleRequestEmailsAndNotifications(Interview interview) {
        userNotificationService.createInterviewRescheduleRequestNotificationAndSend(interview);
        // TODO: Implement email sending
        // emailSender.sendInterviewRescheduleRequestEmail(interview);
    }

    @Async
    public void sendDocumentViaEmail(String fullName, String email, String fileUrl, String otherPartyName,
            boolean isDocument) {
        emailSender.sendDocumentViaEmail(fullName, email, fileUrl, otherPartyName, isDocument);
    }
}
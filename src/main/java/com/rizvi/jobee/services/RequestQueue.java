package com.rizvi.jobee.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
            System.out.println("Added educations for user ID: " + userProfile.getId());
            userSkillService.createUserSkills(skills, userProfile);
            System.out.println("Added skills for user ID: " + userProfile.getId());
            userExperienceService.addExperiencesForUserFromAISchemas(experiences, userProfile);
            System.out.println("Added experiences for user ID: " + userProfile.getId());
            userProjectService.createProjectsForUserFromAISchemas(projects, userProfile);
            System.out.println("Added projects for user ID: " + userProfile.getId());
            userSocialMediaService.createSocialMediaLinksForUserFromAISchemas(socialMediaLinks, userProfile);
            System.out.println("Added social media links for user ID: " + userProfile.getId());
            userNotificationService.sendInAppNotificationForResumeParsingCompletion(userProfile.getId());
            System.out.println("Completed resume parsing for user ID: " + userProfile.getId());
        } catch (Exception e) {
            // Log the error but continue
            System.err.println("Failed to extract details from resume: " + e.getMessage());
        }
    }
}

package com.rizvi.jobee.services;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.InterviewPreparationResource;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.PreparationStatus;
import com.rizvi.jobee.helpers.AISchemas.AICandidate;
import com.rizvi.jobee.helpers.AISchemas.AICompany;
import com.rizvi.jobee.helpers.AISchemas.AIInterview;
import com.rizvi.jobee.helpers.AISchemas.AIJob;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RequestQueue {
    private final AIService aiService;
    private final InterviewPreparationRepository interviewPreparationRepository;
    private final UserNotificationService userNotificationService;
    private final EducationService userEducationService;
    private final ProjectService userProjectService;
    private final UserSkillService userSkillService;
    private final SocialMediaService userSocialMediaService;
    private final ExperienceService userExperienceService;
    private final EmailSender emailSender;

    @Async
    public void processInterviewPrep(
            InterviewPreparation interviewPrep, Interview interview) {
        try {
            AIJob aiJob = new AIJob(interview.getJob());
            AICompany aiCompany = new AICompany(interview.getJob().getCompany());
            AICandidate aiCandidate = new AICandidate(interview.getCandidate());
            AIInterview aiInterview = new AIInterview(interview);
            PrepareForInterviewRequest prepareForInterviewRequest = new PrepareForInterviewRequest(aiJob, aiCompany,
                    aiCandidate, aiInterview);
            PrepareForInterviewResponse response = aiService.generateInterviewPrep(prepareForInterviewRequest);
            interviewPrep.updateViaAIResponse(response);
            interviewPrep.setStatus(PreparationStatus.IN_PROGRESS);
            var savedInterviewPrep = interviewPreparationRepository.save(interviewPrep);
            userNotificationService.createInterviewPrepNotificationAndSend(savedInterviewPrep, interview);
            emailSender.sendInterviewPrepEmail(interviewPrep);
        } catch (Exception e) {
            // Then we sent out a failure notification to the user and they can retry
            interviewPrep.setStatus(PreparationStatus.NOT_STARTED);
            interviewPreparationRepository.save(interviewPrep);
        }
    }

    @Async
    public void sendInterviewPrepResourcesViaEmail(
            Set<InterviewPreparationResource> resources, String companyName, String jobTitle, String candidateName,
            String candidateEmail) {
        emailSender.sendInterviewPrepResourcesEmail(
                resources, companyName, jobTitle, candidateName, candidateEmail);
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
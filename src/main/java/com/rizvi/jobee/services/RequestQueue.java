package com.rizvi.jobee.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.entities.HiringTeam;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.InterviewPreparationResource;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.Tag;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.PreparationStatus;
import com.rizvi.jobee.helpers.AISchemas.AICandidate;
import com.rizvi.jobee.helpers.AISchemas.AICompany;
import com.rizvi.jobee.helpers.AISchemas.AIInterview;
import com.rizvi.jobee.helpers.AISchemas.AIJob;
import com.rizvi.jobee.helpers.AISchemas.PostedJobInformationRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.TagRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RequestQueue {
    private final AIService aiService;
    private final TagRepository tagRepository;
    private final JobRepository jobRepository;
    private final InterviewPreparationRepository interviewPreparationRepository;
    private final UserNotificationService userNotificationService;
    private final EducationService userEducationService;
    private final ProjectService userProjectService;
    private final UserSkillService userSkillService;
    private final SocialMediaService userSocialMediaService;
    private final ExperienceService userExperienceService;
    private final EmailSender emailSender;
    private final InvitationService invitationService;

    @Async
    public void processInterviewPrep(
            InterviewPreparation interviewPrep, Interview interview) {
        Boolean successfullyGeneratedPrep = false;
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
            successfullyGeneratedPrep = true;
            userNotificationService.createInterviewPrepNotificationAndSend(savedInterviewPrep, interview);
            System.out.println("SYED-DEBUG: Sending interview prep email");
            emailSender.sendInterviewPrepEmail(interview);
        } catch (Exception e) {
            // Then we sent out a failure notification to the user and they can retry
            System.out.println("SYED-DEBUG: Failed to generate interview prep: " + e.getMessage());
            if (!successfullyGeneratedPrep) {
                interviewPrep.setStatus(PreparationStatus.NOT_STARTED);
                var savedInterviewPrep = interviewPreparationRepository.save(interviewPrep);
                userNotificationService.createInterviewPrepNotificationAndSend(savedInterviewPrep, interview);
            } else {
                userNotificationService.createInterviewPrepNotificationAndSend(interviewPrep, interview);
            }

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
            Set<BusinessAccount> removedInterviewers, Set<ConductorDto> removedOtherInterviewers,
            Set<BusinessAccount> unchangedInterviewers, Set<ConductorDto> unchangedOtherInterviewers) {
        userNotificationService.createInterviewUpdatedNotificationAndSend(interview, newInterviewers,
                removedInterviewers, unchangedInterviewers);
        emailSender.sendUpdatedInterviewEmail(interview, newInterviewers,
                newOtherInterviewers,
                removedInterviewers, removedOtherInterviewers, unchangedInterviewers,
                unchangedOtherInterviewers);
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

    @Async
    public void sendHiringTeamInvitationsForJob(Job job, Set<HiringTeam> jobeeTeamMembers,
            Set<HiringTeam> nonJobeeTeamMembers) {
        for (HiringTeam member : jobeeTeamMembers) {
            invitationService.sendHiringTeamInvitationEmail(member.getBusinessAccount(),
                    member.getBusinessAccount(),
                    job);
            userNotificationService.createdAddedToHiringTeamNotificationAndSend(member.getBusinessAccount(), job);
        }
        for (HiringTeam member : nonJobeeTeamMembers) {
            invitationService.sendHiringTeamInvitationAndJoinJobeeEmail(member.getEmail(),
                    job.getBusinessAccount(),
                    job);
        }
    }

    @Async
    public void addMoreTagsToJob(Job job, Company company, List<String> existingTags) {
        var request = PostedJobInformationRequest.builder()
                .job(new AIJob(job))
                .company(new AICompany(company))
                .existingTags(existingTags)
                .build();
        try {
            var response = aiService.generateMoreTagsForJob(request);
            var newTags = response.getTags();
            var tagEntities = new ArrayList<Tag>();
            for (String tagName : newTags) {
                var slugName = tagName.trim().replaceAll("[^a-zA-Z0-9 ]", "");
                var tag = tagRepository.findBySlug(slugName);
                if (tag == null) {
                    tag = Tag.builder().name(tagName).slug(slugName).build();
                    tag = tagRepository.save(tag);
                }
                tagEntities.add(tag);
            }
            job.addTags(tagEntities);
            var savedJob = jobRepository.save(job);
            userNotificationService.createJobUpdatedViaAINotificationAndSend(savedJob.getId(),
                    job.getBusinessAccount().getId());
        } catch (Exception e) {
            System.out.println("SYED-DEBUG: Failed to generate more tags for job: " + e.getMessage());
        }
    }

    @Async
    public void sendBusinessAccountVerificationEmail(String email, String verificationCode, String fullName) {
        emailSender.sendBusinessAccountVerificationEmail(email, verificationCode, fullName);
    }

    @Async
    public void sendUnofficialJobOfferEmailAndNotification(String candidateName, String candidateEmail,
            String companyName, String jobTitle, Long candidateId, String offerDetails, Long jobId,
            Long applicationId, Long companyId) {
        userNotificationService.createUnofficialJobOfferNotificationAndSend(
                candidateId, companyName, jobTitle, jobId, applicationId, companyId);
        emailSender.sendUnofficialJobOfferEmail(companyName, jobTitle, candidateName, candidateEmail, offerDetails);
    }
}
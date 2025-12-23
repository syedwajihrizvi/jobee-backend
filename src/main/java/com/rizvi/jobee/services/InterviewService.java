package com.rizvi.jobee.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewRescheduleDto;
import com.rizvi.jobee.dtos.interview.InterviewDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.InterviewPreparationFeedback;
import com.rizvi.jobee.entities.InterviewPreparationQuestion;
import com.rizvi.jobee.entities.InterviewRejection;
import com.rizvi.jobee.entities.InterviewRescheduleRequest;
import com.rizvi.jobee.entities.InterviewTip;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.InterviewDecisionResult;
import com.rizvi.jobee.enums.InterviewStatus;
import com.rizvi.jobee.enums.InterviewType;
import com.rizvi.jobee.enums.PreparationStatus;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.InterviewNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AICandidate;
import com.rizvi.jobee.helpers.AISchemas.AICompany;
import com.rizvi.jobee.helpers.AISchemas.AIJob;
import com.rizvi.jobee.helpers.AISchemas.AnswerInterviewQuestionRequest;
import com.rizvi.jobee.helpers.AISchemas.AnswerInterviewQuestionResponse;
import com.rizvi.jobee.helpers.AISchemas.InterviewPrepQuestion;
import com.rizvi.jobee.helpers.AISchemas.ReferenceToPreviousAnswer;
import com.rizvi.jobee.queries.InterviewQuery;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.InterviewPreparationFeedbackRepository;
import com.rizvi.jobee.repositories.InterviewPreparationQuestionRepository;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;
import com.rizvi.jobee.repositories.InterviewRepository;
import com.rizvi.jobee.specifications.InterviewSpecifications;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class InterviewService {
    private final BusinessAccountRepository businessAccountRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewPreparationRepository interviewPreparationRepository;
    private final InterviewPreparationQuestionRepository interviewPreparationQuestionRepository;
    private final InterviewPreparationFeedbackRepository interviewPreparationFeedbackRepository;
    private final ApplicationRepository applicationRepository;
    private final AIService aiService;
    private final S3Service s3Service;
    private final RequestQueue requestQueue;
    private final UserNotificationService userNotificationService;
    private final ObjectMapper objectMapper;

    public PaginatedResponse<Interview> getAllInterviews(InterviewQuery query, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber, pageSize,
                Sort.by("interviewDate").descending().and(Sort.by("startTime").ascending())
                        .and(Sort.by("id").ascending()));
        Specification<Interview> specification = InterviewSpecifications.withFilters(query);
        Page<Interview> page = interviewRepository.findAll(specification, pageRequest);
        var interviews = page.getContent();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        var totalInterviews = page.getTotalElements();
        return new PaginatedResponse<Interview>(hasMore, interviews, totalInterviews);
    }

    public Interview getInterviewById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + id));
    }

    public Interview getInterviewByJobIdAndCandidateId(Long jobId, Long candidateId) {
        return interviewRepository.findByJobIdAndCandidateId(jobId, candidateId);
    }

    public List<Interview> getInterviewsByJobId(Long jobId, Number limit) {
        var sort = Sort.by(
                Sort.Order.asc("interviewDate"),
                Sort.Order.desc("createdAt"));
        if (limit != null) {
            return interviewRepository.findByJobIdWithLimit(jobId, limit, sort);
        }
        return interviewRepository.findByJobId(jobId, sort);
    }

    public List<Interview> getInterviewsByCandidate(Long candidateId) {
        var sort = Sort.by(
                Sort.Order.asc("interviewDate"),
                Sort.Order.desc("createdAt"));
        return interviewRepository.findByCandidateId(candidateId, sort);
    }

    public List<Interview> getInterviewsForRecruiter(Long businessAccountId) {
        var sort = Sort.by(
                Sort.Order.asc("interviewDate"),
                Sort.Order.desc("createdAt"));
        return interviewRepository.findByCreatedAccountId(businessAccountId, sort);
    }

    public List<Interview> getInterviewsForEmployee(Long businessAccountId) {
        var sort = Sort.by(
                Sort.Order.asc("interviewDate"),
                Sort.Order.desc("createdAt"));
        return interviewRepository.findByInterviewersId(businessAccountId, sort);
    }

    public List<Interview> getInterviewsForBusinessUser(Long businessAccountId) {
        Set<Interview> interviews = new HashSet<>();
        var recruiterInterviews = getInterviewsForRecruiter(businessAccountId);
        var employeeInterviews = getInterviewsForEmployee(businessAccountId);
        interviews.addAll(recruiterInterviews);
        interviews.addAll(employeeInterviews);
        return interviews.stream().toList();
    }

    public List<Interview> getInterviewsByCompanyId(Long businessAccountId) {
        var businessAccount = businessAccountRepository.findById(businessAccountId).orElseThrow(
                () -> new AccountNotFoundException("Business account not found with id: " + businessAccountId));
        var sort = Sort.by(
                Sort.Order.asc("interviewDate"),
                Sort.Order.desc("createdAt"));
        var companyId = businessAccount.getCompany().getId();
        return interviewRepository.findByCompanyId(companyId, sort);

    }

    public InterviewPreparationQuestion getInterviewPreparationQuestion(
            Long interviewQuestionId) {
        return interviewPreparationQuestionRepository.findById(interviewQuestionId)
                .orElseThrow(() -> new InterviewNotFoundException(
                        "Interview question not found with id: " + interviewQuestionId));
    }

    @Transactional
    public Interview createInterview(
            CreateInterviewDto request, BusinessAccount businessAccount,
            UserProfile candidate, Job job, Application application) {
        var interview = Interview.builder()
                .job(job)
                .candidate(candidate)
                .title(request.getTitle())
                .description(request.getDescription())
                .interviewDate(request.getInterviewDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .interviewType(request.getInterviewType())
                .timezone(request.getTimezone())
                .streetAddress(request.getStreetAddress())
                .buildingName(request.getBuildingName())
                .parkingInfo(request.getParkingInfo())
                .contactInstructionsOnArrival(request.getContactInstructionsOnArrival())
                .phoneNumber(request.getPhoneNumber())
                .status(InterviewStatus.SCHEDULED)
                .createdBy(businessAccount)
                .build();
        String meetingPlatformStr = request.getMeetingPlatform();
        if (meetingPlatformStr != null && !meetingPlatformStr.isEmpty()) {
            try {
                interview.updateMeetingPlatform(meetingPlatformStr, objectMapper, request);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid meeting platform: " + meetingPlatformStr);
            }
        }
        interview.setApplication(application);
        for (ConductorDto conductor : request.getConductors()) {
            var interviewer = businessAccountRepository.findByEmail(conductor.getEmail()).orElse(null);
            if (interviewer == null) {
                interview.addOtherInterviewer(conductor);
            } else {
                interview.addInterviewer(interviewer);
            }
        }
        for (String tip : request.getPreparationTipsFromInterviewer()) {
            InterviewTip interviewTip = InterviewTip.builder().tip(tip).interview(interview).build();
            interview.getInterviewTips().add(interviewTip);
        }
        var savedInterview = interviewRepository.save(interview);
        Long previousInterviewId = request.getPreviousInterviewId();
        if (previousInterviewId != null) {
            var previousInterview = interviewRepository.findById(
                    previousInterviewId)
                    .orElseThrow(() -> new InterviewNotFoundException(
                            "Previous interview not found with id: " + previousInterviewId));
            previousInterview.setStatus(InterviewStatus.COMPLETED);
            interviewRepository.save(previousInterview);

        }
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);
        requestQueue.sendInterviewScheduledEmailsAndNotifications(savedInterview);
        return savedInterview;
    }

    public Interview updateInterview(Long interviewId, CreateInterviewDto request, Long businessAccountId) {
        var interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));
        interview.setTitle(request.getTitle());
        interview.setDescription(request.getDescription());
        interview.setInterviewDate(request.getInterviewDate());
        interview.setStartTime(request.getStartTime());
        interview.setEndTime(request.getEndTime());
        interview.setInterviewType(request.getInterviewType());
        interview.setTimezone(request.getTimezone());
        interview.setStreetAddress(request.getStreetAddress());
        interview.setBuildingName(request.getBuildingName());
        interview.setParkingInfo(request.getParkingInfo());
        interview.setContactInstructionsOnArrival(request.getContactInstructionsOnArrival());
        interview.setPhoneNumber(request.getPhoneNumber());

        if (request.getInterviewType() != InterviewType.ONLINE) {
            interview.setInterviewMeetingPlatform(null);
            interview.setOnlineMeetingInformation(null);
        } else {
            // Update the meeting platform details if needed
            String meetingPlatformStr = request.getMeetingPlatform();
            if (meetingPlatformStr != null && !meetingPlatformStr.isEmpty()) {
                try {
                    interview.updateMeetingPlatform(meetingPlatformStr, objectMapper, request);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid meeting platform: " + meetingPlatformStr);
                }
            }
        }

        // Replace all the interview tips
        interview.clearAllInterviewTips();
        for (String tip : request.getPreparationTipsFromInterviewer()) {
            InterviewTip interviewTip = InterviewTip.builder().tip(tip).interview(interview).build();
            interview.getInterviewTips().add(interviewTip);
        }

        // Get current interviewers and other interviewers
        Set<BusinessAccount> originalInterviewers = new HashSet<>(interview.getInterviewers());
        Set<ConductorDto> originalOtherInterviewers = new HashSet<>(interview.getOtherInterviewers());
        interview.clearAllInterviewersAndOtherInterviewers();
        Set<BusinessAccount> newInterviewers = new HashSet<>();
        Set<ConductorDto> newOtherInterviewers = new HashSet<>();

        // Update the hiring team
        for (ConductorDto conductor : request.getConductors()) {
            var interviewer = businessAccountRepository.findByEmail(conductor.getEmail()).orElse(null);
            if (interviewer == null) {
                interview.addOtherInterviewer(conductor);
                if (!originalOtherInterviewers.contains(conductor)) {
                    newOtherInterviewers.add(conductor);
                }
            } else {
                interview.addInterviewer(interviewer);
                if (!originalInterviewers.contains(interviewer) && !interviewer.getId().equals(businessAccountId)) {
                    System.out.println("ADDING INTERVIEWER TO NOTIFY: " + interviewer.getId() + " for interview "
                            + businessAccountId);
                    newInterviewers.add(interviewer);
                }
            }
        }
        Set<BusinessAccount> removedInterviewers = new HashSet<>();
        Set<ConductorDto> removedOtherInterviewers = new HashSet<>();
        for (BusinessAccount interviewer : originalInterviewers) {
            if (!interview.getInterviewers().stream().anyMatch(i -> i.getId().equals(interviewer.getId()))) {
                if (!interviewer.getId().equals(businessAccountId)) {
                    removedInterviewers.add(interviewer);
                }
            }
        }
        for (ConductorDto otherInterviewer : originalOtherInterviewers) {
            if (!originalOtherInterviewers.contains(otherInterviewer)) {
                removedOtherInterviewers.add(otherInterviewer);
            }
        }
        interview.setRescheduleRequest(null);
        var updatedInterview = interviewRepository.save(interview);
        // Get the intviewers that did not change
        Set<BusinessAccount> unchangedInterviewers = new HashSet<>();
        Set<ConductorDto> unchangedOtherInterviewers = new HashSet<>();
        for (BusinessAccount interviewer : originalInterviewers) {
            System.out.println("CHECKING UNCHANGED INTERVIEWER: " + interviewer.getId() + " for interview "
                    + businessAccountId + ": RESULT=" + !interviewer.getId().equals(businessAccountId));
            if (updatedInterview.getInterviewers().stream().anyMatch(i -> i.getId().equals(interviewer.getId()))
                    && !interviewer.getId().equals(businessAccountId)) {
                unchangedInterviewers.add(interviewer);
            }
        }
        for (ConductorDto otherInterviewer : originalOtherInterviewers) {
            if (updatedInterview.getOtherInterviewers().stream()
                    .anyMatch(i -> i.getEmail().equals(otherInterviewer.getEmail()))) {
                unchangedOtherInterviewers.add(otherInterviewer);
            }
        }
        requestQueue.sendInterviewUpdatedEmailsAndNotifications(updatedInterview, newInterviewers, newOtherInterviewers,
                removedInterviewers, removedOtherInterviewers, unchangedInterviewers, unchangedOtherInterviewers);
        return updatedInterview;
    }

    @Transactional
    public Boolean prepareForInterview(Long interviewId, Long candidateId) {
        var interview = interviewRepository.findInterviewForPreparation(interviewId);
        if (interview == null) {
            throw new InterviewNotFoundException("Interview not found with id: " + interviewId);
        }
        if (!interview.getCandidate().getId().equals(candidateId)) {
            return false;
        }
        // Initially set to generating so user cannot spam requests
        var interviewPreparation = InterviewPreparation.builder()
                .interview(interview)
                .status(PreparationStatus.GENERATING_PREP)
                .build();
        var savedInterviewPreparation = interviewPreparationRepository.save(interviewPreparation);
        requestQueue.processInterviewPrep(savedInterviewPreparation, interview);
        return true;
    }

    public InterviewPreparation getInterviewPreparationDetails(Long interviewId) {
        return interviewPreparationRepository.findByInterviewId(interviewId);
    }

    public InterviewPreparationQuestion getInterviewPreparationQuestionTextToSpeech(
            Long interviewId, Long interviewQuestionId) throws RuntimeException {
        var interviewPrep = interviewPreparationRepository.findByInterviewId(interviewId);
        var question = interviewPrep.getQuestions().stream()
                .filter(q -> q.getId().equals(interviewQuestionId))
                .findFirst()
                .orElse(null);
        if (question == null) {
            throw new InterviewNotFoundException("Interview question not found with id: " + interviewQuestionId);
        }
        if (question.getQuestionAudioUrl() != null && !question.getQuestionAudioUrl().isEmpty()) {
            return question;
        }
        try {
            byte[] audioBytes = aiService.textToSpeech(question.getQuestion());
            var audioFileName = s3Service.uploadInterviewPrepQuestionAudio(interviewId, interviewQuestionId,
                    audioBytes);
            question.setQuestionAudioUrl(audioFileName);
            var savedQuestion = interviewPreparationQuestionRepository.save(question);
            return savedQuestion;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate text to speech: " + e.getMessage());
        }
    }

    public InterviewPreparationQuestion getInterviewPreparationQuestionSpeechToText(
            Long interviewId, Long interviewQuestionId, MultipartFile audioFile) throws RuntimeException {
        var interviewPrep = interviewPreparationRepository.findByInterviewId(interviewId);
        var question = interviewPrep.getQuestions().stream()
                .filter(q -> q.getId().equals(interviewQuestionId))
                .findFirst()
                .orElse(null);
        if (question == null) {
            throw new InterviewNotFoundException("Interview question not found with id: " + interviewQuestionId);
        }
        // It does not matter if it already has an answer, we will overwrite it with the
        // new answer
        try {
            String answerText = aiService.speechToText(audioFile);
            var audioFileName = s3Service.uploadInterviewPrepQuestionAnswerAudio(interviewId, interviewQuestionId,
                    audioFile.getBytes());
            question.setAnswer(answerText);
            question.setAnswerAudioUrl(audioFileName);
            var savedQuestion = interviewPreparationQuestionRepository.save(question);
            return savedQuestion;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process speech to text: " + e.getMessage());
        }
    }

    public AnswerInterviewQuestionResponse answerQuestionWithAI(Long interviewId, Long candidateId,
            InterviewPreparationQuestion question) {
        var interviewPrep = interviewPreparationRepository.findByInterviewId(interviewId);
        if (interviewPrep == null) {
            throw new InterviewNotFoundException("Interview preparation not found for interview id: " + interviewId);
        }
        var interview = interviewRepository.findInterviewForPreparation(interviewId);
        if (interview == null || !interview.getCandidate().getId().equals(candidateId)) {
            throw new InterviewNotFoundException("Interview not found with id: " + interviewId);
        }
        // TODO: Extract to InterviewPreparation Entity
        var interviewQuestion = interviewPrep.getQuestions().stream()
                .filter(q -> q.getId().equals(question.getId()))
                .findFirst()
                .orElse(null);

        if (interview.getCandidate().getId() != candidateId) {
            throw new InterviewNotFoundException("You are not authorized to answer questions for this interview");
        }

        // Create new interview answer
        AnswerInterviewQuestionRequest answerInterviewQuestion = new AnswerInterviewQuestionRequest();
        AIJob aiJob = new AIJob(interview.getJob());
        AICompany aiCompany = new AICompany(interview.getJob().getBusinessAccount().getCompany());
        AICandidate aiCandidate = new AICandidate(interview.getCandidate());
        InterviewPrepQuestion aiQuestion = new InterviewPrepQuestion();
        aiQuestion.setQuestion(question.getQuestion());
        aiQuestion.setAnswer(question.getAnswer());
        answerInterviewQuestion.setJob(aiJob);
        answerInterviewQuestion.setCompany(aiCompany);
        answerInterviewQuestion.setCandidate(aiCandidate);
        answerInterviewQuestion.setQuestion(aiQuestion);

        try {
            AnswerInterviewQuestionResponse response = aiService.answerInterviewQuestion(answerInterviewQuestion);
            var audioData = aiService.textToSpeech(response.getAnswer());
            var audioFile = s3Service.uploadInterviewPrepQuestionAIAnswerAudio(interviewId, interviewQuestion.getId(),
                    audioData);
            interviewQuestion.addAIInterviewAnswer(response, audioFile);
            interviewPreparationQuestionRepository.save(interviewQuestion);
            response.setAnswerAudioUrl(audioFile);
            return response;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }

    }

    public InterviewPreparationQuestion getFeedbackForAnswerFromAI(
            Long interviewId, Long candidateId, Long interviewQuestionId, MultipartFile audioFile) {
        var interviewPrep = interviewPreparationRepository.findByInterviewId(interviewId);
        if (interviewPrep == null) {
            throw new InterviewNotFoundException("Interview preparation not found for interview id: " + interviewId);
        }
        var interview = interviewRepository.findInterviewForPreparation(interviewId);
        if (interview == null || !interview.getCandidate().getId().equals(candidateId)) {
            throw new InterviewNotFoundException("Interview not found with id: " + interviewId);
        }
        var interviewQuestion = interviewPrep.getQuestions().stream()
                .filter(q -> q.getId().equals(interviewQuestionId))
                .findFirst()
                .orElse(null);

        AnswerInterviewQuestionRequest answerInterviewQuestion = new AnswerInterviewQuestionRequest();
        AIJob aiJob = new AIJob(interview.getJob());
        AICompany aiCompany = new AICompany(interview.getJob().getBusinessAccount().getCompany());
        AICandidate aiCandidate = new AICandidate(interview.getCandidate());
        InterviewPrepQuestion aiQuestion = new InterviewPrepQuestion();
        answerInterviewQuestion.setJob(aiJob);
        answerInterviewQuestion.setCompany(aiCompany);
        answerInterviewQuestion.setCandidate(aiCandidate);
        answerInterviewQuestion.setQuestion(aiQuestion);
        var oldAnswer = interviewQuestion.getAnswer();
        var newAnswer = getInterviewPreparationQuestionSpeechToText(interviewId, interviewQuestionId, audioFile)
                .getAnswer();
        var referenceToPreviousAnswer = new ReferenceToPreviousAnswer(interviewQuestion, oldAnswer, newAnswer);
        AnswerInterviewQuestionResponse response = aiService.getFeedbackForAnswer(answerInterviewQuestion,
                referenceToPreviousAnswer);
        response.setAnswerAudioUrl(interviewQuestion.getAiAnswerAudioUrl());
        interviewQuestion.updateViaAiFeedback(response);
        var savedInterviewQuestion = interviewPreparationQuestionRepository.save(interviewQuestion);
        return savedInterviewQuestion;
    }

    @Transactional
    public void markInterviewAsCompleted(Long interviewId) {
        var interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));
        interview.setStatus(InterviewStatus.COMPLETED);
        var application = interview.getApplication();
        application.setStatus(ApplicationStatus.INTERVIEW_COMPLETED);
        applicationRepository.save(application);
        userNotificationService.sendInAppNotificationForInterviewCompletion(interview);
        interviewRepository.save(interview);
    }

    @Transactional
    public void cancelInterview(Long interviewId, String reason) {
        var interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));
        interview.setStatus(InterviewStatus.CANCELLED);
        interview.setCancellationReason(reason);
        interview.setOnlineMeetingInformation(null);
        interview.updateInterviewApplicationStatus();
        requestQueue.sendInterviewCancelledEmailsAndNotifications(interview);
        interviewRepository.save(interview);
    }

    @Transactional
    public Interview rejectCandidateInterview(Long interviewId, String reason, String feedback) {
        var interview = interviewRepository.findByInterviewWithApplication(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));
        var application = interview.getApplication();
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setDecisionResult(InterviewDecisionResult.REJECTED);
        InterviewRejection rejection = InterviewRejection
                .builder()
                .interview(interview)
                .application(application)
                .reason(reason)
                .feedback(feedback)
                .build();
        interview.setRejection(rejection);
        var savedInterview = interviewRepository.save(interview);
        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);
        requestQueue.sendRejectionAfterInterviewEmailsAndNotifications(savedInterview);
        return savedInterview;
    }

    public InterviewDto secureDetailedInterview(InterviewDto interviewDto, String userEmail) {
        JsonNode onlineMeetingInfo = interviewDto.getOnlineMeetingInformation();
        if (onlineMeetingInfo != null && onlineMeetingInfo.has("registrants")) {
            ArrayNode registrants = (ArrayNode) onlineMeetingInfo.get("registrants");
            // Find registrant matching the user's email
            JsonNode userRegistrant = null;
            for (JsonNode registrant : registrants) {
                if (registrant.has("email") && userEmail.equalsIgnoreCase(registrant.get("email").asText())) {
                    userRegistrant = registrant;
                    break;
                }
            }
            ObjectNode safeMeetingInfo = onlineMeetingInfo.deepCopy();
            safeMeetingInfo.remove("registrants");
            if (userRegistrant != null && userRegistrant.has("join_url")) {
                safeMeetingInfo.put("joinUrl", userRegistrant.get("join_url").asText());
            }
            safeMeetingInfo.putNull("meetingPassword");
            interviewDto.setOnlineMeetingInformation(safeMeetingInfo);
        }
        return interviewDto;
    }

    public void createInterviewRescheduleRequest(Long interviewId, CreateInterviewRescheduleDto request) {
        var interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));
        var existingRequest = interview.getRescheduleRequest();
        if (existingRequest != null) {
            existingRequest.setInterviewDate(request.getInterviewDate());
            existingRequest.setStartTime(request.getStartTime());
            existingRequest.setReason(request.getReason());
            existingRequest.setTimezone(request.getTimezone());
            interview.setRescheduleRequest(existingRequest);
        } else {
            var rescheduleRequest = InterviewRescheduleRequest.builder()
                    .interview(interview)
                    .interviewDate(request.getInterviewDate())
                    .startTime(request.getStartTime())
                    .reason(request.getReason())
                    .timezone(request.getTimezone())
                    .build();
            interview.setRescheduleRequest(rescheduleRequest);
        }
        interviewRepository.save(interview);
        requestQueue.sendInterviewRescheduleRequestEmailsAndNotifications(interview);
    }

    public void sendInterviewPreparationResourcesViaEmail(Long interviewId) {
        // Get the intervie prep
        var interview = interviewRepository.findById(interviewId).orElse(null);
        if (interview == null) {
            throw new InterviewNotFoundException("Interview not found with id: " + interviewId);
        }
        var interviewPrep = interview.getPreparation();
        if (interviewPrep == null) {
            throw new InterviewNotFoundException("Interview preparation not found for interview id: " + interviewId);
        }
        var resources = interviewPrep.getResources();
        if (resources == null || resources.isEmpty()) {
            throw new InterviewNotFoundException("No preparation resources found for interview id: " + interviewId);
        }
        var jobTitle = interview.getJob().getTitle();
        var companyName = interview.getJob().getCompany().getName();
        var candidate = interview.getCandidate();
        var candidateEmail = candidate.getAccount().getEmail();
        var candidateFullName = candidate.getFullName();
        requestQueue.sendInterviewPrepResourcesViaEmail(resources, companyName, jobTitle, candidateFullName,
                candidateEmail);
    }

    public void setHelpMeRememberTrue(Long interviewId) {
        var interviewPrep = interviewPreparationRepository.findByInterviewId(interviewId);
        if (interviewPrep == null) {
            throw new InterviewNotFoundException("Interview preparation not found for interview id: " + interviewId);
        }
        interviewPrep.setHelpMeRemember(true);
        interviewPreparationRepository.save(interviewPrep);
    }

    public void submitFeedbackForInterviewPrep(Long interviewId, Integer rating, String comments) {
        var interviewPrep = interviewPreparationRepository.findByInterviewId(interviewId);
        if (interviewPrep == null) {
            throw new InterviewNotFoundException("Interview preparation not found for interview id: " + interviewId);
        }
        var interviewFeedback = InterviewPreparationFeedback.builder().reviewRating(rating).reviewText(comments)
                .interviewPreparation(interviewPrep).build();
        interviewPrep.setFeedback(interviewFeedback);
        interviewPreparationRepository.save(interviewPrep);
    }

    public InterviewPreparationFeedback getInterviewPrepFeedback(Long interviewId) {
        var interviewPrep = interviewPreparationFeedbackRepository.findByInterviewPreparationId(interviewId);
        return interviewPrep;
    }
}

package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.InterviewPreparationQuestion;
import com.rizvi.jobee.entities.InterviewRejection;
import com.rizvi.jobee.entities.InterviewTip;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.InterviewDecisionResult;
import com.rizvi.jobee.enums.InterviewStatus;
import com.rizvi.jobee.enums.PreparationStatus;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.InterviewNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AICandidate;
import com.rizvi.jobee.helpers.AISchemas.AICompany;
import com.rizvi.jobee.helpers.AISchemas.AIInterview;
import com.rizvi.jobee.helpers.AISchemas.AIJob;
import com.rizvi.jobee.helpers.AISchemas.AnswerInterviewQuestionRequest;
import com.rizvi.jobee.helpers.AISchemas.AnswerInterviewQuestionResponse;
import com.rizvi.jobee.helpers.AISchemas.InterviewPrepQuestion;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.ReferenceToPreviousAnswer;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.InterviewPreparationQuestionRepository;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;
import com.rizvi.jobee.repositories.InterviewRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class InterviewService {
    private final BusinessAccountRepository businessAccountRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewPreparationRepository interviewPreparationRepository;
    private final InterviewPreparationQuestionRepository interviewPreparationQuestionRepository;
    private final ApplicationRepository applicationRepository;
    private final AIService aiService;
    private final S3Service s3Service;
    private final InterviewPrepQueue interviewPrepQueue;
    private final UserNotificationService userNotificationService;

    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
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
                .meetingLink(request.getMeetingLink())
                .phoneNumber(request.getPhoneNumber())
                .interviewMeetingPlatform(request.getMeetingPlatform())
                .status(InterviewStatus.SCHEDULED)
                .createdBy(businessAccount)
                .build();
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
        userNotificationService.createInterviewScheduledNotificationAndSend(savedInterview);
        return savedInterview;
    }

    @Transactional
    public Boolean prepareForInterview(Long interviewId, Long candidateId) {
        // If the interview already has a preparation, return False since we cannot
        // reprepare again
        var interview = interviewRepository.findInterviewForPreparation(interviewId);
        if (interview == null) {
            throw new InterviewNotFoundException("Interview not found with id: " + interviewId);
        }
        if (!interview.getCandidate().getId().equals(candidateId)) {
            return false;
        }
        // Create new interview preparation
        var interviewPreparation = InterviewPreparation.builder()
                .interview(interview)
                .status(PreparationStatus.IN_PROGRESS)
                .build();
        var savedInterviewPreparation = interviewPreparationRepository.save(interviewPreparation);
        AIJob aiJob = new AIJob(interview.getJob());
        AICompany aiCompany = new AICompany(interview.getJob().getBusinessAccount().getCompany());
        AICandidate aiCandidate = new AICandidate(interview.getCandidate());
        AIInterview aiInterview = new AIInterview(interview);

        PrepareForInterviewRequest prepareForInterviewRequest = new PrepareForInterviewRequest(aiJob, aiCompany,
                aiCandidate, aiInterview);
        interviewPrepQueue.processInterviewPrep(prepareForInterviewRequest, savedInterviewPreparation);

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
        // If it has an audio url, then we already generated it so simply return the aws
        // bucket url
        if (question.getQuestionAudioUrl() != null && !question.getQuestionAudioUrl().isEmpty()) {
            return question;
        }
        // Otherwise, we need to generate the audio using AI and store it in the bucket
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
        userNotificationService.createInterviewRejectionNotificationAndSend(savedInterview);
        return savedInterview;
    }
}

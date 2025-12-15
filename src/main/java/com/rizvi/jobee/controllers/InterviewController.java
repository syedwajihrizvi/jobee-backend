package com.rizvi.jobee.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.interview.CancelInterviewRequestDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewRejectionDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewRescheduleDto;
import com.rizvi.jobee.dtos.interview.InterviewDto;
import com.rizvi.jobee.dtos.interview.InterviewPrepQuestionDto;
import com.rizvi.jobee.dtos.interview.InterviewPreparationDto;
import com.rizvi.jobee.dtos.interview.InterviewSummaryDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.enums.BusinessType;
import com.rizvi.jobee.dtos.application.ApplicationDto;
import com.rizvi.jobee.mappers.InterviewMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.queries.InterviewQuery;
import com.rizvi.jobee.services.AccountService;
import com.rizvi.jobee.services.ApplicationService;
import com.rizvi.jobee.services.InterviewService;
import com.rizvi.jobee.services.JobService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/interviews")
@AllArgsConstructor
public class InterviewController {
    private final AccountService accountService;
    private final JobService jobService;
    private final InterviewService interviewService;
    private final UserProfileService userProfileService;
    private final ApplicationService applicationService;
    private final InterviewMapper interviewMapper;

    @GetMapping()
    @Operation(summary = "Get interviews")
    public ResponseEntity<PaginatedResponse<InterviewSummaryDto>> getInterviews(
            @ModelAttribute InterviewQuery query,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Number limit,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var accountId = principal.getId();
        var accountType = principal.getAccountType();
        var companyId = principal.getCompanyId();
        if (accountType.equals(BusinessType.RECRUITER.name())) {
            query.setPostedById(accountId);
        } else if (accountType.equals(BusinessType.EMPLOYEE.name())) {
            query.setConductorId(accountId);
        }
        query.setCompanyId(companyId);
        var paginatedInterviews = interviewService.getAllInterviews(query, pageNumber, pageSize);
        var interviews = paginatedInterviews.getContent();
        var hasMore = paginatedInterviews.isHasMore();
        var totalInterviews = paginatedInterviews.getTotalElements();
        var interviewDtos = interviews.stream().map(interviewMapper::toSummaryDto).toList();
        PaginatedResponse<InterviewSummaryDto> response = new PaginatedResponse<>(hasMore, interviewDtos,
                totalInterviews);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interview by ID")
    public ResponseEntity<InterviewDto> getInterviewById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var interview = interviewService.getInterviewById(id);
        var userEmail = principal.getEmail();
        var interviewDto = interviewMapper.toDto(interview);
        var secureInterviewDto = interviewService.secureDetailedInterview(interviewDto, userEmail);
        return ResponseEntity.ok(secureInterviewDto);
    }

    @GetMapping("/candidate/me")
    @Operation(summary = "Get interviews for the authenticated candidate")
    public ResponseEntity<List<InterviewSummaryDto>> getInterviewsForAuthenticatedCandidate(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var candidateId = principal.getId();
        var interviews = interviewService.getInterviewsByCandidate(candidateId);
        var interviewSummaryDtos = interviews.stream()
                .map(interviewMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(interviewSummaryDtos);
    }

    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "Get interviews for a specific candidate")
    public ResponseEntity<List<InterviewSummaryDto>> getInterviewsByCandidateId(@PathVariable Long candidateId) {
        var interviews = interviewService.getInterviewsByCandidate(candidateId);
        var interviewSummaryDtos = interviews.stream()
                .map(interviewMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(interviewSummaryDtos);
    }

    @GetMapping("/business")
    @Operation(summary = "Get interviews for the authenticated business.")
    public ResponseEntity<List<InterviewSummaryDto>> getInterviewsByBusinessId(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var businessId = principal.getId();
        var accountType = principal.getAccountType();
        List<Interview> interviews = new ArrayList<>();
        if (accountType.equals(BusinessType.ADMIN.name())) {
            interviews = interviewService.getInterviewsByCompanyId(businessId);
        } else if (accountType.equals(BusinessType.RECRUITER.name())) {
            interviews = interviewService.getInterviewsForRecruiter(businessId);
        } else if (accountType.equals(BusinessType.EMPLOYEE.name())) {
            interviews = interviewService.getInterviewsForEmployee(businessId);
        }
        var interviewSummaryDtos = interviews.stream()
                .map(interviewMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(interviewSummaryDtos);
    }

    @GetMapping("/application")
    @Operation(summary = "Get application by job ID and candidate ID")
    public ResponseEntity<ApplicationDto> getApplicationByJobAndCandidate(
            @RequestParam Long jobId,
            @RequestParam Long candidateId) {
        var application = applicationService.getApplicationByJobAndCandidate(jobId, candidateId);
        return ResponseEntity.ok(application);
    }

    @PostMapping()
    @Operation(summary = "Business can schedule an interview for a candidate")
    public ResponseEntity<InterviewDto> createInterview(
            @Valid @RequestBody CreateInterviewDto request,
            @AuthenticationPrincipal CustomPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var creatorId = principal.getId();
        var creator = accountService.getBusinessAccountById(creatorId);
        var job = jobService.getJobById(request.getJobId());
        var userProfile = userProfileService.getUserProfileById(request.getCandidateId());
        var applicationId = request.getApplicationId();
        var application = applicationService.findById(applicationId);
        var savedInterview = interviewService.createInterview(request, creator, userProfile, job, application);
        var uri = uriComponentsBuilder.path("/interviews/{id}").buildAndExpand(savedInterview.getId()).toUri();
        return ResponseEntity.created(uri).body(interviewMapper.toDto(savedInterview));
    }

    @PostMapping("/{id}/prepare")
    @Operation(summary = "Candidate wishes to use Jobsee AI to prepare for the interview")
    public ResponseEntity<Void> prepareForInterview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var candidateId = principal.getId();
        interviewService.prepareForInterview(id, candidateId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/mark-as-completed")
    @Operation(summary = "Mark the interview as completed")
    public ResponseEntity<Void> markInterviewAsCompleted(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal) {
        interviewService.markInterviewAsCompleted(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an interview")
    public ResponseEntity<Void> cancelInterview(
            @PathVariable Long id,
            @RequestBody @Valid CancelInterviewRequestDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        interviewService.cancelInterview(id, request.getCancellationReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/prepare")
    @Operation(summary = "Get interview preparation details")
    public ResponseEntity<InterviewPreparationDto> getInterviewPreparationDetails(
            @PathVariable Long id) {
        var interviewPreparation = interviewService.getInterviewPreparationDetails(id);
        var interviewPreparationDto = interviewMapper.toPreparationDto(interviewPreparation);
        return ResponseEntity.ok(interviewPreparationDto);
    }

    @GetMapping("/{id}/prepare/questions")
    @Operation(summary = "Get interview preparation questions")
    public ResponseEntity<List<InterviewPrepQuestionDto>> getInterviewPreparationQuestions(
            @PathVariable Long id) {
        var interviewPreparation = interviewService.getInterviewPreparationDetails(id);
        var interviewPrepQuestionDtos = interviewPreparation.getQuestions().stream()
                .map(interviewMapper::toInterviewPrepQuestionDto)
                .toList();
        return ResponseEntity.ok(interviewPrepQuestionDtos);
    }

    @GetMapping("/{id}/prepare/questions/{interviewQuestionId}")
    public ResponseEntity<InterviewPrepQuestionDto> getInterviewPreparationQuestion(
            @PathVariable Long id, @PathVariable Long interviewQuestionId) {
        var interviewQuestion = interviewService.getInterviewPreparationQuestion(interviewQuestionId);
        return ResponseEntity.ok(interviewMapper.toInterviewPrepQuestionDto(interviewQuestion));
    }

    @PostMapping("/{id}/prepare/questions/{interviewQuestionId}/question/text-to-speech")
    @Operation(summary = "Convert text to speech for interview question")
    public ResponseEntity<InterviewPrepQuestionDto> getQuestionTextToSpeech(
            @PathVariable Long id, @PathVariable Long interviewQuestionId) {
        var interviewPrepQuestion = interviewService.getInterviewPreparationQuestionTextToSpeech(id,
                interviewQuestionId);
        var interviewPrepQuestionDto = new InterviewPrepQuestionDto();
        interviewPrepQuestionDto.setId(interviewPrepQuestion.getId());
        interviewPrepQuestionDto.setQuestion(interviewPrepQuestion.getQuestion());
        interviewPrepQuestionDto.setQuestionAudioUrl(interviewPrepQuestion.getQuestionAudioUrl());
        interviewPrepQuestionDto.setAnswer(interviewPrepQuestion.getAnswer());
        interviewPrepQuestionDto.setAnswerAudioUrl(interviewPrepQuestion.getAnswerAudioUrl());
        return ResponseEntity.ok(interviewPrepQuestionDto);
    }

    @PostMapping("{id}/prepare/questions/{interviewQuestionId}/answer/speech-to-text")
    @Operation(summary = "Convert speech to text for interview answer. Called when candidate submits their answer for the first time.")
    public ResponseEntity<InterviewPrepQuestionDto> getAnswerSpeechToText(
            @PathVariable Long id,
            @PathVariable Long interviewQuestionId,
            @RequestParam("audioFile") MultipartFile audioFile,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var interviewPrepQuestion = interviewService.getInterviewPreparationQuestionSpeechToText(id,
                interviewQuestionId, audioFile);
        interviewService.answerQuestionWithAI(id, principal.getId(), interviewPrepQuestion);
        return ResponseEntity.ok(interviewMapper.toInterviewPrepQuestionDto(interviewPrepQuestion));
    }

    @PostMapping("{id}/prepare/questions/{interviewQuestionId}/answer/feedback")
    public ResponseEntity<InterviewPrepQuestionDto> getFeedbackForAnswer(
            @PathVariable Long id,
            @PathVariable Long interviewQuestionId,
            @RequestParam("audioFile") MultipartFile audioFile,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var interviewQuestion = interviewService.getFeedbackForAnswerFromAI(id, principal.getId(), interviewQuestionId,
                audioFile);
        return ResponseEntity.ok(interviewMapper.toInterviewPrepQuestionDto(interviewQuestion));
    }

    @PostMapping("{id}/reject-candidate")
    @Operation(summary = "Business can reject an interview")
    public ResponseEntity<InterviewDto> rejectInterview(
            @PathVariable Long id,
            @RequestBody @Valid CreateInterviewRejectionDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var reason = request.getReason();
        var feedback = request.getFeedback();
        var savedInterview = interviewService.rejectCandidateInterview(id, reason, feedback);
        return ResponseEntity.ok(interviewMapper.toDto(savedInterview));
    }

    @PostMapping("{id}/request-reschedule")
    @Operation(summary = "Candidate can submit a request to reschedule an interview")
    public ResponseEntity<Void> submitRescheduleRequest(
            @PathVariable Long id, @RequestBody CreateInterviewRescheduleDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        interviewService.createInterviewRescheduleRequest(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    @Operation(summary = "Business can update an interview")
    public ResponseEntity<InterviewDto> updateInterview(
            @PathVariable Long id,
            @RequestBody @Valid CreateInterviewDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {

        var updatedInterview = interviewService.updateInterview(id, request);
        return ResponseEntity.ok(interviewMapper.toDto(updatedInterview));
    }
}

package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.dtos.interview.InterviewDto;
import com.rizvi.jobee.dtos.interview.InterviewPrepQuestionDto;
import com.rizvi.jobee.dtos.interview.InterviewPreparationDto;
import com.rizvi.jobee.dtos.interview.InterviewSummaryDto;
import com.rizvi.jobee.mappers.InterviewMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.AccountService;
import com.rizvi.jobee.services.ApplicationService;
import com.rizvi.jobee.services.InterviewService;
import com.rizvi.jobee.services.JobService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/interviews")
@AllArgsConstructor
public class InterviewController {
    private final AccountService accountService;
    private final JobService jobService;
    private final InterviewService interviewService;
    private final UserProfileService userProfileService;
    private final ApplicationService applicationService;
    private final InterviewMapper interviewMapper;

    @GetMapping()
    public ResponseEntity<List<InterviewDto>> getAllInterviews() {
        var interviews = interviewService.getAllInterviews();
        var interviewDtos = interviews.stream().map(interviewMapper::toDto).toList();
        return ResponseEntity.ok(interviewDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interview by ID")
    public ResponseEntity<InterviewDto> getInterviewById(@PathVariable Long id) {
        var interview = interviewService.getInterviewById(id);
        var interviewDto = interviewMapper.toDto(interview);
        return ResponseEntity.ok(interviewDto);
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

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get interviews for a specific job")
    public ResponseEntity<List<InterviewSummaryDto>> getInterviewsByJobId(
            @PathVariable Long jobId,
            @RequestParam(required = false) Number limit) {
        var interviews = interviewService.getInterviewsByJobId(jobId, limit);
        var interviewDtos = interviews.stream()
                .map(interviewMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(interviewDtos);
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
}

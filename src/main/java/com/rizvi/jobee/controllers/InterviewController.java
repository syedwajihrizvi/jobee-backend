package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.dtos.interview.InterviewDto;
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
    public ResponseEntity<List<InterviewDto>> getInterviewsByJobId(
            @PathVariable Long jobId,
            @RequestParam(required = false) Number limit) {
        var interviews = interviewService.getInterviewsByJobId(jobId, limit);
        var interviewDtos = interviews.stream()
                .map(interviewMapper::toDto)
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
}

package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateInterviewDto;
import com.rizvi.jobee.dtos.InterviewDto;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.enums.InterviewStatus;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.InterviewMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.InterviewRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/interviews")
@AllArgsConstructor
public class InterviewController {
    private final InterviewRepository interviewRepository;
    private final UserProfileRepository userProfileRepository;
    private final JobRepository jobRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final InterviewMapper interviewMapper;

    @GetMapping()
    public ResponseEntity<List<InterviewDto>> getAllInterviews() {
        List<Interview> interviews = interviewRepository.findAll();
        List<InterviewDto> interviewDtos = interviews.stream()
                .map(interviewMapper::toDto)
                .toList();
        return ResponseEntity.ok(interviewDtos);
    }

    @PostMapping()
    public ResponseEntity<InterviewDto> createInterview(
            @Valid @RequestBody CreateInterviewDto request,
            @AuthenticationPrincipal CustomPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var job = jobRepository.findById(request.getJobId()).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job not found with ID: " + request.getJobId());
        }

        var userProfile = userProfileRepository.findById(request.getCandidateId()).orElse(null);
        if (userProfile == null) {
            throw new AccountNotFoundException("User profile not found with ID: " + request.getCandidateId());
        }
        var interviewerId = principal.getId();
        var interviewer = businessAccountRepository.findById(interviewerId).orElse(null);
        if (interviewer == null) {
            throw new AccountNotFoundException("Interviewer not found with ID: " + interviewerId);
        }
        var interview = Interview.builder()
                .scheduledTime(request.getScheduledTime())
                .description(request.getDescription())
                .status(InterviewStatus.SCHEDULED)
                .job(job)
                .candidate(userProfile)
                .interviewer(interviewer)
                .build();
        var savedInterview = interviewRepository.save(interview);
        var uri = uriComponentsBuilder.path("/interviews/{id}").buildAndExpand(savedInterview.getId()).toUri();
        return ResponseEntity.created(uri).body(interviewMapper.toDto(savedInterview));
    }
}

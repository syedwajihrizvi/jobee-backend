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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.dtos.interview.InterviewDto;
import com.rizvi.jobee.dtos.interview.InterviewSummaryDto;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.InterviewStatus;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.ApplicationNotFoundException;
import com.rizvi.jobee.exceptions.InterviewNotFoundException;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.InterviewMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.InterviewRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/interviews")
@AllArgsConstructor
public class InterviewController {
    private final InterviewRepository interviewRepository;
    private final UserProfileRepository userProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
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

    @GetMapping("/{id}")
    @Operation(summary = "Get interview by ID")
    public ResponseEntity<InterviewDto> getInterviewById(@PathVariable Long id) {
        Interview interview = interviewRepository.findById(id).orElseThrow(
                () -> new InterviewNotFoundException("Interview not found with ID: " + id));
        InterviewDto interviewDto = interviewMapper.toDto(interview);
        return ResponseEntity.ok(interviewDto);
    }

    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "Get interviews for a specific candidate")
    public ResponseEntity<List<InterviewSummaryDto>> getInterviewsByCandidateId(@PathVariable Long candidateId) {
        List<Interview> interviews = interviewRepository.findByCandidateId(candidateId);
        List<InterviewSummaryDto> interviewSummaryDtos = interviews.stream()
                .map(interviewMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(interviewSummaryDtos);
    }

    @PostMapping()
    @Transactional
    @Operation(summary = "Business can schedule an interview for a candidate")
    public ResponseEntity<InterviewDto> createInterview(
            @Valid @RequestBody CreateInterviewDto request,
            // @AuthenticationPrincipal CustomPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        System.out.println(request);
        var creator_id = 54L;
        // var creator_id = principal.getId();
        var creator = businessAccountRepository.findById(creator_id).orElse(null);
        var job = jobRepository.findById(request.getJobId()).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job not found with ID: " +
                    request.getJobId());
        }

        var userProfile = userProfileRepository.findById(request.getCandidateId()).orElse(null);
        if (userProfile == null) {
            throw new AccountNotFoundException("User profile not found with ID: " +
                    request.getCandidateId());
        }
        // Convert interviewDate and startTime to LocalDate and LocalTime
        var interview = Interview.builder()
                .job(job)
                .candidate(userProfile)
                .title(request.getTitle())
                .description(request.getDescription())
                .interviewDate(request.getInterviewDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .interviewType(request.getInterviewType())
                .timezone(request.getTimezone())
                .location(request.getLocation())
                .meetingLink(request.getMeetingLink())
                .phoneNumber(request.getPhoneNumber())
                .status(InterviewStatus.SCHEDULED)
                .createdBy(creator)
                .build();
        for (ConductorDto conductor : request.getConductors()) {
            var interviewer = businessAccountRepository.findByEmail(conductor.getEmail()).orElse(null);
            if (interviewer == null) {
                interview.addOtherInterviewer(conductor);
            } else {
                interview.addInterviewer(interviewer);
            }
        }
        var savedInterview = interviewRepository.save(interview);
        var applicationId = request.getApplicationId();
        var application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            throw new ApplicationNotFoundException("Application not found with ID: " + applicationId);
        }
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);
        var uri = uriComponentsBuilder.path("/interviews/{id}").buildAndExpand(savedInterview.getId()).toUri();
        return ResponseEntity.created(uri).body(interviewMapper.toDto(savedInterview));
    }
}

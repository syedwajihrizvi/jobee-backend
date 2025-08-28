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
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.ApplicantSummaryForBusinessDto;
import com.rizvi.jobee.dtos.ApplicationDto;
import com.rizvi.jobee.dtos.CreateApplicationDto;
import com.rizvi.jobee.dtos.JobApplicationStatusDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.exceptions.UserDocumentNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserDocumentRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final UserProfileRepository userProfileRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final ApplicationMapper applicationMapper;

    @GetMapping()
    public ResponseEntity<List<?>> getAllApplications(
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            var userProfile = userProfileRepository.findById(userId).orElse(null);
            if (userProfile == null) {
                throw new AccountNotFoundException("User profile not found for user id: " + userId);
            }
            var applications = applicationRepository.findByUserProfile(userProfile);
            var applicationDtos = applications.stream().map(application -> {
                var dto = new JobApplicationStatusDto();
                dto.setJob(jobMapper.toSummaryDto(application.getJob()));
                dto.setStatus(application.getStatus());
                return dto;
            }).toList();
            return ResponseEntity.ok(applicationDtos);
        }
        var applications = applicationRepository.findAll();
        var applicationDtos = applications.stream().map(applicationMapper::toDto).toList();
        return ResponseEntity.ok(applicationDtos);
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<List<ApplicantSummaryForBusinessDto>> getApplicationsForJobs(
            @PathVariable Long id) {
        var applications = applicationRepository.findByJobId(id);
        var applicationDtos = applications.stream().map(applicationMapper::toApplicantSummaryForBusinessDto).toList();
        return ResponseEntity.ok(applicationDtos);
    }

    @PostMapping
    public ResponseEntity<ApplicationDto> createApplication(
            @Valid @RequestBody CreateApplicationDto createApplicationDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var jobId = createApplicationDto.getJobId();
        var job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job with ID " + jobId + " not found");
        }
        var userId = customPrincipal.getId();
        var userProfile = userProfileRepository.findById(userId).orElse(null);
        if (userProfile == null) {
            throw new AccountNotFoundException("User account with ID " + userId + " not found");
        }
        var application = new Application();
        application.setJob(job);
        application.setUserProfile(userProfile);
        var coverLetterDocumentId = createApplicationDto.getCoverLetterDocumentId();
        var resumeDocumentId = createApplicationDto.getResumeDocumentId();
        if (coverLetterDocumentId != null) {
            var coverLetterDocument = userDocumentRepository.findById(coverLetterDocumentId)
                    .orElseThrow(() -> new UserDocumentNotFoundException(coverLetterDocumentId));
            application.setCoverLetterDocument(coverLetterDocument);
        }
        if (resumeDocumentId == null) {
            throw new UserDocumentNotFoundException("Resume document ID is required");
        }
        var resumeDocument = userDocumentRepository.findById(resumeDocumentId)
                .orElseThrow(() -> new UserDocumentNotFoundException(resumeDocumentId));
        application.setResumeDocument(resumeDocument);
        var savedApplication = applicationRepository.save(application);
        var uri = uriComponentsBuilder.path("/applications/{id}")
                .buildAndExpand(savedApplication.getId()).toUri();
        return ResponseEntity.created(uri).body(applicationMapper.toDto(savedApplication));
    }
}

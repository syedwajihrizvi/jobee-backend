package com.rizvi.jobee.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.application.ApplicantSummaryForBusinessDto;
import com.rizvi.jobee.dtos.application.ApplicationDetailsForBusinessDto;
import com.rizvi.jobee.dtos.application.ApplicationDto;
import com.rizvi.jobee.dtos.application.BatchQuickApplyDto;
import com.rizvi.jobee.dtos.application.BatchQuickApplySuccessDto;
import com.rizvi.jobee.dtos.application.CreateApplicationDto;
import com.rizvi.jobee.dtos.job.JobApplicationStatusDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.exceptions.UserDocumentNotFoundException;
import com.rizvi.jobee.exceptions.ApplicationNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.queries.ApplicationQuery;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserDocumentRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.JobService;
import com.rizvi.jobee.services.UserProfileService;
import com.rizvi.jobee.specifications.ApplicantSpecification;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final JobService jobService;
    private final JobMapper jobMapper;
    private final UserProfileService userProfileService;
    private final UserProfileRepository userProfileRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final ApplicationMapper applicationMapper;

    @GetMapping()
    public ResponseEntity<List<?>> getAllApplications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long jobId) {
        if (userId != null && jobId == null) {
            var userProfile = userProfileRepository.findById(userId).orElse(null);
            if (userProfile == null) {
                throw new AccountNotFoundException("User profile not found for user id: " + userId);
            }
            var sort = Sort.by("createdAt").descending();
            var applications = applicationRepository.findByUserProfile(userProfile, sort);
            var applicationDtos = applications.stream().map(application -> {
                var dto = new JobApplicationStatusDto();
                dto.setJob(jobMapper.toSummaryDto(application.getJob()));
                dto.setApplicationId(application.getId());
                dto.setStatus(application.getStatus());
                dto.setAppliedAt(application.getCreatedAt().toString());
                return dto;
            }).toList();
            return ResponseEntity.ok(applicationDtos);
        }
        if (userId != null && jobId != null) {
            var applications = applicationRepository.findByUserProfileIdAndJobId(userId, jobId);
            var applicationDtos = applications.stream().map(applicationMapper::toApplicationDetailsForBusinessDto)
                    .toList();
            return ResponseEntity.ok(applicationDtos);
        }

        var applications = applicationRepository.findAll();
        var applicationDtos = applications.stream().map(applicationMapper::toDto).toList();
        return ResponseEntity.ok(applicationDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "For business users to get full application details about a user")
    public ResponseEntity<ApplicationDetailsForBusinessDto> getApplication(
            @PathVariable Long id) {
        var application = applicationRepository.findById(id).orElseThrow(
                () -> new ApplicationNotFoundException("Application with ID " + id + " not found"));
        return ResponseEntity.ok(applicationMapper.toApplicationDetailsForBusinessDto(application));
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<List<ApplicantSummaryForBusinessDto>> getApplicationsForJobs(
            @ModelAttribute ApplicationQuery query,
            @PathVariable Long id) {
        query.setJobId(id);
        var applications = applicationRepository.findAll(ApplicantSpecification.withFilters(query));
        var applicationDtos = applications.stream().map(applicationMapper::toApplicantSummaryForBusinessDto).toList();
        return ResponseEntity.ok(applicationDtos);
    }

    @GetMapping("/me")
    @Operation(summary = "Get applications for jobs posted by the authenticated business user")
    public ResponseEntity<List<ApplicantSummaryForBusinessDto>> getApplicationsForJobsPostedByUser(
            @RequestParam(required = false) String pending,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var jobs = jobService.getJobsByBusinessAccountId(userId, null);
        var applications = Job.getApplicationsFromJobs(jobs);
        var response = applications.stream()
                .map(applicationMapper::toApplicantSummaryForBusinessDto)
                .sorted((a1, a2) -> a2.getAppliedAt().compareTo(a1.getAppliedAt()))
                .toList();
        if (pending != null && pending.equals("true")) {
            response = response.stream().filter(app -> app.getStatus().equals("PENDING")).toList();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/quickApply")
    @Operation(summary = "Quick apply to job with logged in user")
    public ResponseEntity<ApplicationDto> quickApplyToJob(
            @RequestBody CreateApplicationDto createQuickApplicationDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            UriComponentsBuilder uriComponentsBuilder) {
        var userId = customPrincipal.getId();
        var userProfile = userProfileRepository.findUserById(userId).get();
        if (userProfile == null) {
            throw new AccountNotFoundException("User account with ID " + userId + " not found");
        }
        var primaryResume = userProfile.getPrimaryResume();
        if (primaryResume == null) {
            throw new UserDocumentNotFoundException("User does not have a primary resume set");
        }
        var application = new Application();
        var jobId = createQuickApplicationDto.getJobId();
        var job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job with ID " + jobId + " not found");
        }
        application.setJob(job);
        application.setUserProfile(userProfile);
        application.setResumeDocument(primaryResume);
        var savedApplication = applicationRepository.save(application);
        var uri = uriComponentsBuilder.path("/applications/{id}")
                .buildAndExpand(savedApplication.getId()).toUri();
        return ResponseEntity.created(uri).body(applicationMapper.toDto(savedApplication));
    }

    @PostMapping("/quickApplyBatch")
    @Operation(summary = "Quick apply to multiple jobs with logged in user")
    @Transactional
    public ResponseEntity<BatchQuickApplySuccessDto> quickApplyToMultipleJobs(
            @RequestBody BatchQuickApplyDto batchQuickApplyDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            UriComponentsBuilder uriComponentsBuilder) {
        var userId = customPrincipal.getId();
        var userProfile = userProfileRepository.findUserById(userId).get();
        if (userProfile == null) {
            throw new AccountNotFoundException("User account with ID " + userId + " not found");
        }
        var primaryResume = userProfile.getPrimaryResume();
        if (primaryResume == null) {
            throw new UserDocumentNotFoundException("User does not have a primary resume set");
        }
        var savedApplications = batchQuickApplyDto.getJobIds().stream().map(jobId -> {
            var job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                throw new JobNotFoundException("Job with ID " + jobId + " not found");
            }
            if (!job.hasUserApplied(userId)) {
                var application = new Application();
                application.setJob(job);
                application.setUserProfile(userProfile);
                application.setResumeDocument(primaryResume);
                System.out.println("SUCCESS: User applying to job with ID " + jobId);
                return applicationRepository.save(application);
            }
            System.out.println("ERROR: User has already applied to job with ID " + jobId);
            return null;
        }).toList();
        var applicationDtos = savedApplications.stream()
                .filter(app -> app != null)
                .map(applicationMapper::toDto)
                .toList();
        List<URI> uris = savedApplications.stream()
                .filter(app -> app != null)
                .map(app -> {
                    System.out.println("Building URI for application ID: " + app.getId());
                    return uriComponentsBuilder.cloneBuilder()
                            .path("/applications/{id}")
                            .buildAndExpand(app.getId()).toUri();
                })
                .toList();
        var responseDto = new BatchQuickApplySuccessDto(applicationDtos, uris);
        userProfileService.updateQuickApplyTimestamp(userId);
        return ResponseEntity.ok(responseDto);
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

    @PatchMapping("/{id}/shortList")
    @Operation(summary = "Shortlist a candidate application")
    public ResponseEntity<?> shortListCandidate(
            @PathVariable Long id) {
        var application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        application.setShortListed(true);
        applicationRepository.save(application);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unShortList")
    @Operation(summary = "Unshortlist a candidate application")
    public ResponseEntity<?> unShortListCandidate(
            @PathVariable Long id) {
        var application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        application.setShortListed(false);
        applicationRepository.save(application);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/updateStatus")
    @Operation(summary = "Update application status")
    public ResponseEntity<ApplicationDto> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status) {
        System.out.println("Updating application ID " + id + " to status " + status);
        var application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application with ID " + id + " not found"));
        // Ensure application status is valid
        application.setStatus(status);
        var savedApplication = applicationRepository.save(application);
        return ResponseEntity.ok(applicationMapper.toDto(savedApplication));
    }
}

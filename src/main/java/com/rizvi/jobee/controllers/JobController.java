package com.rizvi.jobee.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.rizvi.jobee.dtos.job.CheckMatchDto;
import com.rizvi.jobee.dtos.job.CreateJobDto;
import com.rizvi.jobee.dtos.job.JobDetailedSummaryForBusinessDto;
import com.rizvi.jobee.dtos.job.JobSummaryDto;
import com.rizvi.jobee.dtos.job.JobSummaryForBusinessDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.dtos.user.FindCandidateDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.BusinessType;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.BusinessNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AIJobDescriptionResponse;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.queries.JobQuery;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.AccountService;
import com.rizvi.jobee.services.CompanyService;
import com.rizvi.jobee.services.JobService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/jobs")
public class JobController {
    private final JobRepository jobRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;
    private final AccountService accountService;
    private final CompanyService companyService;
    private final JobService jobService;
    private final JobMapper jobMapper;

    @GetMapping
    @Operation(summary = "Get all jobs with optional filters and search")
    public ResponseEntity<PaginatedResponse<JobSummaryDto>> getJobs(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute JobQuery jobQuery) {
        var paginatedJobData = jobService.getAllJobs(jobQuery, pageNumber, pageSize);
        var jobs = paginatedJobData.getContent();
        var hasMore = paginatedJobData.isHasMore();
        var jobDtos = jobs.stream().map(jobMapper::toSummaryDto).toList();
        PaginatedResponse<JobSummaryDto> response = new PaginatedResponse<JobSummaryDto>(hasMore, jobDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/businesses")
    @Operation(summary = "Get all jobs for a business with optional filters and search")
    public ResponseEntity<PaginatedResponse<JobSummaryForBusinessDto>> getJobsForBusiness(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute JobQuery jobQuery,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var accountType = principal.getAccountType();
        // TODO: Add company id to the principal to avoid extra DB call
        var businessAccount = accountService.getBusinessAccountById(userId);
        Long companyId = businessAccount.getCompany().getId();
        jobQuery.setCompanyId(companyId);
        if (accountType.equals(BusinessType.RECRUITER.name())) {
            jobQuery.setPostedByAccountId(userId);

        } else if (accountType.equals(BusinessType.EMPLOYEE.name())) {
            jobQuery.setHiringTeamMemberAccountId(userId);
        }
        var paginatedJobData = jobService.getJobsByCompany(jobQuery, pageNumber, pageSize);
        var jobs = paginatedJobData.getContent();
        var hasMore = paginatedJobData.isHasMore();
        var totalElements = paginatedJobData.getTotalElements();
        var jobDtos = jobs.stream().map(jobMapper::toSummaryForBusinessDto).toList();
        PaginatedResponse<JobSummaryForBusinessDto> response = new PaginatedResponse<JobSummaryForBusinessDto>(
                hasMore, jobDtos, totalElements);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID")
    public ResponseEntity<JobSummaryDto> getJobById(@PathVariable("id") Long id) {
        var job = jobService.getJobById(id);
        return ResponseEntity.ok(jobMapper.toSummaryDto(job));
    }

    @GetMapping("/{id}/ai-insights")
    @Operation(summary = "Get AI-generated insights for a specific job")
    public ResponseEntity<List<String>> getAIJobInsight(@PathVariable Long id) {
        System.out.println("Generating AI insights for job ID: " + id);
        var job = jobService.getJobById(id);
        var companyId = job.getCompanyId();
        var company = companyService.findCompanyById(companyId);
        var aiJobInsight = jobService.generateAIJobInsight(job, company);
        return ResponseEntity.ok(aiJobInsight.getAiAnalysis());
    }

    @GetMapping("/applied")
    @Operation(summary = "Get all applied jobs for the authenticated user")
    public ResponseEntity<List<Long>> getAppliedJobs(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var userProfile = userProfileRepository.findByAccountId(userId)
                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
        List<Long> appliedJobs = userProfile.getApplications().stream().map((app) -> app.getJob().getId()).toList();
        List<Job> jobs = jobRepository.findJobWithIdList(appliedJobs);
        List<Long> jobDtos = jobs.stream()
                .map(job -> job.getId())
                .toList();
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/companies/{companyId}/job-count")
    @Operation(summary = "Get total job count for a specific company")
    public ResponseEntity<Integer> getJobCountByCompany(
            @PathVariable Long companyId) {
        var jobCount = jobService.getJobsByCompanyId(companyId).size();
        return ResponseEntity.ok(jobCount);
    }

    @GetMapping("/companies/{companyId}/recent-jobs")
    @Operation(summary = "Get most recent jobs for a specific company")
    public ResponseEntity<List<JobSummaryDto>> getMostRecentJobsByCompany(
            @RequestParam(required = false, defaultValue = "3") Long limit,
            @PathVariable Long companyId) {
        var jobs = jobService.getMostRecentJobsByCompany(companyId, limit);
        var jobDtos = jobs.stream().map(jobMapper::toSummaryDto).toList();
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/companies/{companyId}/jobs")
    @Operation(summary = "Get all jobs for a specific company with optional filters and search")
    public ResponseEntity<PaginatedResponse<JobSummaryForBusinessDto>> getJobsByCompany(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute JobQuery jobQuery,
            @PathVariable Long companyId,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var accountType = principal.getAccountType();
        jobQuery.setCompanyId(companyId);
        if (accountType.equals(BusinessType.RECRUITER.name())) {
            jobQuery.setPostedByAccountId(userId);

        } else if (accountType.equals(BusinessType.EMPLOYEE.name())) {
            jobQuery.setHiringTeamMemberAccountId(userId);
        }
        var paginatedJobData = jobService.getJobsByCompany(jobQuery, pageNumber, pageSize);
        var jobs = paginatedJobData.getContent();
        var hasMore = paginatedJobData.isHasMore();
        var totalElements = paginatedJobData.getTotalElements();
        var jobDtos = jobs.stream().map(jobMapper::toSummaryForBusinessDto).toList();
        PaginatedResponse<JobSummaryForBusinessDto> response = new PaginatedResponse<JobSummaryForBusinessDto>(
                hasMore, jobDtos, totalElements);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/companies/jobs/{jobId}")
    @Operation(summary = "Get detailed job information for a specific job of a company")
    public ResponseEntity<JobDetailedSummaryForBusinessDto> getDetailedJobForBusiness(
            @PathVariable Long jobId) {
        var job = jobService.getCompanyJobById(jobId);
        var jobDto = jobMapper.toDetailedSummaryForBusinessDto(job);
        System.out.println(jobDto.getHiringTeam().size() + " hiring team members added to job DTO");
        return ResponseEntity.ok(jobDto);
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get all favorite jobs for the authenticated user")
    public ResponseEntity<List<JobSummaryDto>> getFavoriteJobs(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var userProfile = userProfileService.getAuthenticatedUserProfile(userId);
        List<Long> favoriteJobs = userProfile.getFavoriteJobs().stream().map((job) -> job.getId()).toList();
        List<Job> jobs = jobService.getJobsByIds(favoriteJobs);
        List<JobSummaryDto> jobDtos = jobs.stream()
                .map(jobMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/businessUser")
    @Operation(summary = "Get jobs posted by the authenticated business user")
    public ResponseEntity<List<JobSummaryForBusinessDto>> getJobsForAuthenticatedBusinessUser(
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var accountId = principal.getId();
        var accountType = principal.getAccountType();
        List<Job> jobs = new ArrayList<>();
        if (accountType.equals(BusinessType.RECRUITER.name())) {
            jobs = jobService.getJobsByBusinessAccountIdForRecruiter(accountId, search);
        } else if (accountType.equals(BusinessType.EMPLOYEE.name())) {
            jobs = jobService.getJobsByBusinessAccountIdForEmployee(accountId, search);
        } else if (accountType.equals(BusinessType.ADMIN.name())) {
            var account = accountService.getBusinessAccountById(accountId);
            Long companyId = account.getCompany().getId();
            jobs = jobService.getJobsByCompanyId(companyId);
        }

        var jobDtos = jobs.stream().map(jobMapper::toSummaryForBusinessDto).toList();
        return ResponseEntity.ok(jobDtos);
    }

    @PostMapping
    @Operation(summary = "Create a new job posting")
    public ResponseEntity<JobSummaryDto> createJob(
            @RequestBody CreateJobDto request,
            @AuthenticationPrincipal CustomPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var accountId = principal.getId();
        var businessAccount = accountService.getBusinessAccountById(accountId);
        if (businessAccount == null) {
            throw new BusinessNotFoundException();
        }
        System.out.println("SYED-DEBUG: HIRING TEAM " + request.getHiringTeam());
        var savedJob = jobService.createJob(request, businessAccount);
        var uri = uriComponentsBuilder.path("/jobs/{id}").buildAndExpand(savedJob.getId()).toUri();
        return ResponseEntity.created(uri).body(jobMapper.toSummaryDto(savedJob));

    }

    @GetMapping("/{id}/shortlisted")
    @Operation(summary = "Get all shortlisted candidates for a job")
    public ResponseEntity<List<Long>> getShortListedApplicants(
            @PathVariable Long id) {
        var job = jobService.getJobById(id);
        var shortListedApplicants = job.getShortListedApplications().stream().map(Application::getId).toList();
        System.out.println("Found " + shortListedApplicants.size() + " shortlisted applicants for job ID: " + id);
        System.out.println(shortListedApplicants);
        return ResponseEntity.ok(shortListedApplicants);
    }

    @PatchMapping("/{id}/views")
    @Operation(summary = "Increment the job view count")
    public ResponseEntity<Void> incrementJobViews(@PathVariable Long id) {
        jobService.incrementJobViews(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/{id}/check-match")
    @Operation(summary = "Check if the authenticated user's profile matches the job requirements")
    public ResponseEntity<CheckMatchDto> checkJobMatch(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var userProfile = userProfileService.getAuthenticatedUserProfile(userId);
        var matchResult = jobService.checkJobMatch(id, userProfile);
        CheckMatchDto dto = new CheckMatchDto();
        dto.setMatch(matchResult);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/find-candidates")
    @Operation(summary = "Find matching candidates for a specific job")
    public ResponseEntity<List<FindCandidateDto>> findMatchingCandidatesForJob(
            @PathVariable Long id) {
        Map<UserProfile, Integer> result = jobService.findCandidatesForJob(id);
        List<FindCandidateDto> candidateDtos = result.entrySet().stream()
                .map(entry -> {
                    UserProfile userProfile = entry.getKey();
                    Integer score = entry.getValue();
                    FindCandidateDto dto = new FindCandidateDto();
                    dto.setId(userProfile.getAccount().getId());
                    dto.setFullName(userProfile.getFullName());
                    dto.setLocation(userProfile.getLocation());
                    dto.setProfileImageUrl(userProfile.getProfileImageUrl());
                    dto.setTitle(userProfile.getTitle());
                    dto.setMatchScore(score);
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()))
                .toList();
        return ResponseEntity.ok(candidateDtos);
    }

    @PostMapping("/generate-ai-description")
    @Operation(summary = "Generate AI-based job description")
    public ResponseEntity<AIJobDescriptionResponse> generateAIJobDescription(
            @RequestBody CreateJobDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var accountId = principal.getId();
        var businessAccount = accountService.getBusinessAccountById(accountId);
        if (businessAccount == null) {
            throw new BusinessNotFoundException();
        }
        String aiJobDescription = jobService.generateAIJobDescription(request, businessAccount.getCompany());
        return ResponseEntity.ok(new AIJobDescriptionResponse(aiJobDescription));
    }
}

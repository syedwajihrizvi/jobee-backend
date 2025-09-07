package com.rizvi.jobee.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.job.CreateJobDto;
import com.rizvi.jobee.dtos.job.JobDetailedSummaryForBusinessDto;
import com.rizvi.jobee.dtos.job.JobSummaryDto;
import com.rizvi.jobee.dtos.job.JobSummaryForBusinessDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.Tag;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.BusinessNotFoundException;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.queries.JobQuery;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.TagRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.specifications.JobSpecifications;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/jobs")
public class JobController {
    private final JobRepository jobRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final TagRepository tagRepository;
    private final JobMapper jobMapper;

    @GetMapping
    public ResponseEntity<List<JobSummaryDto>> getJobs(
            @ModelAttribute JobQuery jobQuery,
            @RequestParam(required = false) String search) {
        List<Job> jobs = new ArrayList<>();
        jobs = jobRepository.findAll(JobSpecifications.withFilters(jobQuery));
        var jobDtos = jobs.stream()
                .map(jobMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/applied")
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

    @GetMapping("/companies/{companyId}/jobs")
    public ResponseEntity<List<JobSummaryForBusinessDto>> getJobsByCompany(
            @ModelAttribute JobQuery jobQuery,
            @PathVariable Long companyId) {
        jobQuery.setCompanyId(companyId);
        var jobs = jobRepository.findAll(JobSpecifications.withFilters(jobQuery));
        var jobDto = jobs.stream().map(jobMapper::toSummaryForBusinessDto).toList();
        return ResponseEntity.ok(jobDto);
    }

    @GetMapping("/companies/{companyId}/jobs/{jobId}")
    public ResponseEntity<JobDetailedSummaryForBusinessDto> getDetailedJobForBusiness(
            @PathVariable Long companyId, @PathVariable Long jobId) {
        var job = jobRepository.findDetailedJobById(jobId).orElseThrow(
                () -> new JobNotFoundException("Job not found"));

        return ResponseEntity.ok(jobMapper.toDetailedSummaryForBusinessDto(job));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<JobSummaryDto>> getFavoriteJobs(
            @RequestParam String userId) {
        var userProfile = userProfileRepository.findByAccountId(Long.valueOf(userId))
                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
        List<Long> favoriteJobs = userProfile.getFavoriteJobs().stream().map((job) -> job.getId()).toList();
        List<Job> jobs = jobRepository.findJobWithIdList(favoriteJobs);
        List<JobSummaryDto> jobDtos = jobs.stream()
                .map(jobMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobSummaryDto> getJobById(@PathVariable("id") Long id) {
        var job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job not found with ID: " + id);
        }
        return ResponseEntity.ok(jobMapper.toSummaryDto(job));
    }

    // TODO: Business account ID should come from CustomPrincipal
    @Transactional
    @PostMapping
    public ResponseEntity<?> createJob(
            @RequestBody CreateJobDto request,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var accountId = request.getBusinessAccountId();
        var tags = request.getTags();
        var tagEntities = new ArrayList<Tag>();
        for (String tagName : tags) {
            var tag = tagRepository.findByName(tagName.trim().replaceAll("[^a-zA-Z0-9 ]", ""));
            if (tag == null) {
                tag = Tag.builder().name(tagName).build();
                tag = tagRepository.save(tag);
            }
            tagEntities.add(tag);
        }
        var businessAccount = businessAccountRepository.findById(accountId).orElse(null);
        if (businessAccount == null) {
            throw new BusinessNotFoundException();
        }
        var job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .employmentType(request.getEmploymentType())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .experience(request.getExperience())
                .build();
        job.setBusinessAccount(businessAccount);
        for (Tag tag : tagEntities) {
            job.addTag(tag);
        }
        var savedJob = jobRepository.save(job);
        var uri = uriComponentsBuilder.path("/jobs/{id}").buildAndExpand(savedJob.getId()).toUri();
        return ResponseEntity.created(uri).body(jobMapper.toSummaryDto(savedJob));

    }

    @GetMapping("/{id}/shortlisted")
    @Operation(summary = "Get all shortlisted candidates for a job")
    public ResponseEntity<List<Long>> getShortListedApplicants(
            @PathVariable Long id) {
        System.out.println("Fetching shortlisted applicants for job ID: " + id);
        var job = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException("Job not found"));
        var shortListedApplicants = job.getShortListedApplications().stream().map(Application::getId).toList();
        return ResponseEntity.ok(shortListedApplicants);
    }

}

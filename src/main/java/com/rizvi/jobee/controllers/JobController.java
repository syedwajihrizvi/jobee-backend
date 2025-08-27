package com.rizvi.jobee.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateJobDto;
import com.rizvi.jobee.dtos.JobSummaryDto;
import com.rizvi.jobee.dtos.JobSummaryForBusinessDto;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.BusinessNotFoundException;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.queries.JobQuery;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.specifications.JobSpecifications;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/jobs")
public class JobController {
    private final JobRepository jobRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final UserProfileRepository userProfileRepository;
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
    public ResponseEntity<List<JobSummaryDto>> getAppliedJobs(
            @RequestParam String userId) {
        var userProfile = userProfileRepository.findByAccountId(Long.valueOf(userId))
                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
        List<Long> appliedJobs = userProfile.getApplications().stream().map((app) -> app.getJob().getId()).toList();
        List<Job> jobs = jobRepository.findJobWithIdList(appliedJobs);
        List<JobSummaryDto> jobDtos = jobs.stream()
                .map(jobMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/companies/{companyId}")
    public ResponseEntity<List<JobSummaryForBusinessDto>> getJobsByCompany(@PathVariable Long companyId) {
        var jobs = jobRepository.findByCompanyId(companyId);
        var jobDto = jobs.stream().map(jobMapper::toSummaryForBusinessDto).toList();
        return ResponseEntity.ok(jobDto);
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

    @PostMapping
    public ResponseEntity<?> createJob(
            @RequestBody CreateJobDto request,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var accountId = request.getBusinessAccountId();

        var businessAccount = businessAccountRepository.findById(accountId).orElse(null);
        if (businessAccount == null) {
            throw new BusinessNotFoundException();
        }
        var job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        job.setBusinessAccount(businessAccount);
        var savedJob = jobRepository.save(job);
        var uri = uriComponentsBuilder.path("/jobs/{id}").buildAndExpand(savedJob.getId()).toUri();
        return ResponseEntity.created(uri).body(jobMapper.toSummaryDto(savedJob));

    }

}

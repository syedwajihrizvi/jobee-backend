package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateJobDto;
import com.rizvi.jobee.dtos.JobSummaryDto;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.exceptions.BusinessNotFoundException;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.JobRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/jobs")
public class JobController {
    private final JobRepository jobRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final JobMapper jobMapper;

    @GetMapping
    public ResponseEntity<List<JobSummaryDto>> getJobs() {
        var jobs = jobRepository.findAll();
        var jobDtos = jobs.stream()
                .map(jobMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(jobDtos);
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

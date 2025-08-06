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

import com.rizvi.jobee.dtos.ApplicationDto;
import com.rizvi.jobee.dtos.CreateApplicationDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserAccountRepository;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserAccountRepository userAccountRepository;
    private final ApplicationMapper applicationMapper;

    @GetMapping()
    public ResponseEntity<List<ApplicationDto>> getAllApplications() {
        var applications = applicationRepository.findAll();
        var applicationDtos = applications.stream().map(applicationMapper::toDto).toList();
        ;
        return ResponseEntity.ok(applicationDtos);
    }

    @PostMapping
    public ResponseEntity<ApplicationDto> createApplication(
            @Valid @RequestBody CreateApplicationDto createApplicationDto,
            @AuthenticationPrincipal CustomPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var jobId = createApplicationDto.getJobId();
        var job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job with ID " + jobId + " not found");
        }
        var userId = principal.getId();
        var userAccount = userAccountRepository.findById(userId).orElse(null);
        if (userAccount == null) {
            throw new AccountNotFoundException("User account with ID " + userId + " not found");
        }
        var application = new Application();
        application.setJob(job);
        application.setUserAccount(userAccount);
        var savedApplication = applicationRepository.save(application);
        var uri = uriComponentsBuilder.path("/applications/{id}")
                .buildAndExpand(savedApplication.getId()).toUri();
        return ResponseEntity.created(uri).body(applicationMapper.toDto(savedApplication));
    }
}

package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.application.ApplicationDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.exceptions.ApplicationNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.repositories.ApplicationRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + id));
    }

    public ApplicationDto getApplicationByJobAndCandidate(Long jobId, Long candidateId) {
        var application = applicationRepository.findByJobIdAndUserProfileId(jobId, candidateId);
        if (application == null) {
            throw new ApplicationNotFoundException(
                    "Application not found for job ID: " + jobId + " and candidate ID: " + candidateId);
        }
        return applicationMapper.toDto(application);
    }

    public Application updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        var application = findById(applicationId);
        application.setStatus(newStatus);
        return applicationRepository.save(application);
    }
}
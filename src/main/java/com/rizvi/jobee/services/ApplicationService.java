package com.rizvi.jobee.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.application.ApplicationDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.exceptions.ApplicationNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.queries.ApplicationQuery;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.specifications.ApplicantSpecification;

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

    public PaginatedResponse<Application> getAllApplications(ApplicationQuery query, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, 
            Sort.by("createdAt").descending().and(Sort.by("id").ascending()));
        Specification<Application> specification = ApplicantSpecification.withFilters(query);
        Page<Application> page = applicationRepository.findAll(specification, pageRequest);
        var applications = page.getContent();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        var totalApplications = page.getTotalElements();
        System.out.println("Total applications found: " + totalApplications);
        return new PaginatedResponse<Application>(hasMore, applications, totalApplications);
    }
}
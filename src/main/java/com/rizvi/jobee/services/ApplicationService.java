package com.rizvi.jobee.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.application.ApplicationDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.UnofficalJobOffer;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.exceptions.ApplicationNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.queries.ApplicationQuery;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.specifications.ApplicantSpecification;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final UserNotificationService userNotificationService;

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
        if (application == null) {
            throw new ApplicationNotFoundException("Application not found with id: " + applicationId);
        }
        application.setStatus(newStatus);
        if (newStatus == ApplicationStatus.REJECTED) {
            userNotificationService.createApplicationRejectionsNotificationAndSend(application);
        }
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
        return new PaginatedResponse<Application>(hasMore, applications, totalApplications);
    }

    public Application getMostRecentApplicationForUser(Long userId) {
        var application = applicationRepository.findMostRecentApplicationForUser(userId).orElse(null);
        return application;
    }

    public Application addUnofficalJobOfferToApplication(Application application, String offerDetails) {
        var unofficalJobOffer = UnofficalJobOffer.builder()
                .offerDetails(offerDetails)
                .application(application)
                .build();
        application.setJobOffer(unofficalJobOffer);
        application.setStatus(ApplicationStatus.OFFER_MADE);
        return applicationRepository.save(application);
    }

    public UnofficalJobOffer getUnofficalJobOfferForApplication(Long applicationId) {
        var application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            throw new ApplicationNotFoundException("Application not found with id: " + applicationId);
        }
        return application.getJobOffer();
    }

    @Transactional
    public UnofficalJobOffer updateUnofficalJobOfferForApplication(Long applicationId, Boolean accepted) {
        var application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            throw new ApplicationNotFoundException("Application not found with id: " + applicationId);
        }
        application.updateJobOfferStatus(accepted);
        return applicationRepository.save(application).getJobOffer();
    }
}
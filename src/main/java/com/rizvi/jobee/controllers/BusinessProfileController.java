package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.dtos.user.BusinessProfileDashboardSummaryDto;
import com.rizvi.jobee.dtos.user.BusinessProfileSummaryForInterviewDto;
import com.rizvi.jobee.dtos.user.UpdateBusinessProfileGeneralInfoDto;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.mappers.BusinessMapper;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.BusinessProfileService;
import com.rizvi.jobee.services.CompanyService;
import com.rizvi.jobee.services.JobService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/business-profiles")
public class BusinessProfileController {
    private final CompanyService companyService;
    private final JobService jobService;
    private final BusinessProfileService businessProfileService;
    private final BusinessMapper businessMapper;
    private final JobMapper jobMapper;

    @GetMapping()
    public ResponseEntity<BusinessProfileSummaryForInterviewDto> getBusinessProfileSummaryForInterview(
            @RequestParam String email) {
        var businessProfile = businessProfileService.getBusinessProfileByEmail(email);
        if (businessProfile == null) {
            return ResponseEntity.notFound().build();
        }
        var dto = businessMapper.toBusinessProfileSummaryForInterviewDto(businessProfile);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get business profile for dashboard for the authenticated user")
    public ResponseEntity<BusinessProfileDashboardSummaryDto> getBusinessProfileForDashboard(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var companyId = businessProfileService.getCompanyIdForBusinessProfileId(userId);
        var jobs = jobService.getJobsByCompanyId(companyId);
        var totalJobs = jobs.size();
        var totalApplications = jobs.stream().mapToInt(job -> job.getApplications().size()).sum();
        var totalViews = jobs.stream().mapToInt(job -> job.getViews()).sum();
        var totalInterviews = jobs.stream().mapToInt(job -> job.getInterviews().size()).sum();
        Job lastJobPosted = null;
        if (totalJobs > 0) {
            lastJobPosted = jobs.get(0);
        }
        var mostAppliedJobs = jobs.stream()
                .sorted((j1, j2) -> Integer.compare(j1.getApplications().size(), j2.getApplications().size()))
                .limit(3).map(jobMapper::toDetailedSummaryForBusinessDto).toList().reversed();
        var mostViewedJobs = jobs.stream().sorted((j1, j2) -> Integer.compare(j1.getViews(), j2.getViews()))
                .limit(3).map(jobMapper::toDetailedSummaryForBusinessDto).toList().reversed();

        var response = BusinessProfileDashboardSummaryDto.builder()
                .totalJobsPosted(totalJobs)
                .totalApplicationsReceived(totalApplications)
                .totalJobViews(totalViews)
                .totalInterviewsScheduled(totalInterviews)
                .lastJobPosted(lastJobPosted != null ? jobMapper.toSummaryDto(lastJobPosted) : null)
                .mostAppliedJobs(mostAppliedJobs)
                .mostViewedJobs(mostViewedJobs)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-profile-image/me")
    @Operation(summary = "Update business profile image")
    public ResponseEntity<BusinessProfileSummaryForInterviewDto> updateBusinessProfileImage(
            @RequestParam("profileImage") MultipartFile profileImage,
            @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
        if (profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile image file is empty");
        }
        var userId = principal.getId();
        var savedProfile = businessProfileService.updateProfileImage(profileImage, userId);
        return ResponseEntity.ok().body(businessMapper.toBusinessProfileSummaryForInterviewDto(savedProfile));
    }

    @PatchMapping("/update-general-info/me")
    @Operation(summary = "Update business general info")
    public ResponseEntity<BusinessProfileSummaryForInterviewDto> updateBusinessProfileGeneralInfo(
            @RequestBody UpdateBusinessProfileGeneralInfoDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var businessProfile = businessProfileService.updateBusinessProfileGeneralInfo(request, userId);
        var dto = businessMapper.toBusinessProfileSummaryForInterviewDto(businessProfile);
        return ResponseEntity.ok(dto);
    }
}

package com.rizvi.jobee.dtos.application;

import java.time.LocalDateTime;

import com.rizvi.jobee.dtos.user.UserProfileSummaryForBusinessDto;
import com.rizvi.jobee.enums.ApplicationStatus;

import lombok.Data;

@Data
public class ApplicationDetailsForBusinessDto {
    private Long id;
    private LocalDateTime appliedAt;
    private String resumeUrl;
    private String coverLetterUrl;
    private Long jobId;
    private ApplicationStatus status;
    private Boolean shortListed;
    private UserProfileSummaryForBusinessDto userProfile;
}

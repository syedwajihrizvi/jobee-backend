package com.rizvi.jobee.dtos.application;

import java.time.LocalDateTime;
import java.util.List;

import com.rizvi.jobee.dtos.user.UserDocumentDto;
import com.rizvi.jobee.dtos.user.UserProfileSummaryForBusinessDto;
import com.rizvi.jobee.enums.ApplicationStatus;

import lombok.Data;

@Data
public class ApplicationDetailsForBusinessDto {
    private Long id;
    private LocalDateTime appliedAt;
    private String resumeUrl;
    private String coverLetterUrl;
    private UserDocumentDto resumeDocument;
    private UserDocumentDto coverLetterDocument;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private ApplicationStatus status;
    private List<Long> interviewIds;
    private List<UserDocumentDto> userDocuments;
    private Boolean shortListed;
    private UserProfileSummaryForBusinessDto userProfile;
}

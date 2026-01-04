package com.rizvi.jobee.dtos.notification;

import lombok.Data;

@Data
public class NotificationContext {
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private String candidateProfileImageUrl;
    private Long jobId;
    private String jobTitle;
    private Long applicationId;
    private Long interviewId;
    private String fullName;
    private String userSummary;
}

package com.rizvi.jobee.dtos.notification;

import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.NotificationType;

import lombok.Data;

@Data
public class CreateNotificationDto {
    private Long recipientId;
    private MessagerUserType recipientType;
    private String title;
    private String message;
    private NotificationType notificationType;
    private Long companyId;
    private Long jobId;
    private String jobTitle;
    private Long applicationId;
    private Long interviewId;
    private String candidateProfileImageUrl;
    private String fullName;
    private String userSummary;
}

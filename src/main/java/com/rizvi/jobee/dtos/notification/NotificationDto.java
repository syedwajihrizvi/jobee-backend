package com.rizvi.jobee.dtos.notification;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificationDto {
    private Long id;
    private String message;
    private Long recipientId;
    private String recipientType;
    private String notificationType;
    private LocalDateTime timestamp;
    private String companyName;
    private String companyLogoUrl;
    private Long applicationId;
    private String jobId;
    private boolean read;

}

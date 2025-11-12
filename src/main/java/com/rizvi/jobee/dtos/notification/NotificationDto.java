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
    private NotificationContext context;
    private boolean read;

}

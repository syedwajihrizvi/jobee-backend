package com.rizvi.jobee.dtos.message;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageDto {
    private Long id;
    private String text;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private String messageType;
    private Long fileSize;
    private Long conversationId;
    private LocalDateTime timestamp;
    private Boolean sentByUser;
}

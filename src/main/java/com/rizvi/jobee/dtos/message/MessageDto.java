package com.rizvi.jobee.dtos.message;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageDto {
    private Long id;
    private String text;
    private Long conversationId;
    private LocalDateTime timestamp;
    private Boolean sentByUser;
}

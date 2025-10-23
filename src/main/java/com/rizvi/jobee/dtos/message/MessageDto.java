package com.rizvi.jobee.dtos.message;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageDto {
    private Long id;
    private String content;
    private LocalDateTime dateReceived;
    private String from;
    private String to;
    private String senderProfileImageUrl;
    private String receiverProfileImageUrl;
    private Boolean read;
}

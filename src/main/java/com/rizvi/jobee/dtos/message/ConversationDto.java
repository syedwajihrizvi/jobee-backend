package com.rizvi.jobee.dtos.message;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConversationDto {
    private Long id;
    private Boolean lastMessageRead;
    private String lastMessageContent;
    private String participantName;
    private String participantProfileImageUrl;
    private Long participantId;
    private LocalDateTime lastMessageTimestamp;
    private Boolean wasLastMessageSender;
}

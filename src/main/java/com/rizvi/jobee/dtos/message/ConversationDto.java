package com.rizvi.jobee.dtos.message;

import java.time.LocalDateTime;

import com.rizvi.jobee.enums.MessageType;

import lombok.Data;

@Data
public class ConversationDto {
    private Long id;
    private Boolean lastMessageRead;
    private MessageType lastMessageType;
    private String lastMessageContent;
    private String participantName;
    private String participantProfileImageUrl;
    private String participantRole;
    private Long participantId;
    private LocalDateTime lastMessageTimestamp;
    private Boolean wasLastMessageSender;
}

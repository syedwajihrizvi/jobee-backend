package com.rizvi.jobee.dtos.message;

import lombok.Data;

@Data
public class ConversationMessageRequestDto {
    private Long conversationId;
    private Long otherPartyId;
    private String otherPartyRole;
}

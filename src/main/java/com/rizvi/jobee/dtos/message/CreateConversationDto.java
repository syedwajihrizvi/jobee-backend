package com.rizvi.jobee.dtos.message;

import lombok.Data;

@Data
public class CreateConversationDto {
    private Long otherPartyId;
    private String otherPartyRole;
}

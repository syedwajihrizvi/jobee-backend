package com.rizvi.jobee.dtos.message;

import lombok.Data;

@Data
public class SendMessageAttachmentDocumentEmailRequestDto {
    private String fileUrl;
    private String otherPartyName;
    private String formatType;
}

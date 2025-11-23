package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class UpdateUserDocumentRequestDto {
    private String title;
    private String documentType;
}

package com.rizvi.jobee.dtos.user;

import java.time.LocalDateTime;

import com.rizvi.jobee.enums.UserDocumentType;

import lombok.Data;

@Data
public class UserDocumentDto {
    private Integer id;
    private UserDocumentType documentType;
    private String documentUrl;
    private String filename;
    private LocalDateTime createdAt;
}

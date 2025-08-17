package com.rizvi.jobee.dtos;

import java.util.List;

import lombok.Data;

@Data
public class UserProfileSummaryDto {
    private String firstName;
    private String lastName;
    private Integer age;
    private String summary;
    private String title;
    private List<UserDocumentDto> documents;
    private UserAccountSummaryDto account;
    private Boolean profileComplete;
}

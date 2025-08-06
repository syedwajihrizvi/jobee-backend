package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class UserProfileSummaryDto {
    private String firstName;
    private String lastName;
    private Integer age;
    private UserAccountSummaryDto account;
}

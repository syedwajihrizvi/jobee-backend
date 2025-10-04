package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class BusinessProfileSummaryForInterviewDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String title;
    private String summary;
}

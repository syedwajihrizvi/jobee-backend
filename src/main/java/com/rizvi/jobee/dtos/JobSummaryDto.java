package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class JobSummaryDto {
    private Long id;
    private String title;
    private String description;
    private Long businessAccountId;
    private String businessName;
}

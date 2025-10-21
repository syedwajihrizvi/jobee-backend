package com.rizvi.jobee.dtos.job;

import lombok.Data;

@Data
public class RecommendedJobDto {
    private JobSummaryDto job;
    private Long match;
}

package com.rizvi.jobee.queries;

import com.rizvi.jobee.enums.InterviewDecisionResult;
import com.rizvi.jobee.enums.InterviewStatus;

import lombok.Data;

@Data
public class InterviewQuery {
    private Long jobId;
    private Long companyId;
    private Long postedById;
    private Long conductorId;
    private String search;
    private Integer daysAgo;
    private InterviewStatus interviewStatus;
    private InterviewDecisionResult decisionResult;
}

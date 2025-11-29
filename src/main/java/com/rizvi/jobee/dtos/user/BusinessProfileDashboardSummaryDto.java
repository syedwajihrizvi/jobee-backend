package com.rizvi.jobee.dtos.user;

import java.util.List;

import com.rizvi.jobee.dtos.interview.InterviewSummaryDto;
import com.rizvi.jobee.dtos.job.JobDetailedSummaryForBusinessDto;
import com.rizvi.jobee.dtos.job.JobSummaryDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessProfileDashboardSummaryDto {
    Integer totalJobsPosted;
    Integer totalApplicationsReceived;
    Integer totalInterviews;
    Integer totalJobViews;
    Integer totalOffersMade;
    Integer totalHires;
    JobSummaryDto lastJobPosted;
    List<JobDetailedSummaryForBusinessDto> mostAppliedJobs;
    List<JobDetailedSummaryForBusinessDto> mostViewedJobs;
    List<InterviewSummaryDto> upcomingInterviews;

}

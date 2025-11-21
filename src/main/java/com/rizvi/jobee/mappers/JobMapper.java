package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.job.JobDetailedSummaryForBusinessDto;
import com.rizvi.jobee.dtos.job.JobIdDto;
import com.rizvi.jobee.dtos.job.JobSummaryDto;
import com.rizvi.jobee.dtos.job.JobSummaryForBusinessDto;
import com.rizvi.jobee.entities.Job;

@Mapper(componentModel = "spring", uses = { BusinessMapper.class })
public interface JobMapper {
    @Mapping(target = "businessName", source = "businessAccount.company.name")
    @Mapping(target = "businessAccountId", source = "businessAccount.id")
    @Mapping(target = "location", expression = "java(job.getJobLocation())")
    @Mapping(target = "companyId", source = "businessAccount.company.id")
    @Mapping(target = "applicationCount", expression = "java(job.getApplications().size())")
    @Mapping(target = "companyLogoUrl", source = "businessAccount.company.logo")
    @Mapping(target = "level", source = "level")
    @Mapping(target = "employmentType", source = "employmentType")
    @Mapping(target = "setting", source = "setting")
    @Mapping(target = "views", expression = "java(job.getViews())")
    @Mapping(target = "experience", ignore = true)
    JobSummaryDto toSummaryDto(Job job);

    @Mapping(target = "applicants", expression = "java(job.getApplications().size())")
    @Mapping(target = "pendingApplicationsSize", expression = "java(job.getPendingApplications().size())")
    @Mapping(target = "totalInterviews", expression = "java(job.getInterviews().size())")
    @Mapping(target = "businessName", source = "businessAccount.company.name")
    JobSummaryForBusinessDto toSummaryForBusinessDto(Job job);

    @Mapping(target = "applicants", expression = "java(job.getApplications().size())")
    @Mapping(target = "interviews", expression = "java(job.getInterviews().size())")
    @Mapping(target = "totalShortListedCandidates", expression = "java(job.getShortListedApplications().size())")
    @Mapping(target = "hiringTeam", source = "hiringTeamMembers")
    JobDetailedSummaryForBusinessDto toDetailedSummaryForBusinessDto(Job job);

    JobIdDto toJobIdDto(Job job);

}

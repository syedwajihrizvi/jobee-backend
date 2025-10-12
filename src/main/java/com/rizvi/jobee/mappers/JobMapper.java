package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.job.JobDetailedSummaryForBusinessDto;
import com.rizvi.jobee.dtos.job.JobIdDto;
import com.rizvi.jobee.dtos.job.JobSummaryDto;
import com.rizvi.jobee.dtos.job.JobSummaryForBusinessDto;
import com.rizvi.jobee.entities.Job;

@Mapper(componentModel = "spring", uses = { ApplicationMapper.class })
public interface JobMapper {
    @Mapping(target = "businessName", source = "businessAccount.company.name")
    @Mapping(target = "businessAccountId", source = "businessAccount.id")
    @Mapping(target = "companyId", source = "businessAccount.company.id")
    JobSummaryDto toSummaryDto(Job job);

    @Mapping(target = "applicants", expression = "java(job.getApplications().size())")
    @Mapping(target = "businessName", source = "businessAccount.company.name")
    JobSummaryForBusinessDto toSummaryForBusinessDto(Job job);

    @Mapping(target = "applicants", expression = "java(job.getApplications().size())")
    @Mapping(target = "interviews", expression = "java(job.getInterviews().size())")
    @Mapping(target = "totalShortListedCandidates", expression = "java(job.getShortListedApplications().size())")
    JobDetailedSummaryForBusinessDto toDetailedSummaryForBusinessDto(Job job);

    JobIdDto toJobIdDto(Job job);

}

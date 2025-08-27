package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.EntityGraph;

import com.rizvi.jobee.dtos.JobSummaryDto;
import com.rizvi.jobee.dtos.JobSummaryForBusinessDto;
import com.rizvi.jobee.entities.Job;

@Mapper(componentModel = "spring")
public interface JobMapper {
    @EntityGraph(attributePaths = "businessAccount.company")
    @Mapping(target = "businessName", source = "businessAccount.company.name")
    @Mapping(target = "businessAccountId", source = "businessAccount.id")
    JobSummaryDto toSummaryDto(Job job);

    @Mapping(target = "applicants", expression = "java(job.getApplications().size())")
    @Mapping(target = "businessName", source = "businessAccount.company.name")
    JobSummaryForBusinessDto toSummaryForBusinessDto(Job job);
}

package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.InterviewConductorDto;
import com.rizvi.jobee.dtos.InterviewDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;

@Mapper(componentModel = "spring")
public interface InterviewMapper {
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "interview_date", source = "interviewDate")
    @Mapping(target = "start_time", source = "startTime")
    @Mapping(target = "candidateEmail", source = "candidate.account.email")
    @Mapping(target = "candidateName", expression = "java(interview.getCandidate().getFullName())")
    InterviewDto toDto(Interview interview);

    @Mapping(target = "id", source = "businessAccount.id")
    @Mapping(target = "email", source = "businessAccount.email")
    @Mapping(target = "name", expression = "java(businessAccount.getFullName())")
    InterviewConductorDto toConductorDto(BusinessAccount businessAccount);
}

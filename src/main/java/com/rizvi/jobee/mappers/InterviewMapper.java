package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.InterviewDto;
import com.rizvi.jobee.entities.Interview;

@Mapper(componentModel = "spring")
public interface InterviewMapper {
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateEmail", source = "candidate.account.email")
    @Mapping(target = "candidateName", expression = "java(interview.getCandidate().getFullName())")
    @Mapping(target = "interviewerId", source = "interviewer.id")
    @Mapping(target = "interviewerEmail", source = "interviewer.email")
    InterviewDto toDto(Interview interview);
}

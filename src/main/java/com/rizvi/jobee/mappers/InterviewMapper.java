package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.interview.InterviewConductorDto;
import com.rizvi.jobee.dtos.interview.InterviewDto;
import com.rizvi.jobee.dtos.interview.InterviewPreparationDto;
import com.rizvi.jobee.dtos.interview.InterviewSummaryDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.InterviewPreparationQuestion;
import com.rizvi.jobee.helpers.AISchemas.InterviewPrepQuestion;

@Mapper(componentModel = "spring")
public interface InterviewMapper {
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateEmail", source = "candidate.account.email")
    @Mapping(target = "candidateName", expression = "java(interview.getCandidate().getFullName())")
    @Mapping(target = "companyName", source = "job.businessAccount.company.name")
    @Mapping(target = "preparationStatus", expression = "java(interview.getPreparationStatus())")
    @Mapping(target = "preparationTipsFromInterviewer", expression = "java(interview.getPreparationTipsAsList())")
    InterviewDto toDto(Interview interview);

    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.businessAccount.company.name")
    InterviewSummaryDto toSummaryDto(Interview interview);

    @Mapping(target = "id", source = "businessAccount.id")
    @Mapping(target = "email", source = "businessAccount.email")
    @Mapping(target = "name", expression = "java(businessAccount.getFullName())")
    InterviewConductorDto toConductorDto(BusinessAccount businessAccount);

    InterviewPrepQuestion toInterviewPrepQuestionDto(InterviewPreparationQuestion question);

    @Mapping(target = "strengths", expression = "java(interviewPreparation.getStrengthsAsList())")
    @Mapping(target = "weaknesses", expression = "java(interviewPreparation.getWeaknessesAsList())")
    @Mapping(target = "questions", source = "questions")
    @Mapping(target = "resources", source = "resources")
    @Mapping(target = "overallAdvice", source = "overallAdvice")
    @Mapping(target = "notesFromInterviewer", expression = "java(interviewPreparation.getNotesFromInterviewerAsList())")
    InterviewPreparationDto toPreparationDto(InterviewPreparation interviewPreparation);
}

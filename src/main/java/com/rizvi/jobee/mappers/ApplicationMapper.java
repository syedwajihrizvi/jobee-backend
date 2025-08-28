package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.ApplicantSummaryForBusinessDto;
import com.rizvi.jobee.dtos.ApplicationDto;
import com.rizvi.jobee.dtos.UserApplicationDto;
import com.rizvi.jobee.entities.Application;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.businessAccount.company.name")
    @Mapping(target = "userId", source = "userProfile.id")
    @Mapping(target = "userEmail", source = "userProfile.account.email")
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "status", source = "status")
    ApplicationDto toDto(Application application);

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "appliedAt", source = "createdAt")
    UserApplicationDto toUserApplicationDto(Application application);

    @Mapping(target = "email", source = "userProfile.account.email")
    @Mapping(target = "fullName", expression = "java(application.getUserProfile().getFirstName() + \" \" + application.getUserProfile().getLastName())")
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "phoneNumber", source = "userProfile.phoneNumber")
    @Mapping(target = "profileImageUrl", source = "userProfile.profileImageUrl")
    @Mapping(target = "profileSummary", source = "userProfile.summary")
    @Mapping(target = "title", source = "userProfile.title")
    ApplicantSummaryForBusinessDto toApplicantSummaryForBusinessDto(Application application);
}

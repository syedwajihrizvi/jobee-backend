package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.EntityGraph;

import com.rizvi.jobee.dtos.ApplicantSummaryForBusinessDto;
import com.rizvi.jobee.dtos.ApplicationDetailsForBusinessDto;
import com.rizvi.jobee.dtos.ApplicationDto;
import com.rizvi.jobee.dtos.UserApplicationDto;
import com.rizvi.jobee.entities.Application;

@Mapper(componentModel = "spring", uses = { UserProfileMapper.class })
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
    @Mapping(target = "location", expression = "java(application.getUserProfile().getCity() + \", \" + application.getUserProfile().getCountry())")
    ApplicantSummaryForBusinessDto toApplicantSummaryForBusinessDto(Application application);

    @EntityGraph(attributePaths = { "userProfile", "userProfile.account", "resumeDocument", "coverLetterDocument",
            "job" })
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "resumeUrl", source = "application.resumeDocument.documentUrl")
    @Mapping(target = "coverLetterUrl", source = "application.coverLetterDocument.documentUrl")
    @Mapping(target = "jobId", source = "job.id")
    ApplicationDetailsForBusinessDto toApplicationDetailsForBusinessDto(Application application);
}

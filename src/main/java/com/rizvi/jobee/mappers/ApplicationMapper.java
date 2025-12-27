package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.application.ApplicantSummaryForBusinessDto;
import com.rizvi.jobee.dtos.application.ApplicationDetailsForBusinessDto;
import com.rizvi.jobee.dtos.application.ApplicationDto;
import com.rizvi.jobee.dtos.application.ApplicationSummaryDto;
import com.rizvi.jobee.dtos.application.JobOfferDto;
import com.rizvi.jobee.dtos.user.UserApplicationDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.UnofficalJobOffer;

@Mapper(componentModel = "spring", uses = { UserProfileMapper.class, JobMapper.class, UserDocumentMapper.class })
public interface ApplicationMapper {
    @Mapping(target = "job", source = "job")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "userId", source = "userProfile.id")
    @Mapping(target = "userEmail", source = "userProfile.account.email")
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "status", source = "status")
    ApplicationDto toDto(Application application);

    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "jobId", source = "job.id")
    ApplicationSummaryDto toSummaryDto(Application application);

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "appliedAt", source = "createdAt")
    UserApplicationDto toUserApplicationDto(Application application);

    @Mapping(target = "email", source = "userProfile.account.email")
    @Mapping(target = "fullName", expression = "java(application.getUserProfile().getFirstName() + \" \" + application.getUserProfile().getLastName())")
    @Mapping(target = "firstName", source = "userProfile.firstName")
    @Mapping(target = "lastName", source = "userProfile.lastName")
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "phoneNumber", source = "userProfile.phoneNumber")
    @Mapping(target = "profileImageUrl", source = "userProfile.profileImageUrl")
    @Mapping(target = "profileSummary", source = "userProfile.summary")
    @Mapping(target = "title", source = "userProfile.title")
    @Mapping(target = "location", expression = "java(application.getUserProfile().getCity() + \", \" + application.getUserProfile().getCountry())")
    ApplicantSummaryForBusinessDto toApplicantSummaryForBusinessDto(Application application);

    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "resumeUrl", source = "application.resumeDocument.documentUrl")
    @Mapping(target = "coverLetterUrl", source = "application.coverLetterDocument.documentUrl")
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.businessAccount.company.name")
    @Mapping(target = "interviewIds", expression = "java(application.getInterviewIds())")
    @Mapping(target = "userDocuments", source = "userDocuments")
    ApplicationDetailsForBusinessDto toApplicationDetailsForBusinessDto(Application application);

    @Mapping(target = "offerMade", source = "jobOffer.createdAt")
    JobOfferDto toJobOfferDto(UnofficalJobOffer jobOffer);
}

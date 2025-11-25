package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.user.UserAccountSummaryDto;
import com.rizvi.jobee.dtos.user.UserProfileDashboardSummaryDto;
import com.rizvi.jobee.dtos.user.UserProfileSummaryDto;
import com.rizvi.jobee.entities.UserAccount;
import com.rizvi.jobee.entities.UserProfile;

@Mapper(componentModel = "spring", uses = {
                UserDocumentMapper.class,
                ApplicationMapper.class,
                SkillMapper.class,
                EducationMapper.class,
                ExperienceMapper.class,
                ProjectMapper.class,
                CompanyMapper.class
})
public interface UserMapper {
        UserAccountSummaryDto toSummaryDto(UserAccount userAccount);

        @Mapping(target = "profileComplete", expression = """
                        java(userProfile.getDocuments() != null &&
                        !userProfile.getDocuments().isEmpty())""")
        @Mapping(target = "email", source = "account.email")
        @Mapping(target = "location", expression = "java(userProfile.getLocation())")
        UserProfileSummaryDto toProfileSummaryDto(UserProfile userProfile);

        @Mapping(target = "fullName", expression = "java(userProfile.getFullName())")
        @Mapping(target = "totalApplications", expression = "java(userProfile.getTotalApplications())")
        @Mapping(target = "totalRejections", expression = "java(userProfile.getRejectedApplications())")
        @Mapping(target = "totalInConsideration", expression = "java(userProfile.getInConsiderationApplications())")
        @Mapping(target = "totalInterviews", expression = "java(userProfile.geTotalApplicationsInInterview())")
        UserProfileDashboardSummaryDto toDashboardSummaryDto(UserProfile userProfile);
}

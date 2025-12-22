package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.user.BusinessAccountDto;
import com.rizvi.jobee.dtos.user.BusinessProfileSummaryForInterviewDto;
import com.rizvi.jobee.dtos.user.CompanyMemberDto;
import com.rizvi.jobee.dtos.user.HiringTeamMemberResponseDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.BusinessProfile;
import com.rizvi.jobee.entities.HiringTeam;

@Mapper(componentModel = "spring", uses = {
        SocialMediaMapper.class
})
public interface BusinessMapper {
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "companyLogo", source = "company.logo")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "title", source = "profile.title")
    @Mapping(target = "profileImageUrl", source = "profile.profileImageUrl")
    @Mapping(target = "verified", source = "profile.verified")
    @Mapping(target = "location", source = "profile.location")
    @Mapping(target = "city", source = "profile.city")
    @Mapping(target = "state", source = "profile.state")
    @Mapping(target = "country", source = "profile.country")
    @Mapping(target = "socialMedias", source = "profile.socials")
    BusinessAccountDto toDto(BusinessAccount businessAccount);

    @Mapping(target = "id", source = "businessAccount.id")
    @Mapping(target = "firstName", source = "businessAccount.firstName")
    @Mapping(target = "lastName", source = "businessAccount.lastName")
    @Mapping(target = "email", source = "businessAccount.email")
    @Mapping(target = "userType", source = "businessAccount.accountType")
    @Mapping(target = "joinedDate", source = "businessAccount.createdAt")
    @Mapping(target = "profileImageUrl", source = "businessAccount.profile.profileImageUrl")
    CompanyMemberDto toCompanyMemberDto(BusinessAccount businessAccount);

    @Mapping(target = "email", source = "businessAccount.email")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "firstName", source = "businessAccount.firstName")
    @Mapping(target = "lastName", source = "businessAccount.lastName")
    BusinessProfileSummaryForInterviewDto toBusinessProfileSummaryForInterviewDto(BusinessProfile businessProfile);

    @Mapping(target = "email", expression = "java(resolveEmail(member))")
    @Mapping(target = "firstName", expression = "java(resolveFirstName(member))")
    @Mapping(target = "lastName", expression = "java(resolveLastName(member))")
    @Mapping(target = "profileImageUrl", expression = "java(resolveProfileImageUrl(member))")
    @Mapping(target = "verified", expression = "java(member.isVerified())")
    HiringTeamMemberResponseDto toHiringTeamMemberResponseDto(HiringTeam member);

    default String resolveEmail(HiringTeam member) {
        if (member.getBusinessAccount() != null) {
            return member.getBusinessAccount().getEmail();
        }
        return member.getEmail();
    }

    default String resolveFirstName(HiringTeam member) {
        if (member.getBusinessAccount() != null && member.getBusinessAccount().getFirstName() != null) {
            return member.getBusinessAccount().getFirstName();
        }
        return member.getFirstName();
    }

    default String resolveLastName(HiringTeam member) {
        if (member.getBusinessAccount() != null && member.getBusinessAccount().getLastName() != null) {
            return member.getBusinessAccount().getLastName();
        }
        return member.getLastName();
    }

    default String resolveProfileImageUrl(HiringTeam member) {
        if (member.getBusinessAccount() != null
                && member.getBusinessAccount().getProfile() != null
                && member.getBusinessAccount().getProfile().getProfileImageUrl() != null) {
            return member.getBusinessAccount().getProfile().getProfileImageUrl();
        }
        return null;
    }

}

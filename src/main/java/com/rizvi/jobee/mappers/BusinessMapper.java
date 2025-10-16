package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.user.BusinessAccountDto;
import com.rizvi.jobee.dtos.user.BusinessProfileSummaryForInterviewDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.BusinessProfile;

@Mapper(componentModel = "spring")
public interface BusinessMapper {
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "title", source = "profile.title")
    @Mapping(target = "profileImageUrl", source = "profile.profileImageUrl")
    @Mapping(target = "verified", source = "profile.verified")
    @Mapping(target = "location", source = "profile.location")
    @Mapping(target = "city", source = "profile.city")
    @Mapping(target = "state", source = "profile.state")
    @Mapping(target = "country", source = "profile.country")
    BusinessAccountDto toDto(BusinessAccount businessAccount);

    @Mapping(target = "email", source = "businessAccount.email")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "firstName", source = "businessAccount.firstName")
    @Mapping(target = "lastName", source = "businessAccount.lastName")
    BusinessProfileSummaryForInterviewDto toBusinessProfileSummaryForInterviewDto(BusinessProfile businessProfile);
}

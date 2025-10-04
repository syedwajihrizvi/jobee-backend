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
    BusinessAccountDto toDto(BusinessAccount businessAccount);

    @Mapping(target = "email", source = "businessAccount.email")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "firstName", source = "businessAccount.firstName")
    @Mapping(target = "lastName", source = "businessAccount.lastName")
    BusinessProfileSummaryForInterviewDto toBusinessProfileSummaryForInterviewDto(BusinessProfile businessProfile);
}

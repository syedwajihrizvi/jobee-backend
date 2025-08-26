package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.BusinessAccountDto;
import com.rizvi.jobee.entities.BusinessAccount;

@Mapper(componentModel = "spring")
public interface BusinessMapper {
    @Mapping(target = "companyName", source = "company.name")
    BusinessAccountDto toDto(BusinessAccount businessAccount);
}

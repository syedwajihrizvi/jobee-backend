package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.company.CompanyDto;
import com.rizvi.jobee.entities.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mapping(target = "location", expression = "java(company.getHqCity() + \", \" + company.getHqState() + \", \" + company.getHqCountry())")
    CompanyDto toCompanyDto(Company company);
}

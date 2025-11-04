package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.company.CompanyDto;
import com.rizvi.jobee.dtos.company.TopHiringCompanyDto;
import com.rizvi.jobee.entities.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mapping(target = "location", expression = "java(company.getHqCity() + \", \" + company.getHqState() + \", \" + company.getHqCountry())")
    @Mapping(target = "logoUrl", source = "logo")
    CompanyDto toCompanyDto(Company company);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "jobCount", source = "jobCount")
    @Mapping(target = "logoUrl", source = "logo")
    TopHiringCompanyDto toTopCompaniesDto(Long id, String name, String logo, Long jobCount);

    default TopHiringCompanyDto map(Object[] data) {
        return toTopCompaniesDto((Long) data[0], (String) data[1], (String) data[2], (Long) data[3]);
    }
}

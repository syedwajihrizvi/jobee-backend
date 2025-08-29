package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;

import com.rizvi.jobee.dtos.EducationDto;
import com.rizvi.jobee.entities.Education;

@Mapper(componentModel = "spring")
public interface EducationMapper {
    EducationDto toEducationDto(Education education);
}

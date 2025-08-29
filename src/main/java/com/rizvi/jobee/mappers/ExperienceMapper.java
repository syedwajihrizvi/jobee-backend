package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.ExperienceDto;
import com.rizvi.jobee.entities.Experience;

@Mapper(componentModel = "spring")
public interface ExperienceMapper {
    @Mapping(target = "currentlyWorking", expression = "java(experience.getTo() == null)")
    ExperienceDto toExperienceDto(Experience experience);
}

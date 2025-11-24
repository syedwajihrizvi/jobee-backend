package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.experience.ExperienceDto;
import com.rizvi.jobee.entities.Experience;

@Mapper(componentModel = "spring")
public interface ExperienceMapper {
    @Mapping(target = "currentlyWorking", expression = "java(experience.getTo() == null || experience.getTo().isEmpty() || experience.getTo().equalsIgnoreCase(\"present\"))")
    @Mapping(target = "location", expression = "java(cleanNullString(experience.getLocation()))")
    @Mapping(target = "city", expression = "java(cleanNullString(experience.getCity()))")
    @Mapping(target = "state", expression = "java(cleanNullString(experience.getState()))")
    @Mapping(target = "country", expression = "java(cleanNullString(experience.getCountry()))")
    ExperienceDto toExperienceDto(Experience experience);
    
    default String cleanNullString(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value;
    }
}

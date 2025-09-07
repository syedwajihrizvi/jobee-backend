package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.user.UserProfileSummaryForBusinessDto;
import com.rizvi.jobee.entities.UserProfile;

@Mapper(componentModel = "spring", uses = {
        SkillMapper.class,
        EducationMapper.class,
        ExperienceMapper.class
})
public interface UserProfileMapper {
    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "location", expression = "java(userProfile.getCity() + \", \" + userProfile.getCountry())")
    UserProfileSummaryForBusinessDto toSummaryDto(UserProfile userProfile);
}

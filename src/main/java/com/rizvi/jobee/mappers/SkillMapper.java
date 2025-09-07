package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;

import com.rizvi.jobee.dtos.skill.SkillDto;
import com.rizvi.jobee.dtos.skill.UserSkillDto;
import com.rizvi.jobee.entities.Skill;
import com.rizvi.jobee.entities.UserSkill;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    UserSkillDto toUserSkillDto(UserSkill userSkill);

    SkillDto toSkillDto(Skill skill);
}

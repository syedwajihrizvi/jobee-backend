package com.rizvi.jobee.dtos;

import lombok.Data;

@Data
public class UserSkillDto {
    private Long id;
    private Long experience;
    private SkillDto skill;
}

package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.skill.CreateUserSkillDto;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.entities.UserSkill;
import com.rizvi.jobee.exceptions.SkillNotFoundException;
import com.rizvi.jobee.repositories.SkillRepository;
import com.rizvi.jobee.repositories.UserSkillRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserSkillService {
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public UserSkill createUserSkill(CreateUserSkillDto request, UserProfile userProfile) {
        var skillName = request.getSkill();
        // Check if a skill with a name similar to the passed skill exists
        var strippedSkillName = skillName.replace(" ", "").toLowerCase();
        var skill = skillRepository.findByNameLike(strippedSkillName);
        if (skill == null) {
            throw new SkillNotFoundException("Skill not found with name: " + strippedSkillName);
        }
        var userSkill = userSkillRepository.findByUserProfileIdAndSkillId(userProfile.getId(), skill.getId());
        if (userSkill != null) {
            userSkill.setExperience(request.getExperience());
            userSkillRepository.save(userSkill);
            return userSkill;
        }
        var newUserSkill = UserSkill.builder().skill(skill).userProfile(userProfile)
                .experience(request.getExperience()).build();
        userProfile.addSkill(newUserSkill);
        var savedUserSkill = userSkillRepository.save(newUserSkill);
        return savedUserSkill;
    }
}

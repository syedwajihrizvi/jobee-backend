package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.skill.CreateUserSkillDto;
import com.rizvi.jobee.entities.Skill;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.entities.UserSkill;
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
        var skillSlug = skillName.replace(" ", "").toLowerCase();
        Skill skill;
        skill = skillRepository.findBySlug(skillSlug);
        if (skill == null) {
            skill = Skill.builder().name(skillName).slug(skillSlug).build();
            skill = skillRepository.save(skill);
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

    @Transactional
    public boolean createUserSkills(List<String> skills, UserProfile userProfile) {
        for (String parsedSkill : skills) {
            var skillSlug = parsedSkill.replace(" ", "").toLowerCase();
            Skill skill;
            skill = skillRepository.findBySlug(skillSlug);
            if (skill == null) {
                skill = Skill.builder().name(parsedSkill).slug(skillSlug).build();
                skill = skillRepository.save(skill);
            }
            var userSkill = userSkillRepository.findByUserProfileIdAndSkillId(userProfile.getId(), skill.getId());
            if (userSkill == null) {
                var newUserSkill = UserSkill.builder().skill(skill).userProfile(userProfile).build();
                userSkill = userSkillRepository.save(newUserSkill);
            }
            userProfile.addSkill(userSkill);
            userSkillRepository.save(userSkill);
        }
        return true;
    }
}

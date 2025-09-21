package com.rizvi.jobee.services;

import java.util.ArrayList;
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

    public void deleteUserSkill(Long userSkillId) {
        userSkillRepository.deleteById(userSkillId);
    }

    // TODO: Refactor methods and extract common code
    @Transactional
    public UserSkill createUserSkill(CreateUserSkillDto request, UserProfile userProfile) {
        var skillName = request.getSkill();
        // Check if a skill with a name similar to the passed skill exists
        var skillSlug = normalizeSkillName(skillName);
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
        // # TODO: Optimize this method to reduce number of DB calls
        System.out.println("SYED-DEBUG: Creating user skills for user: " + userProfile.getId() + ", skills: " + skills);
        var uniqueSkills = extractNewSkillsFromText(skills, userProfile);
        System.out.println("SYED-DEBUG: Unique skills to add: " + uniqueSkills);
        for (String parsedSkill : uniqueSkills) {
            var skillSlug = normalizeSkillName(parsedSkill);
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

    public List<String> extractNewSkillsFromText(List<String> skills, UserProfile userProfile) {
        // TODO: Optimize this method currently O(n^2)
        List<UserSkill> existingUserSkills = userSkillRepository.findByUserProfileId(userProfile.getId());
        List<String> newSkills = new ArrayList<>();
        for (String skill : skills) {
            var skillSlug = normalizeSkillName(skill);
            boolean exists = false;
            for (UserSkill userSkill : existingUserSkills) {
                if (userSkill.getSkillSlug().equals(skillSlug)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                newSkills.add(skill);
            }
        }
        return newSkills;
    }

    public List<UserSkill> getUserSkills(Long userId) {
        return userSkillRepository.findByUserProfileId(userId);
    }

    private String normalizeSkillName(String skill) {
        return skill.replace(" ", "").toLowerCase();
    }
}

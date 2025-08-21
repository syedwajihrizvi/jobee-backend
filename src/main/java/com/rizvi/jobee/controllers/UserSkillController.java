package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateUserSkillDto;
import com.rizvi.jobee.dtos.UserSkillDto;
import com.rizvi.jobee.entities.UserSkill;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.SkillNotFoundException;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.SkillRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.repositories.UserSkillRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles/skills")
public class UserSkillController {
        private final UserSkillRepository userSkillRepository;
        private final SkillRepository skillRepository;
        private final UserProfileRepository userProfileRepository;
        private final UserMapper userMapper;

        @PostMapping
        @Operation(summary = "Add skill to user profile")
        public ResponseEntity<UserSkillDto> addSkill(
                        @RequestBody CreateUserSkillDto createUserSkillDto,
                        @AuthenticationPrincipal CustomPrincipal principal,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                var skillId = createUserSkillDto.getSkillId();
                System.out.println(createUserSkillDto);
                var userId = principal.getId();
                var userProfile = userProfileRepository.findUserById(userId)
                                .orElseThrow(() -> new AccountNotFoundException(
                                                "User profile not found for user id: " + userId));
                System.out.println("Skill ID: " + skillId);
                var skill = skillRepository.findById(skillId)
                                .orElseThrow(() -> new SkillNotFoundException(skillId));
                System.out.println("Skill found: " + skill.getName());
                var userSkill = userSkillRepository.findByUserProfileIdAndSkillId(userId, skillId);
                if (userSkill != null) {
                        userSkill.setExperience(createUserSkillDto.getExperience());
                        userSkillRepository.save(userSkill);
                        return ResponseEntity.ok(userMapper.toUserSkillDto(userSkill));
                }
                var newUserSkill = UserSkill.builder().skill(skill).userProfile(userProfile)
                                .experience(createUserSkillDto.getExperience()).build();
                userProfile.addSkill(newUserSkill);
                userSkillRepository.save(newUserSkill);
                var uri = uriComponentsBuilder.path("/skills/{id}").buildAndExpand(newUserSkill.getId()).toUri();
                return ResponseEntity.created(uri).body(userMapper.toUserSkillDto(newUserSkill));
        }
}

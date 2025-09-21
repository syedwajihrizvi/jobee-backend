package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.skill.CreateUserSkillDto;
import com.rizvi.jobee.dtos.skill.UserSkillDto;
import com.rizvi.jobee.mappers.SkillMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.UserProfileService;
import com.rizvi.jobee.services.UserSkillService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles/skills")
public class UserSkillController {
        private final UserProfileService userProfileService;
        private final UserSkillService userSkillService;
        private final SkillMapper skillMapper;

        @GetMapping("/my-skills")
        @Operation(summary = "Get all skills for authenticated user")
        public ResponseEntity<List<UserSkillDto>> getAllSkillsForUser(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var id = principal.getId();
                var skills = userSkillService.getUserSkills(id).stream()
                                .map(skillMapper::toUserSkillDto)
                                .toList();
                return ResponseEntity.ok(skills);
        }

        @PostMapping
        @Operation(summary = "Add skill to user profile")
        @Transactional
        public ResponseEntity<UserSkillDto> addSkill(
                        @RequestBody CreateUserSkillDto createUserSkillDto,
                        @AuthenticationPrincipal CustomPrincipal principal,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                var userProfile = userProfileService.getUserProfileById(principal.getId());
                var savedSkill = userSkillService.createUserSkill(createUserSkillDto, userProfile);
                var uri = uriComponentsBuilder.path("/skills/{id}").buildAndExpand(savedSkill.getId()).toUri();
                return ResponseEntity.created(uri).body(skillMapper.toUserSkillDto(savedSkill));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete a skill from user profile")
        public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
                userSkillService.deleteUserSkill(id);
                return ResponseEntity.noContent().build();
        }
}

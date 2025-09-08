package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
}

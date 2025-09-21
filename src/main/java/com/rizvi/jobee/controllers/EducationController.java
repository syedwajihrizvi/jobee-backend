package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.education.CreateEducationDto;
import com.rizvi.jobee.dtos.education.EducationDto;
import com.rizvi.jobee.mappers.EducationMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.EducationService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles/education")
public class EducationController {
        private final UserProfileService userProfileService;
        private final EducationService educationService;
        private final EducationMapper educationMapper;

        @GetMapping("/my-education")
        @Operation(summary = "Get all education for authenticated user")
        public ResponseEntity<List<EducationDto>> getAllEducationForUser(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var id = principal.getId();
                var educations = educationService.getEducationsForUser(id).stream()
                                .map(educationMapper::toEducationDto)
                                .toList();
                return ResponseEntity.ok(educations);
        }

        @PostMapping
        @Operation(summary = "Add education information")
        public ResponseEntity<EducationDto> addEducation(
                        @RequestBody CreateEducationDto createEducationDto,
                        @AuthenticationPrincipal CustomPrincipal customPrincipal,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                var userId = customPrincipal.getId();
                var userProfile = userProfileService.getUserProfileById(userId);
                var savedEducation = educationService.createEducation(createEducationDto, userProfile);
                UriComponents uri = uriComponentsBuilder.path("/education/{id}").buildAndExpand(savedEducation.getId());
                return ResponseEntity.created(uri.toUri()).body(educationMapper.toEducationDto(savedEducation));
        }

        @PutMapping("/{id}")
        public ResponseEntity<EducationDto> updateEducation(
                        @PathVariable Long id,
                        @RequestBody CreateEducationDto updatedEducationDto) throws RuntimeException {
                var education = educationService.updateEducation(id, updatedEducationDto);
                return ResponseEntity.ok(educationMapper.toEducationDto(education));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete an education entry from user profile")
        public ResponseEntity<Void> deleteEducation(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                educationService.deleteEducation(id, principal.getId());
                return ResponseEntity.noContent().build();
        }
}

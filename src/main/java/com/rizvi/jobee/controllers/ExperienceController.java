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
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.experience.CreateExperienceDto;
import com.rizvi.jobee.dtos.experience.ExperienceDto;
import com.rizvi.jobee.mappers.ExperienceMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.ExperienceService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/profiles/experiences")
public class ExperienceController {
    private final UserProfileService userProfileService;
    private final ExperienceService experienceService;
    private final ExperienceMapper experienceMapper;

    @GetMapping
    @Operation(summary = "Get all experiences")
    public ResponseEntity<List<ExperienceDto>> getExperiences() {
        var experiences = experienceService.getAllExperiences().stream()
                .map(experienceMapper::toExperienceDto).toList();
        return ResponseEntity.ok(experiences);
    }

    @GetMapping("/my-experiences")
    @Operation(summary = "Get all experiences for authenticated user")
    public ResponseEntity<List<ExperienceDto>> getAllExperiencesForUser(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var id = principal.getId();
        var experiences = experienceService.getExperiencesForUser(id).stream()
                .map(experienceMapper::toExperienceDto)
                .toList();
        System.out.println("Experiences fetched for user ID: " + id + ", Count: " + experiences.size());
        return ResponseEntity.ok(experiences);
    }

    @PostMapping
    @Operation(summary = "Add experience information")
    public ResponseEntity<ExperienceDto> addExperience(
            @RequestBody CreateExperienceDto createExperienceDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var profileId = customPrincipal.getId();
        var userProfile = userProfileService.getUserProfileById(profileId);
        var savedExperience = experienceService.addExperience(createExperienceDto, userProfile);
        var uri = uriComponentsBuilder.path("/profiles/experience/{id}")
                .buildAndExpand(savedExperience.getId())
                .toUri();
        return ResponseEntity.created(uri).body(experienceMapper.toExperienceDto(savedExperience));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update experience information")
    public ResponseEntity<ExperienceDto> updateExperience(
            @PathVariable Long id,
            @RequestBody CreateExperienceDto updatedExperienceDto) {
        var updatedExperience = experienceService.updateExperience(updatedExperienceDto, id);
        return ResponseEntity.ok(experienceMapper.toExperienceDto(updatedExperience));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an experience entry from user profile")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal) {
        experienceService.deleteExperience(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}

package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.rizvi.jobee.entities.Experience;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.ExperienceNotFoundException;
import com.rizvi.jobee.mappers.ExperienceMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.ExperienceRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles/experiences")
public class ExperienceController {
    private final ExperienceRepository experienceRepository;
    private final UserProfileRepository userProfileRepository;
    private final ExperienceMapper experienceMapper;

    @GetMapping
    public ResponseEntity<List<ExperienceDto>> getExperiences() {
        var experiences = experienceRepository.findAll().stream().map(experienceMapper::toExperienceDto).toList();
        return ResponseEntity.ok(experiences);
    }

    @PostMapping
    @Operation(summary = "Add experience information")
    public ResponseEntity<ExperienceDto> addExperience(
            @RequestBody CreateExperienceDto createExperienceDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var profileId = customPrincipal.getId();
        var userProfile = userProfileRepository.findById(profileId);
        if (userProfile.isEmpty()) {
            throw new AccountNotFoundException("User profile not found for id: " + profileId);
        }
        var experience = Experience.builder().title(createExperienceDto.getTitle())
                .company(createExperienceDto.getCompany()).city(createExperienceDto.getCity())
                .country(createExperienceDto.getCountry()).from(createExperienceDto.getFrom())
                .to(createExperienceDto.getTo()).description(createExperienceDto.getDescription())
                .profile(userProfile.get()).build();
        var savedExperience = experienceRepository.save(experience);
        var uri = uriComponentsBuilder.path("/profiles/experience/{id}")
                .buildAndExpand(savedExperience.getId())
                .toUri();
        return ResponseEntity.created(uri).body(experienceMapper.toExperienceDto(savedExperience));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperienceDto> updateExperience(
            @PathVariable Long id,
            @RequestBody CreateExperienceDto updatedExperienceDto) {
        var experience = experienceRepository.findById(id).orElseThrow(() -> new ExperienceNotFoundException(id));
        experience.setTitle(updatedExperienceDto.getTitle());
        experience.setCompany(updatedExperienceDto.getCompany());
        experience.setCity(updatedExperienceDto.getCity());
        experience.setCountry(updatedExperienceDto.getCountry());
        experience.setFrom(updatedExperienceDto.getFrom());
        experience.setTo(updatedExperienceDto.getTo());
        experience.setDescription(updatedExperienceDto.getDescription());
        var savedExperience = experienceRepository.save(experience);
        return ResponseEntity.ok(experienceMapper.toExperienceDto(savedExperience));
    }
}

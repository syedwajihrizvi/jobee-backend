package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateEducationDto;
import com.rizvi.jobee.dtos.EducationDto;
import com.rizvi.jobee.entities.Education;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.EducationRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

// TODO: Implement the methods
@RestController
@AllArgsConstructor
@RequestMapping("/profiles/education")
public class EducationController {
        private final UserProfileRepository userProfileRepository;
        private final EducationRepository educationRepository;
        private final UserMapper userMapper;

        @PostMapping
        @Operation(summary = "Add education information")
        public ResponseEntity<EducationDto> addEducation(
                        @RequestBody CreateEducationDto createEducationDto,
                        @AuthenticationPrincipal CustomPrincipal customPrincipal,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                var userId = customPrincipal.getId();
                var userProfile = userProfileRepository.findUserById(userId)
                                .orElseThrow(() -> new AccountNotFoundException(
                                                "User profile not found for user id: " + userId));

                var education = Education.builder().degree(createEducationDto.getDegree())
                                .institution(createEducationDto.getInstitution())
                                .fromYear(createEducationDto.getFromYear())
                                .toYear(createEducationDto.getToYear())
                                .userProfile(userProfile).build();
                userProfile.addEducation(education);
                var savedEducation = educationRepository.save(education);
                userProfileRepository.save(userProfile);
                UriComponents uri = uriComponentsBuilder.path("/education/{id}").buildAndExpand(savedEducation.getId());
                return ResponseEntity.created(uri.toUri()).body(userMapper.toEducationDto(savedEducation));
        }
}

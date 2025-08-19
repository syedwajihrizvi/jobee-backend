package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateUserProfileDto;
import com.rizvi.jobee.dtos.UserProfileSummaryDto;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserAccountRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.S3Service;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles")
public class UserProfileController {
        private final UserAccountRepository userAccountRepository;
        private final UserProfileRepository userProfileRepository;
        private final JobRepository jobRepository;
        private final UserMapper userMapper;
        private final S3Service s3Service;

        // TODO: Only ADMIN can accedd this endpoint - Update SecurityConfig
        @GetMapping()
        public ResponseEntity<List<UserProfileSummaryDto>> getAllProfiles() {
                var userProfiles = userProfileRepository.findAll();
                var userProfileDtos = userProfiles.stream()
                                .map(userMapper::toProfileSummaryDto)
                                .toList();
                return ResponseEntity.ok(userProfileDtos);

        }

        @GetMapping("/me")
        public ResponseEntity<UserProfileSummaryDto> getMyProfile(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfile = userProfileRepository.findByAccountId(principal.getId())
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                var userProfileDto = userMapper.toProfileSummaryDto(userProfile);
                return ResponseEntity.ok(userProfileDto);
        }

        @PostMapping()
        public ResponseEntity<UserProfileSummaryDto> createUserProfile(
                        @RequestBody CreateUserProfileDto request,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                var userAccount = userAccountRepository.findById(request.getAccountId()).orElse(null);
                if (userAccount == null) {
                        throw new AccountNotFoundException(
                                        "User account with ID " + request.getAccountId() + " not found");
                }
                var userProfile = UserProfile.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .age(request.getAge())
                                .build();
                userProfile.setAccount(userAccount);
                var savedProfile = userProfileRepository.save(userProfile);
                var uri = uriComponentsBuilder.path("/profiles/{id}")
                                .buildAndExpand(savedProfile.getId()).toUri();
                var userProfileDto = userMapper.toProfileSummaryDto(savedProfile);
                return ResponseEntity.created(uri).body(userProfileDto);
        }

        @PatchMapping("/update-profile-image")
        @Operation(summary = "Update profile image")
        public ResponseEntity<UserProfileSummaryDto> updateProfileImage(
                        @RequestParam("profileImage") MultipartFile profileImage,
                        @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
                System.out.println("Updating profile image for user ID: " + principal.getId());
                System.out.println(profileImage);
                if (profileImage.isEmpty()) {
                        throw new IllegalArgumentException("Profile image file is empty");
                }
                var userId = principal.getId();
                var userProfile = userProfileRepository.findByAccountId(userId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                try {
                        s3Service.uploadProfileImage(userProfile.getId(), profileImage);
                        userProfile.setProfileImageUrl(userId.toString() + "_" + profileImage.getOriginalFilename());
                        userProfileRepository.save(userProfile);
                        System.out.println("FINISHED");
                } catch (Exception e) {
                        throw new AmazonS3Exception(e.getMessage());
                }
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(userProfile));
        }

        @PostMapping("/favorite-jobs")
        public ResponseEntity<?> addFavoriteJob(
                        @RequestParam Long jobId,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userId = principal.getId();
                var userProfile = userProfileRepository.findByAccountId(userId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                var job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new JobNotFoundException("Job with ID " + jobId + " not found"));
                userProfile.toggleFavoriteJob(job);
                userProfileRepository.save(userProfile);
                return ResponseEntity.ok().build();
        }
}

package com.rizvi.jobee.controllers;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.application.ApplicationSummaryDto;
import com.rizvi.jobee.dtos.job.JobIdDto;
import com.rizvi.jobee.dtos.user.CreateUserProfileDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileGeneralInfoDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileSummaryDto;
import com.rizvi.jobee.dtos.user.UserProfileSummaryDto;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.AccountService;
import com.rizvi.jobee.services.InterviewService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles")
public class UserProfileController {
        private final UserProfileRepository userProfileRepository;
        private final ApplicationRepository applicationRepository;
        private final JobRepository jobRepository;
        private final UserProfileService userProfileService;
        private final InterviewService interviewService;
        private final AccountService accountService;
        private final UserMapper userMapper;
        private final JobMapper jobMapper;
        private final ApplicationMapper applicationMapper;

        @GetMapping()
        @Operation(summary = "Get all user profiles")
        public ResponseEntity<List<UserProfileSummaryDto>> getAllProfiles() {
                var userProfiles = userProfileService.getAllUserProfiles();
                var userProfileDtos = userProfiles.stream()
                                .map(userMapper::toProfileSummaryDto)
                                .toList();
                return ResponseEntity.ok(userProfileDtos);

        }

        @GetMapping("/{id}")
        @Operation(summary = "Get user profile by ID")
        public ResponseEntity<UserProfileSummaryDto> getProfileById(@PathVariable Long id) {
                var userProfile = userProfileService.getUserProfileById(id);
                if (userProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                var userProfileDto = userMapper.toProfileSummaryDto(userProfile);
                return ResponseEntity.ok(userProfileDto);
        }

        @GetMapping("/me")
        @Operation(summary = "Get the authenticated user's profile")
        public ResponseEntity<UserProfileSummaryDto> getMyProfile(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfile = userProfileService.getAuthenticatedUserProfile(principal.getId());
                if (userProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                var userProfileDto = userMapper.toProfileSummaryDto(userProfile);
                return ResponseEntity.ok(userProfileDto);
        }

        // TODO: Should be in the ApplicationController
        @GetMapping("appliedJobs")
        @Operation(summary = "Get all the jobs the user has applied to")
        public ResponseEntity<List<?>> getUserAppliedJobs(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userId = principal.getId();
                var applications = applicationRepository.findByUserProfileId(userId);
                var applicationDtos = applications.stream()
                                .map(applicationMapper::toSummaryDto)
                                .toList();
                return ResponseEntity.ok(applicationDtos);
        }

        // # TODO: Should be in the ApplicationController
        @GetMapping("appliedJobs/{jobId}")
        @Operation(summary = "Get the users applications for a specific job")
        public ResponseEntity<ApplicationSummaryDto> getUserApplicationForJob(
                        @AuthenticationPrincipal CustomPrincipal principal,
                        @PathVariable Long jobId) {
                var userId = principal.getId();
                var application = applicationRepository.findByJobIdAndUserProfileId(jobId, userId);
                if (application == null) {
                        return ResponseEntity.notFound().build();
                }
                var applicationDto = applicationMapper.toSummaryDto(application);
                var interview = interviewService.getInterviewByJobIdAndCandidateId(jobId, userId);
                if (interview != null) {
                        applicationDto.setInterviewId(interview.getId());
                }
                return ResponseEntity.ok(applicationDto);
        }

        // # Should be in the JobController
        @GetMapping("/favorite-jobs")
        @Operation(summary = "Get all favorite jobs for the authenticated user")
        public ResponseEntity<List<JobIdDto>> getFavoriteJobs(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userId = principal.getId();
                var userProfile = userProfileRepository.findById(userId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                var favoriteJobIds = userProfile.getFavoriteJobs().stream()
                                .map(jobMapper::toJobIdDto)
                                .toList();
                return ResponseEntity.ok(favoriteJobIds);
        }

        @PostMapping()
        @Operation(summary = "Create a new user profile")
        public ResponseEntity<UserProfileSummaryDto> createUserProfile(
                        @RequestBody CreateUserProfileDto request,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                var userAccount = accountService.getUserAccountById(request.getAccountId());
                if (userAccount == null) {
                        throw new AccountNotFoundException(
                                        "User account with ID " + request.getAccountId() + " not found");
                }
                var userProfile = userProfileService.createUserProfile(request, userAccount);
                var uri = uriComponentsBuilder.path("/profiles/{id}")
                                .buildAndExpand(userProfile.getId()).toUri();
                var userProfileDto = userMapper.toProfileSummaryDto(userProfile);
                return ResponseEntity.created(uri).body(userProfileDto);
        }

        // # Should be in the JobController
        @PostMapping("/favorite-jobs")
        public ResponseEntity<?> addFavoriteJob(
                        @RequestParam Long jobId,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userId = principal.getId();
                var userProfile = userProfileRepository.findByAccountId(userId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                System.out.println("TOGGLING FAVORITE FOR JOB ID: " + jobId);
                var job = jobRepository.findById(Long.valueOf(jobId))
                                .orElseThrow(() -> new JobNotFoundException("Job with ID " + jobId + " not found"));
                userProfile.toggleFavoriteJob(job);
                userProfileRepository.save(userProfile);
                return ResponseEntity.ok().build();
        }

        @PatchMapping("/update-profile-image")
        @Operation(summary = "Update profile image")
        public ResponseEntity<UserProfileSummaryDto> updateProfileImage(
                        @RequestParam("profileImage") MultipartFile profileImage,
                        @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
                if (profileImage.isEmpty()) {
                        throw new IllegalArgumentException("Profile image file is empty");
                }
                var userId = principal.getId();
                var savedProfile = userProfileService.updateUserProfileImage(profileImage, userId);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(savedProfile));
        }

        @PatchMapping("/update-video-intro")
        @Operation(summary = "Update video introduction")
        public ResponseEntity<UserProfileSummaryDto> updateVideoIntro(
                        @RequestParam("videoIntro") MultipartFile videoIntro,
                        @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
                if (videoIntro.isEmpty()) {
                        throw new IllegalArgumentException("Video introduction file is empty");
                }
                var userId = principal.getId();
                var savedProfile = userProfileService.updateUserVideo(videoIntro, userId);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(savedProfile));
        }

        @PatchMapping("/remove-video-intro")
        @Operation(summary = "Remove video introduction")
        public ResponseEntity<Void> removeVideoIntro(
                        @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
                var userId = principal.getId();
                userProfileService.removeVideoIntro(userId);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/update-general-info")
        @Operation(summary = "Update general information of user profile")
        @Transactional
        public ResponseEntity<UserProfileSummaryDto> updateGeneralInfo(
                        @RequestBody UpdateUserProfileGeneralInfoDto request,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userId = principal.getId();
                var savedProfile = userProfileService.updateGeneralInformation(request, userId);
                if (savedProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(savedProfile));
        }

        @PatchMapping("/update-summary")
        @Operation(summary = "Update summary of user profile")
        public ResponseEntity<UserProfileSummaryDto> updateSummary(
                        @RequestBody UpdateUserProfileSummaryDto summaryDto,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userId = principal.getId();
                var savedProfile = userProfileService.updateUserSummary(summaryDto, userId);
                if (savedProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(savedProfile));
        }

        @PatchMapping("/complete-profile")
        @Transactional
        @Operation(summary = "User completes their profile using complete profile form on client side")
        public ResponseEntity<UserProfileSummaryDto> completeUserProfile(
                        @RequestPart("resume") MultipartFile document,
                        @RequestPart("profileImage") MultipartFile profileImage,
                        @RequestPart(name = "videoIntro", required = false) MultipartFile videoIntro,
                        @RequestPart("data") String request,
                        @RequestPart(name = "resumeTitle", required = false) String resumeTitle,
                        @AuthenticationPrincipal CustomPrincipal principal) throws RuntimeException, AmazonS3Exception {
                var userId = principal.getId();
                var userProfile = userProfileService.updateUserProfileViaCompleteProfile(
                                document, profileImage, videoIntro, resumeTitle, request, userId);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(userProfile));
        }

}

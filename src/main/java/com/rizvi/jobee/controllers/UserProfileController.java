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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rizvi.jobee.dtos.application.ApplicationSummaryDto;
import com.rizvi.jobee.dtos.job.JobIdDto;
import com.rizvi.jobee.dtos.user.CompleteProfileDto;
import com.rizvi.jobee.dtos.user.CreateUserProfileDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileGeneralInfoDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileSummaryDto;
import com.rizvi.jobee.dtos.user.UserProfileSummaryDto;
import com.rizvi.jobee.entities.UserDocument;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserAccountRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.S3Service;
import com.rizvi.jobee.services.UserDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles")
public class UserProfileController {
        private final UserAccountRepository userAccountRepository;
        private final UserProfileRepository userProfileRepository;
        private final ApplicationRepository applicationRepository;
        private final JobRepository jobRepository;
        private final UserMapper userMapper;
        private final JobMapper jobMapper;
        private final ApplicationMapper applicationMapper;
        private final UserDocumentService userDocumentService;
        private final S3Service s3Service;

        @GetMapping()
        public ResponseEntity<List<UserProfileSummaryDto>> getAllProfiles() {
                var userProfiles = userProfileRepository.findAll();
                var userProfileDtos = userProfiles.stream()
                                .map(userMapper::toProfileSummaryDto)
                                .toList();
                return ResponseEntity.ok(userProfileDtos);

        }

        @GetMapping("/{id}")
        public ResponseEntity<UserProfileSummaryDto> getProfileById(@PathVariable Long id) {
                var userProfile = userProfileRepository.findById(id)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                var userProfileDto = userMapper.toProfileSummaryDto(userProfile);
                return ResponseEntity.ok(userProfileDto);
        }

        @GetMapping("/me")
        public ResponseEntity<UserProfileSummaryDto> getMyProfile(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfile = userProfileRepository.findByAccountId(principal.getId())
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                var userProfileDto = userMapper.toProfileSummaryDto(userProfile);
                return ResponseEntity.ok(userProfileDto);
        }

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
                return ResponseEntity.ok(applicationDto);
        }

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

        @PatchMapping("/update-general-info")
        @Operation(summary = "Update general information of user profile")
        @Transactional
        public ResponseEntity<UserProfileSummaryDto> updateGeneralInfo(
                        @RequestBody UpdateUserProfileGeneralInfoDto request,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userId = principal.getId();
                var userProfile = userProfileRepository.findByAccountId(userId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                // Update the fields provided in the request
                if (request.getFirstName() != null)
                        userProfile.setFirstName(request.getFirstName());
                if (request.getLastName() != null)
                        userProfile.setLastName(request.getLastName());
                if (request.getTitle() != null)
                        userProfile.setTitle(request.getTitle());
                if (request.getCompany() != null)
                        userProfile.setCompany(request.getCompany());
                if (request.getPhone() != null)
                        userProfile.setPhoneNumber(request.getPhone());
                if (request.getCity() != null)
                        userProfile.setCity(request.getCity());
                if (request.getCountry() != null)
                        userProfile.setCountry(request.getCountry());
                if (request.getEmail() != null) {
                        var userAccount = userAccountRepository.findById(userId)
                                        .orElseThrow(() -> new AccountNotFoundException("User account not found"));
                        userAccount.setEmail(request.getEmail());
                        userAccountRepository.save(userAccount);
                }
                userProfileRepository.save(userProfile);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(userProfile));
        }

        @PatchMapping("/update-summary")
        @Operation(summary = "Update summary of user profile")
        public ResponseEntity<UserProfileSummaryDto> updateSummary(
                        @RequestBody UpdateUserProfileSummaryDto summaryDto) {
                var userId = 5L;
                var userProfile = userProfileRepository.findByAccountId(userId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                userProfile.setSummary(summaryDto.getSummary());
                userProfileRepository.save(userProfile);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(userProfile));
        }

        @PatchMapping("/complete-profile")
        @Transactional
        @Operation(summary = "User completes their profile using complete profile form on client side")
        public ResponseEntity<UserProfileSummaryDto> completeUserProfile(
                        @RequestPart("resume") MultipartFile document,
                        @RequestPart("profileImage") MultipartFile profileImage,
                        @RequestPart("data") String stringDto,
                        @AuthenticationPrincipal CustomPrincipal principal) throws RuntimeException, AmazonS3Exception {
                var userId = principal.getId();
                var userProfile = userProfileRepository.findByAccountId(userId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                CompleteProfileDto completeProfileDto;
                try {
                        completeProfileDto = new ObjectMapper().readValue(stringDto, CompleteProfileDto.class);
                        userProfile.setTitle(completeProfileDto.getTitle());
                        userProfile.setSummary(completeProfileDto.getSummary());
                        userProfile.setCity(completeProfileDto.getCity());
                        userProfile.setCountry(completeProfileDto.getCountry());
                        userProfile.setPhoneNumber(completeProfileDto.getPhoneNumber());
                        userProfile.setCompany(completeProfileDto.getCompany());
                } catch (JsonProcessingException e) {
                        System.out.println(e.getMessage());
                        return ResponseEntity.badRequest().body(null);
                }
                // Set Profile Image
                try {
                        s3Service.uploadProfileImage(userProfile.getId(), profileImage);
                        userProfile.setProfileImageUrl(userId.toString() + "_" + profileImage.getOriginalFilename());
                        userProfileRepository.save(userProfile);
                        System.out.println("FINISHED");
                } catch (Exception e) {
                        throw new AmazonS3Exception(e.getMessage());
                }
                // Upload the Resume
                var result = userDocumentService.uploadDocument(userId, document,
                                UserDocumentType.valueOf(UserDocumentType.RESUME.name()));
                if (result == null) {
                        return ResponseEntity.badRequest().build();
                }
                var userDocument = UserDocument.builder()
                                .documentType(UserDocumentType.valueOf(UserDocumentType.RESUME.name()))
                                .documentUrl(result).user(userProfile).build();
                userProfile.addDocument(userDocument);
                userProfile.setPrimaryResume(userDocument);
                userProfileRepository.save(userProfile);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(userProfile));
        }

}

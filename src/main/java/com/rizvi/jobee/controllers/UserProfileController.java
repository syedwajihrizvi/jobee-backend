package com.rizvi.jobee.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.rizvi.jobee.dtos.job.JobSummaryDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.dtos.job.RecommendedJobDto;
import com.rizvi.jobee.dtos.user.CreateUserProfileDto;
import com.rizvi.jobee.dtos.user.ProfileCompletenessDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileGeneralInfoDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileSummaryDto;
import com.rizvi.jobee.dtos.user.UserProfileDashboardSummaryDto;
import com.rizvi.jobee.dtos.user.UserProfileSummaryDto;
import com.rizvi.jobee.dtos.user.UserProfileSummaryForBusinessDto;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.mappers.ApplicationMapper;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.mappers.UserProfileMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.queries.ApplicationQuery;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.AccountService;
import com.rizvi.jobee.services.ApplicationService;
import com.rizvi.jobee.services.InterviewService;
import com.rizvi.jobee.services.JobRecommenderService;
import com.rizvi.jobee.services.UserDocumentService;
import com.rizvi.jobee.services.UserFavoriteJobService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles")
public class UserProfileController {
        private final UserProfileRepository userProfileRepository;
        private final ApplicationRepository applicationRepository;
        private final ApplicationService applicationService;
        private final JobRepository jobRepository;
        private final UserFavoriteJobService userFavoriteJobService;
        private final UserDocumentService userDocumentService;
        private final UserProfileService userProfileService;
        private final InterviewService interviewService;
        private final JobRecommenderService jobRecommenderService;
        private final AccountService accountService;
        private final UserMapper userMapper;
        private final JobMapper jobMapper;
        private final ApplicationMapper applicationMapper;
        private final UserProfileMapper userAccountMapper;

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
        public ResponseEntity<UserProfileSummaryForBusinessDto> getProfileById(@PathVariable Long id) {
                var userProfile = userProfileService.getUserProfileById(id);
                if (userProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                var userProfileDto = userAccountMapper.toSummaryDto(userProfile);
                return ResponseEntity.ok(userProfileDto);
        }

        @GetMapping("/me")
        @Operation(summary = "Get the authenticated user's profile")
        public ResponseEntity<UserProfileSummaryDto> getMyProfile(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var accountId = principal.getId();
                var userProfileId = principal.getProfileId();
                var userProfile = userProfileService.getAuthenticatedUserProfile(accountId);
                var canQuickApplyBatch = userProfileService.canQuickApplyBatch(userProfileId);
                if (userProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                var userProfileDto = userMapper.toProfileSummaryDto(userProfile);
                userProfileDto.setCanQuickApplyBatch(canQuickApplyBatch);
                if (canQuickApplyBatch == false) {
                        var nextQuickApplyTime = userProfileService.getNextQuickApplyBatchTime(principal.getId());
                        userProfileDto.setNextQuickApplyBatchTime(nextQuickApplyTime);
                }
                return ResponseEntity.ok(userProfileDto);
        }

        @GetMapping("/dashboard")
        @Operation(summary = "Get the authenticated user's dashboard data")
        public ResponseEntity<UserProfileDashboardSummaryDto> getUserDashboardData(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var accountId = principal.getId();
                var userProfile = userProfileService.getAuthenticatedUserProfile(accountId);
                if (userProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                var dashboardData = userMapper.toDashboardSummaryDto(userProfile);
                return ResponseEntity.ok(dashboardData);
        }

        @GetMapping("/profile-completeness")
        @Operation(summary = "Get the authenticated user's profile completeness percentage")
        public ResponseEntity<ProfileCompletenessDto> getProfileCompleteness(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                var completeness = userProfileService.calculateProfileCompleteness(userProfileId);
                var dto = new ProfileCompletenessDto();
                dto.setId(userProfileId);
                dto.setCompleteness(completeness);
                return ResponseEntity.ok(dto);
        }

        @PatchMapping("/favorite-company")
        @Operation(summary = "Add or remove a company from the authenticated user's favorite companies")
        public ResponseEntity<Void> favoriteCompany(
                        @RequestParam Long companyId,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                userProfileService.toggleFavoriteCompany(companyId, userProfileId);
                return ResponseEntity.ok().build();
        }

        @GetMapping("appliedJobs")
        @Operation(summary = "Get all the jobs the user has applied to")
        public ResponseEntity<PaginatedResponse<JobSummaryDto>> getUserAppliedJobs(
                        @RequestParam(defaultValue = "0") Integer pageNumber,
                        @RequestParam(defaultValue = "10") Integer pageSize,
                        @ModelAttribute ApplicationQuery applicationQuery,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                applicationQuery.setUserProfileId(userProfileId);
                var paginatedApplications = applicationService.getAllApplications(applicationQuery, pageNumber,
                                pageSize);
                var jobSummariesDto = paginatedApplications.getContent().stream()
                                .map(application -> jobMapper.toSummaryDto(application.getJob()))
                                .toList();
                System.out.println("SYED-DEBUG: Total applied jobs found: " + paginatedApplications.getTotalElements());
                var hasMore = paginatedApplications.isHasMore();
                var total = paginatedApplications.getTotalElements();
                return ResponseEntity.ok(new PaginatedResponse<JobSummaryDto>(hasMore, jobSummariesDto, total));
        }

        @GetMapping("appliedJobs/{jobId}")
        @Operation(summary = "Get the users applications for a specific job")
        public ResponseEntity<ApplicationSummaryDto> getUserApplicationForJob(
                        @AuthenticationPrincipal CustomPrincipal principal,
                        @PathVariable Long jobId) {
                var userProfileId = principal.getProfileId();
                var application = applicationRepository.findByJobIdAndUserProfileId(jobId, userProfileId);
                if (application == null) {
                        return ResponseEntity.ok().build();
                }
                var applicationDto = applicationMapper.toSummaryDto(application);
                var interview = interviewService.getInterviewByJobIdAndCandidateId(jobId, userProfileId);
                if (interview != null) {
                        applicationDto.setInterviewId(interview.getId());
                }
                return ResponseEntity.ok(applicationDto);
        }

        @GetMapping("/favorite-jobs")
        @Operation(summary = "Get all favorite jobs for the authenticated user")
        public ResponseEntity<PaginatedResponse<JobSummaryDto>> getFavoriteJobs(
                        @RequestParam(defaultValue = "0") Integer pageNumber,
                        @RequestParam(defaultValue = "10") Integer pageSize,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                var pagiantedJobData = userFavoriteJobService.getFavoriteJobs(userProfileId, pageNumber, pageSize);
                return ResponseEntity.ok(pagiantedJobData);
        }

        @GetMapping("/recommended-jobs")
        @Operation(summary = "Get AI recommended jobs for the authenticated user")
        public ResponseEntity<List<RecommendedJobDto>> getRecommendedJobs(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                var userProfile = userProfileRepository.findById(userProfileId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                var jobs = jobRecommenderService.getRecommendedJobsForUser(userProfile);
                List<RecommendedJobDto> result = new ArrayList<>();
                for (Map.Entry<Job, Long> entry : jobs.entrySet()) {
                        var recommendedJobDto = new RecommendedJobDto();
                        recommendedJobDto.setJob(jobMapper.toSummaryDto(entry.getKey()));
                        recommendedJobDto.setMatch(entry.getValue());
                        result.add(recommendedJobDto);
                }
                var sortedResult = result.stream()
                                .sorted((a, b) -> b.getMatch().compareTo(a.getMatch()))
                                .toList();
                return ResponseEntity.ok(sortedResult);
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

        @PostMapping("/favorite-jobs")
        @Operation(summary = "Add or remove a job from the authenticated user's favorite jobs")
        public ResponseEntity<?> addFavoriteJob(
                        @RequestParam Long jobId,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var accountId = principal.getId();
                var userProfile = userProfileRepository.findByAccountId(accountId)
                                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
                var job = jobRepository.findById(Long.valueOf(jobId))
                                .orElseThrow(() -> new JobNotFoundException("Job with ID " + jobId + " not found"));
                userProfile.toggleFavoriteJob(job);
                userProfileRepository.save(userProfile);
                return ResponseEntity.ok().build();
        }

        @PatchMapping("/views")
        @Operation(summary = "Increment the profile view count")
        public ResponseEntity<Void> incrementProfileViews(@RequestParam Long profileId) {
                userProfileService.incrementProfileViews(profileId);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/update-profile-image")
        @Operation(summary = "Update profile image")
        public ResponseEntity<UserProfileSummaryDto> updateProfileImage(
                        @RequestParam("profileImage") MultipartFile profileImage,
                        @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
                if (profileImage.isEmpty()) {
                        throw new IllegalArgumentException("Profile image file is empty");
                }
                var userProfileId = principal.getProfileId();
                var savedProfile = userProfileService.updateUserProfileImage(profileImage, userProfileId);
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
                var userProfileId = principal.getProfileId();
                var savedProfile = userProfileService.updateUserVideo(videoIntro, userProfileId);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(savedProfile));
        }

        @PatchMapping("/remove-video-intro")
        @Operation(summary = "Remove video introduction")
        public ResponseEntity<Void> removeVideoIntro(
                        @AuthenticationPrincipal CustomPrincipal principal) throws AmazonS3Exception {
                var userProfileId = principal.getProfileId();
                userProfileService.removeVideoIntro(userProfileId);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/update-general-info")
        @Operation(summary = "Update general information of user profile")
        @Transactional
        public ResponseEntity<UserProfileSummaryDto> updateGeneralInfo(
                        @RequestBody UpdateUserProfileGeneralInfoDto request,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                var savedProfile = userProfileService.updateGeneralInformation(request, userProfileId);
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
                var userProfileId = principal.getProfileId();
                var savedProfile = userProfileService.updateUserSummary(summaryDto, userProfileId);
                if (savedProfile == null) {
                        throw new AccountNotFoundException("User profile not found");
                }
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(savedProfile));
        }

        @PatchMapping("/complete-profile")
        @Transactional
        @Operation(summary = "User completes their profile using complete profile form on client side")
        public ResponseEntity<UserProfileSummaryDto> completeUserProfile(
                        @RequestPart("profileImage") MultipartFile profileImage,
                        @RequestPart(name = "videoIntro", required = false) MultipartFile videoIntro,
                        @RequestPart("data") String request,
                        @AuthenticationPrincipal CustomPrincipal principal) throws RuntimeException, AmazonS3Exception {
                var userProfileId = principal.getProfileId();
                var userProfile = userProfileService.updateUserProfileViaCompleteProfile(
                                profileImage, videoIntro, request, userProfileId);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(userProfile));
        }

        @PatchMapping("/update-primary-resume")
        @Operation(summary = "Update primary resume for the authenticated user")
        public ResponseEntity<UserProfileSummaryDto> updatePrimaryResume(
                        @RequestParam("resumeId") Long resumeId,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                var document = userDocumentService.userDocumentExists(resumeId, userProfileId);
                if (document == null) {
                        return ResponseEntity.notFound().build();
                }
                var savedProfile = userProfileService.updatePrimaryResume(document, userProfileId);
                return ResponseEntity.ok().body(userMapper.toProfileSummaryDto(savedProfile));
        }

}

package com.rizvi.jobee.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rizvi.jobee.dtos.user.CompleteProfileDto;
import com.rizvi.jobee.dtos.user.CreateUserProfileDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileGeneralInfoDto;
import com.rizvi.jobee.dtos.user.UpdateUserProfileSummaryDto;
import com.rizvi.jobee.entities.QuickApplyTS;
import com.rizvi.jobee.entities.UserAccount;
import com.rizvi.jobee.entities.UserDocument;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.CompanyNotFoundException;
import com.rizvi.jobee.repositories.CompanyRepository;
import com.rizvi.jobee.repositories.QuickApplyTSRepository;
import com.rizvi.jobee.repositories.UserAccountRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final QuickApplyTSRepository quickApplyTSRepository;
    private final CompanyRepository companyRepository;
    private final S3Service s3Service;

    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
    }

    public UserProfile getUserProfileById(Long userId) {
        return userProfileRepository.findById(userId).orElse(null);
    }

    public UserProfile getAuthenticatedUserProfile(Long accountId) {
        return userProfileRepository.findByAccountId(accountId).orElse(null);
    }

    public UserProfile createUserProfile(
            CreateUserProfileDto request,
            UserAccount userAccount) {
        var userProfile = UserProfile.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .age(request.getAge())
                .build();
        userProfile.setAccount(userAccount);
        var savedProfile = userProfileRepository.save(userProfile);
        return savedProfile;
    }

    public UserProfile updateUserProfileImage(MultipartFile profileImage, Long accountId)
            throws AmazonS3Exception {
        var userProfile = userProfileRepository.findByAccountId(accountId).orElse(null);
        if (userProfile == null) {
            return null;
        }
        try {
            s3Service.uploadProfileImage(accountId, profileImage);
            userProfile.setProfileImageUrl(accountId.toString() + "_" + profileImage.getOriginalFilename());
            return userProfileRepository.save(userProfile);
        } catch (Exception e) {
            throw new AmazonS3Exception(e.getMessage());
        }
    }

    public UserProfile updateGeneralInformation(UpdateUserProfileGeneralInfoDto request, Long accountId) {
        var userProfile = userProfileRepository.findById(accountId).orElse(null);
        if (userProfile == null) {
            return null;
        }
        if (request.getFirstName() != null)
            userProfile.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            userProfile.setLastName(request.getLastName());
        if (request.getTitle() != null)
            userProfile.setTitle(request.getTitle());
        if (request.getCompany() != null)
            userProfile.setCompany(request.getCompany());
        if (request.getPhoneNumber() != null)
            userProfile.setPhoneNumber(request.getPhoneNumber());
        if (request.getCity() != null)
            userProfile.setCity(request.getCity());
        if (request.getCountry() != null)
            userProfile.setCountry(request.getCountry());
        if (request.getState() != null)
            userProfile.setState(request.getState());
        if (request.getProvince() != null)
            userProfile.setProvince(request.getProvince());
        if (request.getEmail() != null) {
            var userAccount = userAccountRepository.findById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException("User account not found"));
            userAccount.setEmail(request.getEmail());
            userAccountRepository.save(userAccount);
        }
        var savedProfile = userProfileRepository.save(userProfile);
        return savedProfile;
    }

    public UserProfile updateUserSummary(UpdateUserProfileSummaryDto request, Long accountId) {
        var userProfile = userProfileRepository.findById(accountId).orElse(null);
        if (userProfile == null) {
            return null;
        }
        userProfile.setSummary(request.getSummary());
        var savedProfile = userProfileRepository.save(userProfile);
        return savedProfile;
    }

    public UserProfile updateUserVideo(MultipartFile videoIntro, Long accountId) throws AmazonS3Exception {
        var userProfile = userProfileRepository.findById(accountId).orElse(null);
        if (userProfile == null) {
            return null;
        }
        try {
            s3Service.uploadVideoIntro(accountId, videoIntro);
            userProfile.setVideoIntroUrl(accountId.toString());
            var savedProfile = userProfileRepository.save(userProfile);
            return savedProfile;
        } catch (Exception e) {
            throw new AmazonS3Exception(e.getMessage());
        }
    }

    public Void removeVideoIntro(Long accountId) throws AmazonS3Exception {
        var userProfile = userProfileRepository.findById(accountId).orElse(null);
        if (userProfile == null) {
            return null;
        }
        userProfile.setVideoIntroUrl(null);
        userProfileRepository.save(userProfile);
        return null;
    }

    @Transactional
    public UserProfile updateUserProfileViaCompleteProfile(
            MultipartFile profileImage, MultipartFile videoIntro,
            String request, Long userId)
            throws RuntimeException, AmazonS3Exception {
        var userProfile = userProfileRepository.findById(userId).orElse(null);
        if (userProfile == null) {
            throw new AccountNotFoundException("User profile not found");
        }
        CompleteProfileDto completeProfileDto;
        try {
            completeProfileDto = new ObjectMapper().readValue(request, CompleteProfileDto.class);
            userProfile.setTitle(completeProfileDto.getTitle());
            userProfile.setSummary(completeProfileDto.getSummary());
            userProfile.setPhoneNumber(completeProfileDto.getPhoneNumber());
        } catch (JsonProcessingException e) {
            // TODO: Handle the exception properly
            throw new RuntimeException("Failed to parse request body");
        }
        try {
            s3Service.uploadProfileImage(userProfile.getId(), profileImage);
            userProfile.setProfileImageUrl(userId.toString() + "_" + profileImage.getOriginalFilename());
            userProfileRepository.save(userProfile);
        } catch (Exception e) {
            throw new AmazonS3Exception(e.getMessage());
        }
        if (videoIntro != null)
            try {
                s3Service.uploadVideoIntro(userProfile.getId(), videoIntro);
                userProfile.setVideoIntroUrl(userId.toString());
                userProfileRepository.save(userProfile);
            } catch (Exception e) {
                throw new AmazonS3Exception(e.getMessage());
            }
        var savedProfile = userProfileRepository.findByAccountId(userId).orElse(null);
        if (savedProfile == null) {
            throw new AccountNotFoundException("User profile not found");
        }
        return savedProfile;
    }

    public void incrementProfileViews(Long profileId) {
        var userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
        userProfile.setProfileViews(userProfile.getProfileViews() + 1);
        userProfileRepository.save(userProfile);
    }

    public Integer calculateProfileCompleteness(Long profileId) {
        var userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
        int completeness = 0;
        if (userProfile.getFirstName() != null && !userProfile.getFirstName().isEmpty())
            completeness += 2;
        if (userProfile.getLastName() != null && !userProfile.getLastName().isEmpty())
            completeness += 2;
        if (userProfile.getAccount().getEmail() != null && !userProfile.getAccount().getEmail().isEmpty())
            completeness += 2;
        if (userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().isEmpty())
            completeness += 2;
        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty())
            completeness += 10;
        if (userProfile.getTitle() != null && !userProfile.getTitle().isEmpty())
            completeness += 2;
        if (userProfile.getSummary() != null && !userProfile.getSummary().isEmpty())
            completeness += 10;
        if (userProfile.getSkills().size() > 0)
            completeness += 10;
        if (userProfile.getEducation().size() > 0)
            completeness += 10;
        if (userProfile.getExperiences().size() > 0)
            completeness += 10;
        if (userProfile.getProjects().size() > 0)
            completeness += 10;
        if (userProfile.getPrimaryResume() != null)
            completeness += 30;
        return Math.min(completeness, 100);
    }

    public void toggleFavoriteCompany(Long companyId, Long userProfileId) {
        var userProfile = userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
        var favoriteCompanies = userProfile.getFavoriteCompanies();
        var company = companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException());
        System.out.println("Looking for company: " + companyId);
        if (favoriteCompanies.contains(company)) {
            System.out.println("Removing company from favorites: " + companyId);
            favoriteCompanies.remove(company);
        } else {
            System.out.println("Adding company to favorites: " + companyId);
            favoriteCompanies.add(company);
        }
        userProfile.setFavoriteCompanies(favoriteCompanies);
        userProfileRepository.save(userProfile);
    }

    public boolean canQuickApplyBatch(Long userId) {
        var quickApplyTS = quickApplyTSRepository.findByUserProfileId(userId);
        if (quickApplyTS == null) {
            return true;
        }
        var lastQuickApply = quickApplyTS.getLastQuickApply();
        var now = java.time.Instant.now();
        var hoursSinceLastApply = java.time.Duration.between(lastQuickApply, now).toHours();
        return hoursSinceLastApply >= 6;
    }

    public QuickApplyTS updateQuickApplyTimestamp(Long userId) {
        var quickApplyTS = quickApplyTSRepository.findByUserProfileId(userId);
        if (quickApplyTS == null) {
            var userProfile = userProfileRepository.findById(userId)
                    .orElseThrow(() -> new AccountNotFoundException("User profile not found"));
            quickApplyTS = QuickApplyTS.builder()
                    .userProfile(userProfile)
                    .quickApplyCount(1)
                    .lastQuickApply(java.time.Instant.now())
                    .build();
        } else {
            quickApplyTS.setQuickApplyCount(quickApplyTS.getQuickApplyCount() + 1);
            quickApplyTS.setLastQuickApply(java.time.Instant.now());
        }
        return quickApplyTSRepository.save(quickApplyTS);
    }

    public Instant getNextQuickApplyBatchTime(Long userId) {
        var quickApplyTS = quickApplyTSRepository.findByUserProfileId(userId);
        if (quickApplyTS == null) {
            return java.time.Instant.now();
        }
        var lastQuickApply = quickApplyTS.getLastQuickApply();
        return lastQuickApply.plus(6, ChronoUnit.HOURS);
    }

    public UserProfile updatePrimaryResume(UserDocument resume, Long userId) {
        var userProfile = userProfileRepository.findById(userId).orElse(null);
        if (userProfile == null) {
            throw new AccountNotFoundException("User profile not found");
        }
        userProfile.setPrimaryResume(resume);
        return userProfileRepository.save(userProfile);
    }
}

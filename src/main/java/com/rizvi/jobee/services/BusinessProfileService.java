package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.dtos.user.UpdateBusinessProfileGeneralInfoDto;
import com.rizvi.jobee.entities.BusinessProfile;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.BusinessProfileRepository;

import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BusinessProfileService {
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final S3Service s3Service;

    public BusinessProfile getBusinessProfileByAccountId(Long userId) {
        return businessProfileRepository.findByBusinessAccountId(userId).orElse(null);
    }

    public BusinessProfile getBusinessProfileById(Long businessProfileId) {
        return businessProfileRepository.findById(businessProfileId).orElse(null);
    }

    public BusinessProfile getBusinessProfileByEmail(String email) {
        return businessProfileRepository.findBusinessProfileByEmail(email.replace(" ", ""));
    }

    public BusinessProfile updateBusinessProfileGeneralInfo(UpdateBusinessProfileGeneralInfoDto request, Long userId) {
        var businessProfile = businessProfileRepository.findByBusinessAccountId(userId).orElse(null);
        var businessAccount = businessProfile.getBusinessAccount();
        if (businessProfile == null || businessAccount == null) {
            throw new AccountNotFoundException("Business profile not found");
        }
        if (request.getFirstName() != null) {
            businessAccount.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            businessAccount.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            businessAccount.setEmail(request.getEmail());
        }
        if (request.getTitle() != null) {
            businessProfile.setTitle(request.getTitle());
        }
        if (request.getCity() != null) {
            businessProfile.setCity(request.getCity());
        }
        if (request.getState() != null) {
            businessProfile.setState(request.getState());
        }
        if (request.getCountry() != null) {
            businessProfile.setCountry(request.getCountry());
        }
        businessAccountRepository.save(businessAccount);
        return businessProfileRepository.save(businessProfile);
    }

    public BusinessProfile updateProfileImage(MultipartFile profileImage, Long userId) throws AmazonS3Exception {
        var businessProfile = businessProfileRepository.findByBusinessAccountId(userId).orElse(null);
        if (businessProfile == null) {
            throw new AccountNotFoundException("Business profile not found");
        }
        try {
            s3Service.updateBusinessProfileImage(userId, profileImage);
            businessProfile.setProfileImageUrl(userId.toString() + "_" + profileImage.getOriginalFilename());
            return businessProfileRepository.save(businessProfile);
        } catch (Exception e) {
            throw new AmazonS3Exception(e.getMessage());
        }
    }

    public Long getCompanyIdForBusinessProfileId(Long businessProfileId) {
        var businessProfile = businessProfileRepository.findByBusinessAccountId(businessProfileId).orElse(null);
        if (businessProfile == null) {
            throw new AccountNotFoundException("Business profile not found");
        }
        return businessProfile.getBusinessAccount().getCompany().getId();
    }

}

package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.socialMedia.CreateSocialMediaDto;
import com.rizvi.jobee.entities.BusinessSocial;
import com.rizvi.jobee.enums.SocialType;
import com.rizvi.jobee.repositories.BusinessSocialRepository;
import com.rizvi.jobee.exceptions.AccountNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BusinessSocialMediaService {
    private final BusinessSocialRepository socialMediaRepository;
    private final BusinessProfileService businessProfileService;

    public List<BusinessSocial> getAllSocialMediaLinksForUserId(Long userId) {
        return socialMediaRepository.findByUserProfileId(userId);
    }

    public BusinessSocial createSocialMediaForUser(Long userId, CreateSocialMediaDto request) {
        var businessProfile = businessProfileService.getBusinessProfileByAccountId(userId);
        if (businessProfile == null) {
            throw new AccountNotFoundException("User profile not found for id: " + userId);
        }
        var businessSocial = new BusinessSocial();
        businessSocial.setUrl(request.getUrl());
        businessSocial.setType(SocialType.valueOf(request.getType()));
        businessSocial.setBusinessProfile(businessProfile);
        return socialMediaRepository.save(businessSocial);
    }

    public BusinessSocial updateSocialMediaForUser(Long userId, Long socialMediaId, CreateSocialMediaDto request) {
        var socialMedia = socialMediaRepository.findByIdAndUserProfileId(socialMediaId, userId);
        if (socialMedia == null) {
            throw new AccountNotFoundException(
                    "Social media link not found for id: " + socialMediaId + " and user id: " + userId);
        }
        socialMedia.setUrl(request.getUrl());
        socialMedia.setType(SocialType.valueOf(request.getType()));
        return socialMediaRepository.save(socialMedia);
    }
}

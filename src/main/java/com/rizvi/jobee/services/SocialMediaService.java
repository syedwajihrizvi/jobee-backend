package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.socialMedia.CreateSocialMediaDto;
import com.rizvi.jobee.entities.Social;
import com.rizvi.jobee.enums.SocialType;
import com.rizvi.jobee.repositories.SocialMediaRepository;
import com.rizvi.jobee.exceptions.AccountNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SocialMediaService {
    private final SocialMediaRepository socialMediaRepository;
    private final UserProfileService userProfileService;

    public List<Social> getAllSocialMediaLinksForUserId(Long userId) {
        return socialMediaRepository.findByUserProfileId(userId);
    }

    public Social createSocialMediaForUser(Long userId, CreateSocialMediaDto request) {
        var userProfile = userProfileService.getUserProfileById(userId);
        if (userProfile == null) {
            throw new AccountNotFoundException("User profile not found for id: " + userId);
        }
        var social = new Social();
        social.setUrl(request.getUrl());
        social.setType(SocialType.valueOf(request.getType()));
        social.setUserProfile(userProfile);
        return socialMediaRepository.save(social);
    }

    public Social updateSocialMediaForUser(Long userId, Long socialMediaId, CreateSocialMediaDto request) {
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

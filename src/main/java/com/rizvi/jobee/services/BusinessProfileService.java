package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.user.UpdateBusinessProfileGeneralInfoDto;
import com.rizvi.jobee.entities.BusinessProfile;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.BusinessProfileRepository;

import com.rizvi.jobee.exceptions.AccountNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BusinessProfileService {
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessAccountRepository businessAccountRepository;

    public BusinessProfile getBusinessProfileByEmail(String email) {
        return businessProfileRepository.findBusinessProfileByEmail(email.replace(" ", ""));
    }

    public BusinessProfile updateBusinessProfileGeneralInfo(UpdateBusinessProfileGeneralInfoDto request, Long userId) {
        var businessProfile = businessProfileRepository.findById(userId).orElse(null);
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
}

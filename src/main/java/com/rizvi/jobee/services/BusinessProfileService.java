package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.BusinessProfile;
import com.rizvi.jobee.repositories.BusinessProfileRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BusinessProfileService {
    private final BusinessProfileRepository businessProfileRepository;

    public BusinessProfile getBusinessProfileByEmail(String email) {
        return businessProfileRepository.findBusinessProfileByEmail(email.replace(" ", ""));
    }
}

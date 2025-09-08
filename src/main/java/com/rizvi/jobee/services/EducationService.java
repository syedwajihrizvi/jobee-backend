package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.education.CreateEducationDto;
import com.rizvi.jobee.entities.Education;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.EducationNotFoundException;
import com.rizvi.jobee.repositories.EducationRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class EducationService {
    private final UserProfileRepository userProfileRepository;
    private final EducationRepository educationRepository;

    public Education createEducation(CreateEducationDto request, UserProfile userProfile) {

        var education = Education.builder().degree(request.getDegree())
                .institution(request.getInstitution())
                .fromYear(request.getFromYear())
                .toYear(request.getToYear())
                .userProfile(userProfile).build();
        userProfile.addEducation(education);
        var savedEducation = educationRepository.save(education);
        userProfileRepository.save(userProfile);
        return savedEducation;
    }

    public Education updateEducation(Long educationId, CreateEducationDto request) {
        var education = educationRepository.findById(educationId).orElse(null);
        if (education == null) {
            throw new EducationNotFoundException(educationId);
        }
        education.setDegree(request.getDegree());
        education.setInstitution(request.getInstitution());
        education.setFromYear(request.getFromYear());
        education.setToYear(request.getToYear());
        return educationRepository.save(education);
    }
}

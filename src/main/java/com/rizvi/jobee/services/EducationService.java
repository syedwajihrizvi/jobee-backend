package com.rizvi.jobee.services;

import java.util.List;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.education.CreateEducationDto;
import com.rizvi.jobee.entities.Education;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.EducationNotFoundException;
import com.rizvi.jobee.exceptions.UnauthorizedException;
import com.rizvi.jobee.helpers.AISchemas.AIEducation;
import com.rizvi.jobee.repositories.EducationRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class EducationService {
    private final UserProfileRepository userProfileRepository;
    private final EducationRepository educationRepository;

    public List<Education> getEducationsForUser(Long userId) {
        return educationRepository.findByUserProfileId(userId);
    }

    public void deleteEducation(Long educationId, Long userId) {
        var education = educationRepository.findById(educationId).orElse(null);
        if (education == null) {
            throw new EducationNotFoundException(educationId);
        }
        if (!education.getUserProfile().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this education");
        }
        educationRepository.deleteById(educationId);
    }

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

    @Transactional
    public boolean createEducationsForUserFromAISchemas(
            List<AIEducation> educations, UserProfile userProfile) {
        for (AIEducation education : educations) {
            if (!educationExists(education, userProfile) &&
                    education.degree != null &&
                    education.institution != null) {
                var newEducation = Education.builder()
                        .degree(education.degree)
                        .institution(education.institution)
                        .userProfile(userProfile)
                        .fromYear(education.fromYear)
                        .toYear(education.toYear)
                        .build();
                userProfile.addEducation(newEducation);
                educationRepository.save(newEducation);
            }
        }
        userProfileRepository.save(userProfile);
        return true;
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

    public boolean educationExists(AIEducation education, UserProfile userProfile) {
        var educations = educationRepository.findByUserProfileId(userProfile.getId());
        for (Education edu : educations) {
            if (edu.isNew(education)) {
                return true;
            }
        }
        return false;
    }
}

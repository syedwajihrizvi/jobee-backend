package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.experience.CreateExperienceDto;
import com.rizvi.jobee.entities.Experience;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.ExperienceNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AIExperience;
import com.rizvi.jobee.repositories.ExperienceRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ExperienceService {
    private final ExperienceRepository experienceRepository;

    public List<Experience> getAllExperiences() {
        return experienceRepository.findAll();
    }

    public List<Experience> getExperiencesForUser(Long userId) {
        return experienceRepository.findByUserProfileId(userId);
    }

    public Experience addExperience(CreateExperienceDto request, UserProfile userProfile) {
        var experience = Experience.builder().title(request.getTitle())
                .company(request.getCompany()).city(request.getCity())
                .country(request.getCountry()).from(request.getFrom())
                .to(request.getTo()).description(request.getDescription())
                .userProfile(userProfile).build();
        return experienceRepository.save(experience);
    }

    public boolean addExperiencesForUserFromAISchemas(
            List<AIExperience> experiences, UserProfile userProfile) {
        for (AIExperience experience : experiences) {
            if (!experienceExists(experience, userProfile)) {
                var newExperience = Experience.builder()
                        .title(experience.getTitle())
                        .company(experience.getCompany())
                        .description(experience.getDescription())
                        .from(experience.getFromYear())
                        .to(experience.getToYear())
                        .userProfile(userProfile)
                        .build();
                experienceRepository.save(newExperience);
            }
        }
        return true;
    }

    public Experience updateExperience(CreateExperienceDto request, Long id) {
        var experience = experienceRepository.findById(id).orElseThrow(() -> new ExperienceNotFoundException(id));
        experience.setTitle(request.getTitle());
        experience.setCompany(request.getCompany());
        experience.setCity(request.getCity());
        experience.setCountry(request.getCountry());
        experience.setFrom(request.getFrom());
        experience.setTo(request.getTo());
        experience.setDescription(request.getDescription());
        return experienceRepository.save(experience);
    }

    public boolean experienceExists(AIExperience experience, UserProfile userProfile) {
        var experiences = experienceRepository.findByUserProfileId(userProfile.getId());
        for (Experience exp : experiences) {
            if (exp.isNew(experience)) {
                return true;
            }
        }
        return false;
    }

}

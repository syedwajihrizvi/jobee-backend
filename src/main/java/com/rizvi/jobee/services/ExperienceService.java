package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.experience.CreateExperienceDto;
import com.rizvi.jobee.entities.Experience;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.ExperienceNotFoundException;
import com.rizvi.jobee.exceptions.UnauthorizedException;
import com.rizvi.jobee.helpers.AISchemas.AIExperience;
import com.rizvi.jobee.repositories.ExperienceRepository;

import jakarta.transaction.Transactional;
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

    public boolean deleteExperience(Long experienceId, Long userId) {
        var experience = experienceRepository.findById(experienceId).orElse(null);
        if (experience == null) {
            throw new ExperienceNotFoundException(experienceId);
        }
        if (!experience.getUserProfile().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this experience");
        }
        experienceRepository.deleteById(experienceId);
        return true;
    }

    public Experience addExperience(CreateExperienceDto request, UserProfile userProfile) {
        var experience = Experience.builder().title(request.getTitle())
                .company(request.getCompany()).city(request.getCity())
                .country(request.getCountry()).from(request.getFrom())
                .state(request.getState())
                .to(request.getTo()).description(request.getDescription())
                .userProfile(userProfile).build();
        return experienceRepository.save(experience);
    }

    @Transactional
    public boolean addExperiencesForUserFromAISchemas(
            List<AIExperience> experiences, UserProfile userProfile) {
        for (AIExperience experience : experiences) {
            System.out.println("Processing AIExperience: " + experience);
            var id = experience.id;
            String fromYear = experience.getFromYear() != null && !experience.getFromYear().trim().isEmpty()
                    ? experience.getFromYear()
                    : null;
            String toYear = experience.getToYear() != null && !experience.getToYear().trim().isEmpty()
                    ? experience.getToYear()
                    : null;
            String title = experience.getTitle();
            String company = experience.getCompany();
            String city = experience.getCity().isEmpty() ? null : experience.getCity();
            String country = experience.getCountry().isEmpty() ? null : experience.getCountry();
            String state = experience.getState().isEmpty() ? null : experience.getState();
            String description = experience.getDescription();
            if (!id.isEmpty()) {
                CreateExperienceDto request = new CreateExperienceDto(
                        title,
                        description,
                        company,
                        state,
                        city,
                        country,
                        fromYear,
                        toYear);
                updateExperience(request, Long.parseLong(id));
            } else {
                var newExperience = Experience.builder()
                        .title(experience.getTitle())
                        .company(experience.getCompany())
                        .description(experience.getDescription())
                        .city(experience.getCity())
                        .country(experience.getCountry())
                        .state(experience.getState())
                        .from(fromYear)
                        .to(toYear)
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
        experience.setState(request.getState());
        experience.setFrom(request.getFrom());
        experience.setTo(request.getTo());
        experience.setDescription(request.getDescription());
        return experienceRepository.save(experience);
    }

}

package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.repositories.JobRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JobRecommenderService {
    private final JobRepository jobRepository;

    public List<Job> getRecommendedJobsForUser(UserProfile user) {
        // Placeholder logic for job recommendation
        // In a real-world scenario, this would involve complex algorithms and possibly
        // AI/ML models
        // Right now, just find jobs that match user's skills, interests
        var skills = user.getSkills().stream().map(s -> s.getSkill().getName().toLowerCase().trim()).toList();
        System.out.println("User skills: " + skills);
        return jobRepository.findJobsWithSkills(skills);
    }
}

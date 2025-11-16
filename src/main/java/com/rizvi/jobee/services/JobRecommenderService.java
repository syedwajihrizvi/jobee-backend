package com.rizvi.jobee.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.repositories.JobRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JobRecommenderService {
    private final JobRepository jobRepository;
    private final JobService jobService;

    public Map<Job, Long> getRecommendedJobsForUser(UserProfile user) {
        // Placeholder logic for job recommendation
        // In a real-world scenario, this would involve complex algorithms and possibly
        // AI/ML models
        // Right now, just find jobs that match user's skills, interests
        // TODO: Only return jobs that the user has not applied to yet
        Map<Job, Long> recommendedJobs = new HashMap<>();
        var skills = user.getSkills().stream().map(s -> s.getSkill().getSlug().toLowerCase().trim()).toList();
        var jobs = jobRepository.findJobsWithSkills(skills);
        for (Job job : jobs) {
            var matchScore = jobService.checkJobMatch(job.getId(), user);
            recommendedJobs.put(job, matchScore);
        }
        return recommendedJobs;
    }
}

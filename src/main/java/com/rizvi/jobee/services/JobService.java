package com.rizvi.jobee.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.job.CreateJobDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.entities.AIJobInsight;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.entities.HiringTeam;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.Tag;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AIJobDescriptionAnswer;
import com.rizvi.jobee.helpers.AISchemas.AIJobInsightAnswer;
import com.rizvi.jobee.helpers.AISchemas.GenerateAIInsightRequest;
import com.rizvi.jobee.helpers.AISchemas.GenerateAIJobDescriptionRequest;
import com.rizvi.jobee.queries.JobQuery;
import com.rizvi.jobee.repositories.AIJobInsightsRepository;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.HiringTeamRepository;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.TagRepository;
import com.rizvi.jobee.specifications.JobSpecifications;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class JobService {
    private final JobRepository jobRepository;
    private final TagRepository tagRepository;
    private final UserProfileService userProfileService;
    private final InvitationService invitationService;
    private final HiringTeamRepository hiringTeamRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final AIJobInsightsRepository aiJobInsightsRepository;
    private final AIService aiService;
    private static final int MAX_CANDIDATES_FOR_JOB = 5;

    public PaginatedResponse<Job> getAllJobs(JobQuery jobQuery, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Job> page = jobRepository.findAll(JobSpecifications.withFilters(jobQuery), pageRequest);
        var jobs = page.getContent();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        return new PaginatedResponse<>(hasMore, jobs);
    }

    public Job getJobById(Long jobId) {
        var job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job with id " + jobId + " not found");
        }
        return job;
    }

    public PaginatedResponse<Job> getJobsByCompany(JobQuery jobQuery, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Job> page = jobRepository.findAll(JobSpecifications.withFilters(jobQuery), pageRequest);
        var jobs = page.getContent();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        var totalElements = page.getTotalElements();
        return new PaginatedResponse<Job>(hasMore, jobs, totalElements);
    }

    public Job getCompanyJobById(Long jobId) {
        var job = jobRepository.findDetailedJobById(jobId).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job with id " + jobId + " not found");
        }
        return job;
    }

    public List<Job> getJobsByIds(List<Long> jobIds) {
        return jobRepository.findJobWithIdList(jobIds);
    }

    @Transactional
    public Job createJob(CreateJobDto request, BusinessAccount businessAccount) {
        var tagEntities = new ArrayList<Tag>();
        for (String tagName : request.getTags()) {
            var slugName = tagName.trim().replaceAll("[^a-zA-Z0-9 ]", "");
            var tag = tagRepository.findBySlug(slugName);
            if (tag == null) {
                tag = Tag.builder().name(tagName).slug(slugName).build();
                tag = tagRepository.save(tag);
            }
            tagEntities.add(tag);
        }

        var job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .state(request.getState())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .department(request.getDepartment())
                .employmentType(request.getEmploymentType())
                .setting(request.getSetting())
                .appDeadline(request.getAppDeadline())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .level(request.getExperience())
                .build();
        job.setBusinessAccount(businessAccount);
        for (Tag tag : tagEntities) {
            job.addTag(tag);
        }
        // Add the hiring team members
        for (var memberDto : request.getHiringTeam()) {
            var email = memberDto.getEmail();
            var firstName = memberDto.getFirstName();
            var lastName = memberDto.getLastName();
            // Create the hiring team member
            var hiringTeamMember = HiringTeam.builder().email(email).firstName(firstName).lastName(lastName).job(job)
                    .build();
            var hiringTeamMemberAccount = businessAccountRepository.findByEmail(email).orElse(null);
            if (hiringTeamMemberAccount != null) {
                hiringTeamMember.setBusinessAccount(hiringTeamMemberAccount);
                hiringTeamMember.setInvited(false);
                invitationService.sendHiringTeamInvitationEmail(hiringTeamMemberAccount, businessAccount, job);
                // TODO: Send notification Email (TODO)
            } else {
                hiringTeamMember.setInvited(true);
                invitationService.sendHiringTeamInvitationAndJoinJobeeEmail(email, businessAccount, job);
                // Send invite Email (TODO)
            }
            job.addHiringTeamMember(hiringTeamMember);
        }

        var savedJob = jobRepository.save(job);
        return savedJob;

    }

    public void incrementJobViews(Long jobId) {
        var job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found"));
        job.setViews(job.getViews() + 1);
        jobRepository.save(job);
    }

    public List<Job> getJobsByBusinessAccountIdForRecruiter(Long accountId, String search) {
        if (search != null && !search.isEmpty()) {
            return jobRepository.findByBusinessAccountIdAndTitle(accountId, search);
        }
        var sort = Sort.by("createdAt").descending();
        return jobRepository.findByBusinessAccountId(accountId, sort);
    }

    public List<Job> getJobsByBusinessAccountIdForEmployee(Long accountId, String search) {
        List<HiringTeam> teamsUserIsPartOf = new ArrayList<>();
        var sort = Sort.by("createdAt").descending();
        if (search != null && !search.isEmpty()) {
            teamsUserIsPartOf = hiringTeamRepository.findByBusinessAccountIdAndJobTitle(accountId, search, sort);
        }
        teamsUserIsPartOf = hiringTeamRepository.findByBusinessAccountId(accountId, sort);
        return teamsUserIsPartOf.stream()
                .map(team -> team.getJob())
                .toList();
    }

    public List<Job> getJobsByCompanyId(Long companyId) {
        return jobRepository.findByCompanyId(companyId);
    }

    public List<Job> getMostRecentJobsByCompany(Long companyId, Long limit) {
        var jobs = jobRepository.findMostRecentJobsByCompanyId(companyId);
        return jobs.size() > limit ? jobs.subList(0, limit.intValue()) : jobs;
    }

    public Long checkJobMatch(Long jobId, UserProfile userProfile) {
        var job = getJobById(jobId);
        var score = 0;
        var maxScore = 100;
        var userSkills = userProfile.getSkills().stream()
                .map(skill -> skill.getSkillSlug().toLowerCase())
                .toList();
        var jobTags = job.getTags().stream()
                .map(tag -> tag.getSlug().toLowerCase())
                .toList();

        if (!jobTags.isEmpty()) {
            long matchingSkills = userSkills.stream()
                    .mapToLong(userSkill -> jobTags.contains(userSkill) ? 1 : 0)
                    .sum();
            score += (int) ((matchingSkills * 40) / jobTags.size());
        }
        var totalExperience = userProfile.getExperiences().stream()
                .mapToLong(exp -> {
                    String fromYear = exp.getFrom().replace(" ", "").toLowerCase();
                    String toYear = exp.getTo() != null ? exp.getTo().replace(" ", "").toLowerCase() : "present";
                    if (fromYear != null && toYear != null && !toYear.equals("present")) {
                        return Long.parseLong(toYear) - Long.parseLong(fromYear);
                    } else if (toYear == null || toYear.equals("present")) {
                        var currentYear = String.valueOf(java.time.LocalDate.now().getYear());
                        return Long.parseLong(currentYear) - Long.parseLong(fromYear);
                    }
                    return 0L;
                })
                .sum();

        var experienceScore = job.getUserMatchWithExperience(totalExperience);
        score += (experienceScore * 30) / 100;
        var jobLocation = job.getLocation();
        List<String> userLocations = new ArrayList<>();
        userLocations.addAll(userProfile.getExperiences().stream()
                .map(exp -> exp.getCity() + ", " + exp.getCountry())
                .toList());
        userLocations.add(userProfile.getLocation());
        if (job.getSetting() != null && job.getSetting().toString().equalsIgnoreCase("REMOTE")) {
            score += 20;
        } else if (jobLocation != null && userLocations.stream()
                .anyMatch(userLoc -> userLoc != null &&
                        userLoc.toLowerCase().contains(jobLocation.toLowerCase()))) {
            score += 20;
        }
        var hasRelevantEducation = userProfile.getEducation().stream()
                .anyMatch(edu -> edu.getLevel() != null &&
                        (edu.getLevel().toString().toLowerCase().contains("bachelor") ||
                                edu.getLevel().toString().toLowerCase().contains("master") ||
                                edu.getLevel().toString().toLowerCase().contains("phd")));
        if (hasRelevantEducation) {
            score += 10;
        }
        return Long.valueOf(Math.min(score, maxScore));
    }

    public Map<UserProfile, Integer> findCandidatesForJob(Long jobId) {
        // Get the job
        // Calculate match score for each user profile
        // Return list of matching candidates sorted by match score (top 15)

        var job = getJobById(jobId);
        if (job == null) {
            throw new JobNotFoundException("Job with id " + jobId + " not found");
        }

        var userProfiles = userProfileService.getAllUserProfiles();
        PriorityQueue<int[]> topCandidates = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        for (var i = 0; i < userProfiles.size(); i++) {
            var userProfile = userProfiles.get(i);
            if (!job.hasUserApplied(userProfile.getId())) {
                var matchScore = checkJobMatch(jobId, userProfile);
                topCandidates.add(new int[] { matchScore.intValue(), i });
                if (topCandidates.size() > MAX_CANDIDATES_FOR_JOB) {
                    topCandidates.remove();
                }
            }
        }
        Map<UserProfile, Integer> result = new HashMap<>();
        // Process the top candidates
        for (int[] candidate : topCandidates) {
            var score = candidate[0];
            var userProfile = userProfiles.get(candidate[1]);
            result.put(userProfile, score);
        }
        return result;
    }

    public AIJobInsight generateAIJobInsight(Job job, Company company) {
        // Check if insight already exist for the jobs
        var jobUpdatedAt = job.getContentUpdatedAt();
        Sort sortByUpdatedAtDesc = Sort.by(Sort.Direction.DESC, "updatedAt");
        var existingInsights = aiJobInsightsRepository.findByJobId(job.getId(), sortByUpdatedAtDesc);
        AIJobInsight existingInsight = existingInsights.isEmpty() ? null : existingInsights.get(0);
        if (existingInsight != null) {
            System.out.println("SYED-DEBUG: Existing Insight Found: " + existingInsight.getId());
        } else {
            System.out.println("SYED-DEBUG: No Existing Insight Found");
        }

        var createNewInsight = existingInsight == null || existingInsight.getUpdatedAt().isBefore(jobUpdatedAt);
        if (createNewInsight) {
            System.out.println("SYED-DEBUG: Generating new AI Job Insight for job ID " + job.getId());
            GenerateAIInsightRequest request = new GenerateAIInsightRequest(job, company);
            AIJobInsightAnswer aiAnswer = aiService.generateAIJobInsight(request);
            if (existingInsight != null) {
                existingInsight.setAiAnalysis(aiAnswer.getInsights());
                var savedInsight = aiJobInsightsRepository.save(existingInsight);
                return savedInsight;
            } else {
                AIJobInsight newInsight = AIJobInsight.builder()
                        .job(job)
                        .aiAnalysis(aiAnswer.getInsights())
                        .build();
                var savedInsight = aiJobInsightsRepository.save(newInsight);
                return savedInsight;
            }
        }
        return existingInsight;
    }

    public String generateAIJobDescription(CreateJobDto request, Company company) {

        var aiRequest = new GenerateAIJobDescriptionRequest(request, company);
        AIJobDescriptionAnswer aiDescription = aiService.generateAIJobDescription(aiRequest);
        return aiDescription.getAnswer();
    }
}

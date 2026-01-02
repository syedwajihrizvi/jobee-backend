package com.rizvi.jobee.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.invitations.HiringTeamInvite;
import com.rizvi.jobee.dtos.job.CreateJobDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.entities.AIJobInsight;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.entities.HiringTeam;
import com.rizvi.jobee.entities.Invitation;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.Tag;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AIJobEnhancementAnswer;
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
    private final RequestQueue requestQueue;
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
        var company = businessAccount.getCompany();
        var job = Job.builder()
                .title(request.getTitle())
                .company(company)
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
        Set<HiringTeam> jobeeMembers = new HashSet<>();
        List<HiringTeamInvite> invitesToSend = new ArrayList<>();
        String jobTitle = request.getTitle();
        String companyName = company.getName();
        String creatorName = businessAccount.getFullName();
        for (var memberDto : request.getHiringTeam()) {
            var email = memberDto.getEmail();
            var firstName = memberDto.getFirstName();
            var lastName = memberDto.getLastName();
            var hiringTeamMember = HiringTeam.builder().email(email).firstName(firstName).lastName(lastName).job(job)
                    .build();
            var hiringTeamMemberAccount = businessAccountRepository.findByEmail(email).orElse(null);
            if (hiringTeamMemberAccount != null) {
                hiringTeamMember.setBusinessAccount(hiringTeamMemberAccount);
                hiringTeamMember.setInvited(false);
                jobeeMembers.add(hiringTeamMember);
            } else {
                hiringTeamMember.setInvited(true);
                var invitation = invitationService.createIntitationForHiringMember(hiringTeamMember, businessAccount);
                invitesToSend.add(new HiringTeamInvite(hiringTeamMember, invitation));
            }
            job.addHiringTeamMember(hiringTeamMember);
        }

        var savedJob = jobRepository.save(job);
        requestQueue.sendHiringTeamInvitationsForJob(job, jobeeMembers);
        if (invitesToSend.size() > 0) {
            requestQueue.sendHiringTeamInvitationsForJobForNonUsers(invitesToSend, jobTitle, companyName, creatorName);
        }
        return savedJob;

    }

    public Job updateJob(Long jobId, CreateJobDto request) {
        var job = jobRepository.findDetailedJobById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found"));
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
        job.clearTags();
        for (Tag tag : tagEntities) {
            job.addTag(tag);
        }
        // Update the hiring team
        Set<HiringTeam> existingMembers = new HashSet<>(job.getHiringTeamMembers());
        Set<HiringTeam> brandNewJobeeMembersToSendInvites = new HashSet<>();
        Set<HiringTeamInvite> brandNewNonJobeeMemebersToSendInvites = new HashSet<>();
        job.clearHiringTeamMembers();
        for (var memberDto : request.getHiringTeam()) {
            var email = memberDto.getEmail();
            var firstName = memberDto.getFirstName();
            var lastName = memberDto.getLastName();
            var hiringTeamMember = HiringTeam.builder().email(email).firstName(firstName).lastName(lastName).job(job)
                    .build();
            var hiringTeamMemberAccount = businessAccountRepository.findByEmail(email).orElse(null);
            if (hiringTeamMemberAccount != null) {
                hiringTeamMember.setBusinessAccount(hiringTeamMemberAccount);
                hiringTeamMember.setInvited(false);
                if (!existingMembers.stream()
                        .anyMatch(member -> member.getEmail().equalsIgnoreCase(email))) {
                    brandNewJobeeMembersToSendInvites.add(hiringTeamMember);
                }
            } else {
                hiringTeamMember.setInvited(true);
                if (!existingMembers.stream()
                        .anyMatch(member -> member.getEmail().equalsIgnoreCase(email))) {
                    var invitation = invitationService.createIntitationForHiringMember(hiringTeamMember,
                            job.getBusinessAccount());
                    brandNewNonJobeeMemebersToSendInvites.add(new HiringTeamInvite(hiringTeamMember, invitation));
                }
            }
            job.addHiringTeamMember(hiringTeamMember);
        }

        String jobTitle = request.getTitle();
        String companyName = job.getCompany().getName();
        String creatorName = job.getBusinessAccount().getFullName();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setState(request.getState());
        job.setStreetAddress(request.getStreetAddress());
        job.setCity(request.getCity());
        job.setCountry(request.getCountry());
        job.setPostalCode(request.getPostalCode());
        job.setDepartment(request.getDepartment());
        job.setEmploymentType(request.getEmploymentType());
        job.setSetting(request.getSetting());
        job.setAppDeadline(request.getAppDeadline());
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());
        job.setLevel(request.getExperience());
        var savedJob = jobRepository.save(job);
        // Send invitations to members
        requestQueue.sendHiringTeamInvitationsForJob(job, brandNewJobeeMembersToSendInvites);
        if (brandNewNonJobeeMemebersToSendInvites.size() > 0) {
            requestQueue.sendHiringTeamInvitationsForJobForNonUsers(
                    new ArrayList<>(brandNewNonJobeeMemebersToSendInvites), jobTitle, companyName, creatorName);
        }

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

    public List<Job> getJobsForNonAdminUsers(Long accountId, String search) {
        var postedJobs = getJobsByBusinessAccountIdForRecruiter(accountId, search);
        var teamJobs = getJobsByBusinessAccountIdForEmployee(accountId, search);
        Set<Job> allJobs = new HashSet<>();
        allJobs.addAll(postedJobs);
        allJobs.addAll(teamJobs);
        return new ArrayList<>(allJobs);
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
        int score = 0;
        int maxScore = 100;
        long totalExperience = userProfile.getExperiences().stream()
                .mapToLong(exp -> {
                    try {
                        String fromYear = exp.getFrom().replaceAll("\\s+", "");
                        String toYear = exp.getTo() != null
                                ? exp.getTo().replaceAll("\\s+", "")
                                : "present";

                        int from = Integer.parseInt(fromYear);
                        int to = toYear.equalsIgnoreCase("present")
                                ? java.time.LocalDate.now().getYear()
                                : Integer.parseInt(toYear);

                        return Math.max(0, to - from);
                    } catch (Exception e) {
                        return 0L;
                    }
                })
                .sum();

        int userLevel;
        if (totalExperience < 1)
            userLevel = 1; // INTERN
        else if (totalExperience < 3)
            userLevel = 2; // JUNIOR
        else if (totalExperience < 6)
            userLevel = 3; // MID
        else if (totalExperience < 9)
            userLevel = 4; // SENIOR
        else
            userLevel = 5; // LEAD

        int jobLevel;
        int minJobYears = job.convertLevelToInteger();

        if (minJobYears <= 1)
            jobLevel = 2;
        else if (minJobYears <= 3)
            jobLevel = 3;
        else if (minJobYears <= 6)
            jobLevel = 4;
        else
            jobLevel = 5;

        if (userLevel >= jobLevel)
            score += 20;
        else if (userLevel == jobLevel - 1)
            score += 14;
        else if (userLevel == jobLevel - 2)
            score += 8;

        var userSkills = userProfile.getSkills().stream()
                .map(s -> s.getSkillSlug().toLowerCase())
                .toList();

        var jobSkills = job.getTags().stream()
                .map(t -> t.getSlug().toLowerCase())
                .toList();
        int skillScore = 0;
        double skillMatchRatio = 0;

        if (!jobSkills.isEmpty() && !userSkills.isEmpty()) {
            long matches = 0;
            for (String userSkill : userSkills) {
                if (jobSkills.stream()
                        .anyMatch(jobSkill -> jobSkill.contains(userSkill) || userSkill.contains(jobSkill))) {
                    matches++;
                }

            }

            // Prevent dilution for senior jobs
            int divisor = Math.min(jobSkills.size(), 10);
            skillMatchRatio = (double) matches / divisor;

            skillScore = (int) Math.round(skillMatchRatio * 35);
        }
        score += skillScore;

        // -----------------------------
        // 6. EXPERIENCE FIT (30)
        // -----------------------------
        int experienceScore = 0;

        if (totalExperience >= minJobYears + 3)
            experienceScore = 30;
        else if (totalExperience >= minJobYears)
            experienceScore = 25;
        else if (totalExperience >= minJobYears - 1)
            experienceScore = 18;
        else if (totalExperience >= minJobYears - 3)
            experienceScore = 10;

        // Bonus if user is more senior than job
        if (userLevel > jobLevel)
            experienceScore += 3;

        System.out.println("Experience Score: " + experienceScore);

        score += Math.min(experienceScore, 30);

        // -----------------------------
        // 7. LOCATION / REMOTE (10)
        // -----------------------------
        int locationScore = 0;
        var jobLocation = job.getLocation();

        List<String> userLocations = new ArrayList<>();
        userLocations.add(userProfile.getLocation());
        userLocations.addAll(
                userProfile.getExperiences().stream()
                        .map(exp -> exp.getCity() + ", " + exp.getCountry())
                        .toList());

        if (job.getSetting() != null &&
                job.getSetting().toString().equalsIgnoreCase("REMOTE")) {
            locationScore = 10;
        } else if (jobLocation != null &&
                userLocations.stream()
                        .anyMatch(loc -> loc != null && loc.toLowerCase().contains(jobLocation.toLowerCase()))) {
            locationScore = 10;
        }
        System.out.println("Location Score: " + locationScore);
        score += locationScore;

        // -----------------------------
        // 8. EDUCATION (5)
        // -----------------------------
        int educationScore = 0;

        boolean hasBachelor = userProfile.getEducation().stream()
                .anyMatch(e -> e.getLevel() != null &&
                        e.getLevel().toString().toLowerCase().contains("bachelor"));

        boolean hasMasterOrPhd = userProfile.getEducation().stream()
                .anyMatch(e -> e.getLevel() != null &&
                        (e.getLevel().toString().toLowerCase().contains("master") ||
                                e.getLevel().toString().toLowerCase().contains("phd")));

        if (hasMasterOrPhd)
            educationScore = 5;
        else if (hasBachelor)
            educationScore = 3;
        score += educationScore;

        // -----------------------------
        // 9. HARD CAP: INSUFFICIENT SKILLS
        // -----------------------------
        if (skillMatchRatio < 0.5) {
            score = Math.min(score, 60);
        }

        // -----------------------------
        // 10. NORMALIZE
        // -----------------------------
        score = Math.min(score, maxScore);

        return (long) score;
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

    public AIJobEnhancementAnswer enhanceJobWithAI(CreateJobDto request, Company company) {
        System.out.println(request);
        var aiRequest = new GenerateAIJobDescriptionRequest(request, company);
        AIJobEnhancementAnswer aiDescription = aiService.enhanceJobCreation(aiRequest);
        return aiDescription;
    }
}

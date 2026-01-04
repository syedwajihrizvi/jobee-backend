package com.rizvi.jobee.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.Education;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.entities.UserSkill;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.EducationLevel;
import com.rizvi.jobee.enums.JobLevel;
import com.rizvi.jobee.queries.ApplicationQuery;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class ApplicantSpecification {
    public static Specification<Application> withFilters(ApplicationQuery query) {
        System.out.println(query);
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Application, Job> jobJoin = root.join("job");
            Join<Application, UserProfile> userProfileJoin = root.join("userProfile");
            if (query.getJobId() != null) {
                predicates.add(cb.equal(jobJoin.get("id"), query.getJobId()));
            }
            if (query.getSearch() != null && !query.getSearch().isEmpty()) {
                String search = query.getSearch().toLowerCase().trim();
                predicates.add(cb.or(
                        cb.like(cb.lower(userProfileJoin.get("firstName")), "%" + search + "%"),
                        cb.like(cb.lower(userProfileJoin.get("lastName")), "%" + search + "%"),
                        cb.like(cb.lower(userProfileJoin.get("country")), "%" + search + "%"),
                        cb.like(cb.lower(userProfileJoin.get("city")), "%" + search + "%"),
                        cb.like(cb.lower(userProfileJoin.get("title")), "%" + search + "%")));
            }
            if (query.getLocations() != null && !query.getLocations().isEmpty()) {
                List<Predicate> locationPredicates = new ArrayList<>();
                for (String loc : query.getLocations()) {
                    String searchLoc = "%" + loc.toLowerCase().trim().replace(" ", "") + "%";
                    locationPredicates.add(
                            cb.like(cb.lower(
                                    cb.function("replace", String.class, userProfileJoin.get("city"), cb.literal(" "),
                                            cb.literal(""))),
                                    searchLoc));
                }
                predicates.add(cb.or(locationPredicates.toArray(new Predicate[0])));
            }
            if (query.getUserProfileId() != null) {
                predicates.add(cb.equal(userProfileJoin.get("id"), query.getUserProfileId()));
            }
            if (query.getExperiences() != null && !query.getExperiences().isEmpty()) {
                try {
                    JobLevel jobLevel = JobLevel.valueOf(query.getExperiences().toUpperCase());
                    int minimumRequiredYears = getMinRequiredYearsForLevel(jobLevel);
                    int maximumRequiredYears = getMaxRequiredYearsForLevel(jobLevel);
                    predicates.add(cb.and(
                            cb.greaterThanOrEqualTo(userProfileJoin.get("experienceLevel"), minimumRequiredYears),
                            cb.lessThanOrEqualTo(userProfileJoin.get("experienceLevel"), maximumRequiredYears)));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid job level: " + query.getExperiences());
                }
            }
            if (query.getSkills() != null && !query.getSkills().isEmpty()) {
                List<Predicate> skillPredicates = new ArrayList<>();
                for (String skill : query.getSkills()) {
                    Join<UserProfile, UserSkill> userSkillsJoin = userProfileJoin.join("skills");
                    String searchSkill = "%" + skill.toLowerCase().trim().replace(" ", "") + "%";
                    skillPredicates.add(cb.like(cb.lower(userSkillsJoin.get("skill").get("name")), searchSkill));
                }
                predicates.add(cb.or(skillPredicates.toArray(new Predicate[0])));
            }
            if (query.getEducationLevel() != null && !query.getEducationLevel().isEmpty()) {
                try {
                    EducationLevel educationLevel = EducationLevel.valueOf(query.getEducationLevel());
                    Set<EducationLevel> encompassingLevels = getEducationLevelsOfAtleast(educationLevel);
                    System.out.println(query.getEducationLevel());
                    System.out.println("Encompassing Levels: " + encompassingLevels);
                    Join<UserProfile, Education> educationJoin = userProfileJoin.join("education");
                    predicates.add(educationJoin.get("level").in(encompassingLevels));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid education level: " + query.getEducationLevel());
                }
            }
            if (query.getApplicationDateRange() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        cb.literal(java.time.LocalDateTime.now().minusDays(query.getApplicationDateRange()))));
            }
            if (query.getHasCoverLetter() != null) {
                predicates.add(cb.isNotNull(root.get("coverLetterDocument")).in(query.getHasCoverLetter()));
            }
            if (query.getHasVideoIntro() != null) {
                predicates.add(cb.isNotNull(userProfileJoin.get("videoIntroUrl")).in(query.getHasVideoIntro()));
            }
            if (query.getShortlisted() != null) {
                predicates.add(cb.equal(root.get("shortlisted"), query.getShortlisted()));
            }
            if (query.getApplicationStatus() != null) {
                var status = query.getApplicationStatus();
                if (status.equals(ApplicationStatus.INTERVIEW_COMPLETED)) {
                    predicates.add(cb.notEqual(root.get("status"), ApplicationStatus.INTERVIEW_SCHEDULED));
                } else if (status.equals(ApplicationStatus.OFFER_MADE)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("status"), ApplicationStatus.OFFER_ACCEPTED),
                            cb.equal(root.get("status"), ApplicationStatus.OFFER_MADE),
                            cb.equal(root.get("status"), ApplicationStatus.OFFER_REJECTED)));
                } else {
                    predicates.add(cb.equal(root.get("status"), query.getApplicationStatus()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static int getMinRequiredYearsForLevel(JobLevel level) {
        return switch (level) {
            case INTERN, ENTRY -> 0;
            case JUNIOR_LEVEL -> 1;
            case MID_LEVEL -> 3;
            case SENIOR_LEVEL -> 7;
            case LEAD -> 10;
            default -> 0;
        };
    }

    private static int getMaxRequiredYearsForLevel(JobLevel level) {
        return switch (level) {
            case INTERN, ENTRY -> 2;
            case JUNIOR_LEVEL -> 4;
            case MID_LEVEL -> 7;
            case SENIOR_LEVEL -> Integer.MAX_VALUE;
            case LEAD -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    private static Set<EducationLevel> getEducationLevelsOfAtleast(EducationLevel level) {
        return switch (level) {
            case HIGH_SCHOOL -> Set.of(EducationLevel.HIGH_SCHOOL, EducationLevel.DIPLOMA, EducationLevel.ASSOCIATES,
                    EducationLevel.BACHELORS, EducationLevel.MASTERS, EducationLevel.PHD,
                    EducationLevel.POSTDOCTORATE);
            case DIPLOMA -> Set.of(EducationLevel.DIPLOMA, EducationLevel.ASSOCIATES, EducationLevel.BACHELORS,
                    EducationLevel.MASTERS, EducationLevel.PHD, EducationLevel.POSTDOCTORATE);
            case ASSOCIATES -> Set.of(EducationLevel.ASSOCIATES, EducationLevel.BACHELORS, EducationLevel.MASTERS,
                    EducationLevel.PHD, EducationLevel.POSTDOCTORATE);
            case BACHELORS -> Set.of(EducationLevel.BACHELORS, EducationLevel.MASTERS, EducationLevel.PHD,
                    EducationLevel.POSTDOCTORATE);
            case MASTERS -> Set.of(EducationLevel.MASTERS, EducationLevel.PHD, EducationLevel.POSTDOCTORATE);
            case PHD -> Set.of(EducationLevel.PHD, EducationLevel.POSTDOCTORATE);
            case POSTDOCTORATE -> Set.of(EducationLevel.POSTDOCTORATE);
            default -> Set.of();
        };
    }
}

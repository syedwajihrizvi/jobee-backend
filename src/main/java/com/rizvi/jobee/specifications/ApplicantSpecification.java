package com.rizvi.jobee.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.Education;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.entities.UserSkill;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.queries.ApplicationQuery;
import com.twilio.type.App;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class ApplicantSpecification {
    public static Specification<Application> withFilters(ApplicationQuery query) {
        System.out.println(query.getApplicationStatus());
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
                Join<UserProfile, Education> educationJoin = userProfileJoin.join("education");
                predicates.add(cb.equal(
                        cb.lower(educationJoin.get("level")),
                        query.getEducationLevel().toLowerCase().trim()));
            }
            if (query.getApplicationDateRange() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        cb.literal(java.time.LocalDateTime.now().minusDays(query.getApplicationDateRange()))));
            }
            if (query.getHasCoverLetter() != null) {
                predicates.add(cb.isNotNull(root.get("coverLetter")).in(query.getHasCoverLetter()));
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
                } else {
                    predicates.add(cb.equal(root.get("status"), query.getApplicationStatus()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

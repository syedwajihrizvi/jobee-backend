package com.rizvi.jobee.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.Education;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.entities.UserSkill;
import com.rizvi.jobee.queries.ApplicationQuery;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class ApplicantSpecification {
    public static Specification<Application> withFilters(ApplicationQuery query) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Application, Job> jobJoin = root.join("job");
            Join<Application, UserProfile> userProfileJoin = root.join("userProfile");
            predicates.add(cb.equal(jobJoin.get("id"), query.getJobId()));
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
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

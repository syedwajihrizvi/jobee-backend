package com.rizvi.jobee.specifications;

import com.rizvi.jobee.queries.JobQuery;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.Tag;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class JobSpecifications {
    public static Specification<Job> withFilters(JobQuery query) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Job, BusinessAccount> businessAccountJoin = root.join("businessAccount");
            Join<BusinessAccount, Company> companyJoin = businessAccountJoin.join("company");
            if (query.getSearch() != null && !query.getSearch().isEmpty()) {
                String search = query.getSearch().toLowerCase().trim();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + search + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + search + "%")));
            }
            if (query.getLocations() != null && !query.getLocations().isEmpty()) {
                List<Predicate> locationPredicates = new ArrayList<>();
                for (String loc : query.getLocations()) {
                    String searchLoc = "%" + loc.toLowerCase().trim().replace(" ", "") + "%";
                    locationPredicates.add(
                            cb.like(
                                    cb.lower(cb.function("replace", String.class, root.get("location"), cb.literal(" "),
                                            cb.literal(""))),
                                    searchLoc));
                }
                predicates.add(cb.or(locationPredicates.toArray(new Predicate[0])));
            }
            if (query.getCompanyId() != null) {
                predicates.add(cb.equal(companyJoin.get("id"), query.getCompanyId()));
            }
            if (query.getCompanies() != null && !query.getCompanies().isEmpty()) {
                for (String comp : query.getCompanies()) {
                    String searchCompany = "%" + comp.toLowerCase().trim().replace(" ", "") + "%";
                    predicates.add(cb.like(
                            cb.lower(cb.function("replace", String.class, companyJoin.get("name"), cb.literal(" "),
                                    cb.literal(""))),
                            searchCompany));
                }
                predicates.add(cb.or(predicates.toArray(new Predicate[0])));
            }
            if (query.getEmploymentTypes() != null && !query.getEmploymentTypes().isEmpty()) {
                List<Predicate> employmentTypePredicates = new ArrayList<>();
                for (String type : query.getEmploymentTypes()) {
                    employmentTypePredicates.add(cb.equal(root.get("employmentType"), type));
                }
                predicates.add(cb.or(employmentTypePredicates.toArray(new Predicate[0])));
            }
            if (query.getTags() != null && !query.getTags().isEmpty()) {
                List<Predicate> tagPredicates = new ArrayList<>();
                Join<Job, Tag> tagsJoin = root.join("tags");
                for (String tag : query.getTags()) {
                    String searchTag = "%" + tag.toLowerCase().trim().replace(" ", "") + "%";
                    tagPredicates.add(cb.like(
                            cb.lower(cb.function("replace", String.class, tagsJoin.get("name"), cb.literal(" "),
                                    cb.literal(""))),
                            searchTag));
                }
                predicates.add(cb.or(tagPredicates.toArray(new Predicate[0])));
            }
            if (query.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minSalary"), query.getMinSalary()));
            }
            if (query.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxSalary"), query.getMaxSalary()));
            }
            if (query.getMinExperience() != null && query.getMaxExperience() != null) {
                predicates.add(cb.between(root.get("experience"), query.getMinExperience(), query.getMaxExperience()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

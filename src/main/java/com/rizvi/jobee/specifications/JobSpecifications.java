package com.rizvi.jobee.specifications;

import com.rizvi.jobee.queries.JobQuery;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class JobSpecifications {
    public static Specification<Job> withFilters(JobQuery jobQuery) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (jobQuery.getSearch() != null && !jobQuery.getSearch().isEmpty()) {
                String search = jobQuery.getSearch().toLowerCase().trim();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + search + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + search + "%")));
            }
            if (jobQuery.getLocation() != null && !jobQuery.getLocation().isEmpty()) {
                String searchLocation = "%" + jobQuery.getLocation().toLowerCase().trim().replace(" ", "") + "%";
                predicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, root.get("location"), cb.literal(" "),
                                cb.literal(""))),
                        searchLocation));
            }
            if (jobQuery.getCompanyName() != null && !jobQuery.getCompanyName().isEmpty()) {
                Join<Job, BusinessAccount> businessAccountJoin = root.join("businessAccount");
                Join<BusinessAccount, Company> companyJoin = businessAccountJoin.join("company");
                String searchCompany = "%" + jobQuery.getCompanyName().toLowerCase().trim().replace(" ", "") + "%";
                predicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, companyJoin.get("name"), cb.literal(" "),
                                cb.literal(""))),
                        searchCompany));
            }
            if (jobQuery.getDistance() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("distance"), jobQuery.getDistance()));
            }
            if (jobQuery.getSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minSalary"), jobQuery.getSalary()));
            }
            if (jobQuery.getExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experience"), jobQuery.getExperience()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

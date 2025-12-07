package com.rizvi.jobee.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.queries.InterviewQuery;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class InterviewSpecifications {
    public static Specification<Interview> withFilters(InterviewQuery query) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Interview, Job> jobJoin = root.join("job");
            Join<Job, BusinessAccount> businessJoin = jobJoin.join("businessAccount");
            Join<BusinessAccount, Company> companyJoin = businessJoin.join("company");
            System.out.println("Using Query: " + query);
            predicates.add(cb.equal(companyJoin.get("id"), query.getCompanyId()));
            if (query.getJobId() != null) {
                predicates.add(cb.equal(jobJoin.get("id"), query.getJobId()));
            }

            if (query.getInterviewStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getInterviewStatus()));
            }

            if (query.getDecisionResult() != null) {
                predicates.add(
                        cb.and(
                                cb.equal(root.get("decisionResult"), query.getDecisionResult()),
                                cb.equal(root.get("status"), "COMPLETED")));
            }

            if (query.getPostedById() != null) {
                predicates.add(cb.equal(businessJoin.get("id"), query.getPostedById()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

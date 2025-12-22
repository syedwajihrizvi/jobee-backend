package com.rizvi.jobee.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.queries.CompanyMemberQuery;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class CompanyMemberSpecification {
    public static Specification<BusinessAccount> withFilters(CompanyMemberQuery query) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<BusinessAccount, Company> companyJoin = root.join("company");
            if (query.getSearch() != null && !query.getSearch().isEmpty()) {
                String search = query.getSearch().toLowerCase().trim();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), "%" + search + "%"),
                        cb.like(cb.lower(root.get("lastName")), "%" + search + "%"),
                        cb.like(cb.lower(root.get("email")), "%" + search + "%")));
            }
            if (query.getRole() != null && !query.getRole().isEmpty()) {
                predicates.add(cb.equal(root.get("accountType"), query.getRole()));
            }
            if (query.getCompanyId() != null) {
                predicates.add(cb.equal(companyJoin.get("id"), query.getCompanyId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

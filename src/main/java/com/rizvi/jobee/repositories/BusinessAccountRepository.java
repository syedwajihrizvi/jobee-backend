package com.rizvi.jobee.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import com.rizvi.jobee.entities.BusinessAccount;

public interface BusinessAccountRepository extends CrudRepository<BusinessAccount, Long> {
    Optional<BusinessAccount> findByEmail(String email);

    @EntityGraph(attributePaths = { "company", "profile", "profile.socials" })
    Optional<BusinessAccount> findById(Long id);

    @EntityGraph(attributePaths = { "profile" })
    Page<BusinessAccount> findAll(Specification<BusinessAccount> specification, Pageable pageable);

}

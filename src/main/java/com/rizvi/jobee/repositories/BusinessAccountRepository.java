package com.rizvi.jobee.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import com.rizvi.jobee.entities.BusinessAccount;

public interface BusinessAccountRepository extends CrudRepository<BusinessAccount, Long> {
    Optional<BusinessAccount> findByEmail(String email);

    @EntityGraph(attributePaths = { "company" })
    Optional<BusinessAccount> findById(Long id);

}

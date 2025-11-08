package com.rizvi.jobee.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(Long id);
}

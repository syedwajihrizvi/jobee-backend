package com.rizvi.jobee.services;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.UserAccount;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.UserAccountRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AccountService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;
    private final BusinessAccountRepository businessAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new User(
                account.getEmail(),
                account.getPassword(),
                Collections.emptyList());
    }

    public UserAccount getUserAccountById(Long accountId) {
        return userAccountRepository.findById(accountId).orElse(null);
    }

    public UserAccount getUserAccountByEmail(String email) {
        return userAccountRepository.findByEmail(email).orElse(null);
    }

    public BusinessAccount getBusinessAccountById(Long accountId) {
        return businessAccountRepository.findById(accountId).orElse(null);
    }

    public BusinessAccount getBusinessAccountByEmail(String email) {
        return businessAccountRepository.findByEmail(email).orElse(null);
    }

}

package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.CreateUserProfileDto;
import com.rizvi.jobee.dtos.UserProfileSummaryDto;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.repositories.UserAccountRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles")
public class UserProfileController {
        private final UserAccountRepository userAccountRepository;
        private final UserProfileRepository userProfileRepository;
        private final UserMapper userMapper;

        @PostMapping()
        public ResponseEntity<UserProfileSummaryDto> createUserProfile(
                        @RequestBody CreateUserProfileDto request,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                System.out.println(request.getAccountId());
                var userAccount = userAccountRepository.findById(request.getAccountId()).orElse(null);
                if (userAccount == null) {
                        throw new AccountNotFoundException(
                                        "User account with ID " + request.getAccountId() + " not found");
                }
                var userProfile = UserProfile.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .age(request.getAge())
                                .build();
                userProfile.setAccount(userAccount);
                var savedProfile = userProfileRepository.save(userProfile);
                var uri = uriComponentsBuilder.path("/profiles/{id}")
                                .buildAndExpand(savedProfile.getId()).toUri();
                var userProfileDto = userMapper.toProfileSummaryDto(savedProfile);
                return ResponseEntity.created(uri).body(userProfileDto);
        }
}

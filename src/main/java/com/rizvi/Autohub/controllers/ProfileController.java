package com.rizvi.Autohub.controllers;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.Autohub.dtos.CreateProfileDto;
import com.rizvi.Autohub.entities.Profile;
import com.rizvi.Autohub.exceptions.AccountNotFoundException;
import com.rizvi.Autohub.repositories.AccountRepository;
import com.rizvi.Autohub.repositories.ProfileRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles")
public class ProfileController {
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;

    @GetMapping()
    public void getProfiles() {
        var profiles = profileRepository.findAll();
        profiles.forEach(profile -> {
            System.out.println("Profile ID: " + profile.getId());
            System.out.println("Bio: " + profile.getBio());
            System.out.println("Profile Picture URL: " + profile.getProfilePictureUrl());
            System.out.println("Account ID: " + profile.getAccount().getId());
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable int id) throws NotFoundException {
        var profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            throw new NotFoundException();
        }
        return ResponseEntity.ok().body(profile);
    }

    @PostMapping()
    public ResponseEntity<?> createProfile(
            @RequestBody CreateProfileDto request,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException, DataIntegrityViolationException {
        var account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException());
        var profile = Profile.builder()
                .bio(request.getBio())
                .profilePictureUrl(request.getProfilePictureUrl())
                .build();
        profile.addAccountToProfile(account);
        profileRepository.save(profile);
        var uri = uriComponentsBuilder.path("/profiles/{id}").buildAndExpand(profile.getId()).toUri();
        return ResponseEntity.created(uri).body(profile);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", "Profile not found"));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotFoundException(AccountNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", "Account not found"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
        return ResponseEntity.status(400).body(Map.of("Invalid Account", "Profile with account ID already exists"));
    }
}

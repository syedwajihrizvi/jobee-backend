package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.user.JwtDto;
import com.rizvi.jobee.dtos.user.LoginDto;
import com.rizvi.jobee.dtos.user.RegisterUserAccountDto;
import com.rizvi.jobee.dtos.user.UserAccountSummaryDto;
import com.rizvi.jobee.mappers.UserMapper;
import com.rizvi.jobee.entities.UserAccount;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.exceptions.IncorrectEmailOrPasswordException;
import com.rizvi.jobee.repositories.UserAccountRepository;
import com.rizvi.jobee.services.JwtService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/accounts")
public class UserAccountController {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UserAccountSummaryDto> registerUser(
            @RequestBody RegisterUserAccountDto request,
            UriComponentsBuilder uriComponentsBuilder) {

        var password = passwordEncoder.encode(request.getPassword());
        var userAccount = UserAccount.builder().email(request.getEmail()).password(password).build();
        var userProfile = UserProfile.builder().firstName(request.getFirstName())
                .lastName(request.getLastName()).age(request.getAge()).account(userAccount).build();
        userAccount.setProfile(userProfile);
        userAccountRepository.save(userAccount);
        var userAccountDto = userMapper.toSummaryDto(userAccount);
        var uri = uriComponentsBuilder.path("/accounts/users/{id}")
                .buildAndExpand(userAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(userAccountDto);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDto> login(
            @Valid @RequestBody LoginDto request) throws RuntimeException {
        var email = request.getEmail();
        // var password = request.getPassword();
        // authenticationManager.authenticate(
        // new UsernamePasswordAuthenticationToken(email, password));
        var userAccount = userAccountRepository.findByEmail(email).orElse(null);
        if (userAccount == null) {
            throw new IncorrectEmailOrPasswordException("Invalid email or password");
        }
        var jwtToken = jwtService.generateUserToken(userAccount.getEmail(), Role.USER, userAccount.getId(),
                userAccount.getProfile().getId());
        return ResponseEntity.ok(new JwtDto(jwtToken));
    }

}

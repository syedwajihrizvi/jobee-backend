package com.rizvi.Autohub.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.AllArgsConstructor;

import com.rizvi.Autohub.repositories.AccountRepository;
import com.rizvi.Autohub.services.JwtService;
import com.rizvi.Autohub.dtos.LoginAccountDto;
import com.rizvi.Autohub.dtos.RegisterAccountDto;
import com.rizvi.Autohub.entities.Account;

@AllArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @GetMapping()
    public void getAccounts() {
        var accounts = accountRepository.findAll();
        accounts.forEach(account -> {
            System.out.println("Account ID: " + account.getId());
            System.out.println("First Name: " + account.getFirstName());
            System.out.println("Last Name: " + account.getLastName());
            System.out.println("Email: " + account.getEmail());
            System.out.println("Created At: " + account.getCreatedAt());
        });
    }

    @PostMapping
    public ResponseEntity<?> registerUser(
            @RequestBody RegisterAccountDto request,
            UriComponentsBuilder uriComponentsBuilder) {
        var encodedPassword = passwordEncoder.encode(request.getPassword());
        var account = Account.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(encodedPassword)
                .build();
        accountRepository.save(account);
        // var accountSummaryDto = accountMapper.toAccountSummaryDto(account);
        var uri = uriComponentsBuilder.path("/accounts/{id}").buildAndExpand(account.getId()).toUri();
        return ResponseEntity.created(uri).body(account);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginAccountDto request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()));

        var token = jwtService.generateToken(request.getEmail());
        // If the email and password match, return the account details
        return ResponseEntity.ok().body(Map.of("jwt", token));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }
}

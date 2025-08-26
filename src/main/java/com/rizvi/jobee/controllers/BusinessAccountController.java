package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.BusinessAccountDto;
import com.rizvi.jobee.dtos.CreateBusinessAccountDto;
import com.rizvi.jobee.dtos.JwtDto;
import com.rizvi.jobee.dtos.LoginDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Company;
import com.rizvi.jobee.enums.BusinessType;
import com.rizvi.jobee.enums.Roles;
import com.rizvi.jobee.exceptions.AlreadyRegisteredException;
import com.rizvi.jobee.exceptions.IncorrectEmailOrPasswordException;
import com.rizvi.jobee.mappers.BusinessMapper;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.CompanyRepository;
import com.rizvi.jobee.services.JwtService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/business-accounts")
public class BusinessAccountController {
    private final BusinessAccountRepository businessAccountRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BusinessMapper businessMapper;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<BusinessAccountDto> createBusinessAccount(
            @RequestBody CreateBusinessAccountDto request,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var companyName = request.getCompanyName();

        var company = companyRepository.findByName(companyName);
        if (company != null) {
            throw new AlreadyRegisteredException("Company with name " + companyName + " exists");
        }
        var password = passwordEncoder.encode(request.getPassword());
        var businessAccount = BusinessAccount.builder()
                .email(request.getEmail())
                .password(password)
                .accountType(BusinessType.ADMIN)
                .build();
        // Create the company
        var newCompany = Company.builder().name(companyName).build();
        newCompany.addBusinessAccount(businessAccount);
        var savedCompany = companyRepository.save(newCompany);
        var uri = uriComponentsBuilder.path("/business-accounts/{id}")
                .buildAndExpand(savedCompany.getId()).toUri();
        return ResponseEntity.created(uri).body(businessMapper.toDto(businessAccount));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDto> login(
            @Valid @RequestBody LoginDto request) throws RuntimeException {
        var email = request.getEmail();
        var password = request.getPassword();
        var businessAccount = businessAccountRepository.findByEmail(email).orElse(null);
        if (businessAccount == null || !passwordEncoder.matches(password, businessAccount.getPassword())) {
            throw new IncorrectEmailOrPasswordException("Invalid email or password");
        }
        var jwtToken = jwtService.generateToken(businessAccount.getEmail(), Roles.BUSINESS, businessAccount.getId());
        return ResponseEntity.ok(new JwtDto(jwtToken));
    }

}

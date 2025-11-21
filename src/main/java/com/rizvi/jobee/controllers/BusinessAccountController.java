package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.invitations.CreateInvitationDto;
import com.rizvi.jobee.dtos.user.BusinessAccountDto;
import com.rizvi.jobee.dtos.user.CreateBusinessAccountDto;
import com.rizvi.jobee.dtos.user.CreateBusinessAccountViaCodeDto;
import com.rizvi.jobee.dtos.user.JwtDto;
import com.rizvi.jobee.dtos.user.LoginDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.BusinessProfile;
import com.rizvi.jobee.enums.InvitationStatus;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.exceptions.AlreadyRegisteredException;
import com.rizvi.jobee.exceptions.IncorrectEmailOrPasswordException;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.mappers.BusinessMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.services.BusinessAccountService;
import com.rizvi.jobee.services.InvitationService;
import com.rizvi.jobee.services.JwtService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/business-accounts")
public class BusinessAccountController {
    private final BusinessAccountRepository businessAccountRepository;
    private final BusinessAccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BusinessMapper businessMapper;
    private final InvitationService invitationService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<BusinessAccountDto> createBusinessAccount(
            @RequestBody CreateBusinessAccountDto request,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        var businessAccount = accountService.createBusinessAccount(request);
        var uri = uriComponentsBuilder.path("/business-accounts/{id}")
                .buildAndExpand(businessAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(businessMapper.toDto(businessAccount));
    }

    @PostMapping("/register-via-code")
    @Transactional
    public ResponseEntity<BusinessAccountDto> createBusinessAccountViaCode(
            @RequestBody CreateBusinessAccountViaCodeDto request,
            UriComponentsBuilder uriComponentsBuilder) {
        var companyCode = request.getCompanyCode();
        var invitation = invitationService.getInvitationByCompanyCode(companyCode);
        if (invitation == null) {
            return ResponseEntity.badRequest().build();
        }
        var email = request.getEmail();
        var existingAccount = businessAccountRepository.findByEmail(email).orElse(null);
        if (existingAccount != null) {
            throw new AlreadyRegisteredException("An account with email " + email + " already exists.");
        }
        if (invitation.getEmail() != null && !invitation.getEmail().equals(email)) {
            throw new IncorrectEmailOrPasswordException(
                    "Invitation email does not match the provided email.");
        }
        var password = passwordEncoder.encode(request.getPassword());
        // Create business profile
        var businessAccount = BusinessAccount.builder()
                .email(email)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(password)
                .accountType(invitation.getInvitationType())
                .build();
        var company = invitation.getCompany();
        businessAccount.setCompany(company);
        var businesProfile = BusinessProfile.builder().title("Employee").businessAccount(businessAccount).build();
        businessAccount.setProfile(businesProfile);
        var savedBusinessAccount = businessAccountRepository.save(businessAccount);
        invitationService.updateInvitationStatus(invitation, InvitationStatus.ACCEPTED);
        var uri = uriComponentsBuilder.path("/business-accounts/{id}")
                .buildAndExpand(savedBusinessAccount.getId()).toUri();
        System.out.println("Business account created with ID: " + savedBusinessAccount.getId());
        return ResponseEntity.created(uri).body(businessMapper.toDto(savedBusinessAccount));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDto> login(
            @Valid @RequestBody LoginDto request) throws RuntimeException {
        var email = request.getEmail();
        var password = request.getPassword();
        var businessAccount = businessAccountRepository.findByEmail(email).orElse(null);
        if (businessAccount == null || !passwordEncoder.matches(password,
                businessAccount.getPassword())) {
            throw new IncorrectEmailOrPasswordException("Invalid email or password");
        }
        var jwtToken = jwtService.generateBusinessJwtToken(businessAccount.getEmail(), Role.BUSINESS,
                businessAccount.getId(),
                businessAccount.getAccountType().name(),
                businessAccount.getProfile().getId(),
                businessAccount.getCompany().getId());
        return ResponseEntity.ok(new JwtDto(jwtToken));
    }

    @GetMapping("/me")
    public ResponseEntity<BusinessAccountDto> getCurrentBusinessAccount(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var accountType = principal.getAccountType();
        var businessAccount = businessAccountRepository.findById(userId).orElse(null);
        if (businessAccount == null) {
            return ResponseEntity.notFound().build();
        }
        BusinessAccountDto dto = businessMapper.toDto(businessAccount);
        System.out.println("Fetched business account for user ID: " + dto);
        dto.setRole(accountType);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/invite-member")
    public ResponseEntity<Void> inviteBusinessAccount(
            @RequestBody CreateInvitationDto request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var userId = principal.getId();
        var accountType = principal.getAccountType();
        // TODO: Ensure user has permissions to invite members
        var businessAccount = businessAccountRepository.findById(userId).orElseThrow(
                () -> new AccountNotFoundException("Business account not found with id: " + userId));
        var email = request.getEmail();
        var phoneNumber = request.getPhoneNumber();
        var invitationType = request.getSelectedUserType();
        invitationService.createInvitation(email, phoneNumber, invitationType, businessAccount);
        return ResponseEntity.ok().build();
    }
}

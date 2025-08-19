package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.mappers.UserDocumentMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.UserDocumentService;
import com.rizvi.jobee.entities.UserDocument;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/user-documents")
public class UserDocumentController {

    private final UserDocumentService userDocumentService;
    private final UserProfileRepository userProfileRepository;
    private final UserDocumentMapper userDocumentMapper;

    @PostMapping()
    @Operation(summary = "Create a user document via file")
    public ResponseEntity<?> createUserDocumentViaFile(
            @RequestParam("document") MultipartFile document,
            @RequestParam("documentType") String documentType,
            @AuthenticationPrincipal CustomPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
        Long userId = principal.getId();
        var result = userDocumentService.uploadDocument(userId, document, UserDocumentType.valueOf(documentType));
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }
        var userProfile = userProfileRepository.findById(userId).orElseThrow(
                () -> new AccountNotFoundException("User profile not found"));
        var userDocument = UserDocument.builder().documentType(UserDocumentType.valueOf(documentType))
                .documentUrl(result).user(userProfile).build();
        userProfile.addDocument(userDocument);
        userProfileRepository.save(userProfile);
        var uri = uriComponentsBuilder.path("/user-documents/{id}")
                .buildAndExpand(userDocument.getId())
                .toUri();
        return ResponseEntity.created(uri).body(userDocumentMapper.toDto(userDocument));
    }
}

package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.UserDocumentDto;
import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.mappers.UserDocumentMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.UserDocumentRepository;
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
        private final UserDocumentRepository userDocumentRepository;
        private final UserProfileRepository userProfileRepository;
        private final UserDocumentMapper userDocumentMapper;

        @GetMapping("/user/me")
        @Operation(summary = "Get documents for the authenticated user")
        public ResponseEntity<List<UserDocumentDto>> getUserDocuments(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                Long userId = principal.getId();
                System.out.println("Fetching documents for user ID: " + userId);
                var documents = userDocumentRepository.findByUserId(userId).stream().map(userDocumentMapper::toDto)
                                .toList();
                return ResponseEntity.ok(documents);
        }

        @PostMapping()
        @Operation(summary = "Create a user document via file")
        public ResponseEntity<?> createUserDocumentViaFile(
                        @RequestParam("document") MultipartFile document,
                        @RequestParam("documentType") String documentType,
                        @AuthenticationPrincipal CustomPrincipal principal,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                Long userId = principal.getId();
                var result = userDocumentService.uploadDocument(userId, document,
                                UserDocumentType.valueOf(documentType));
                System.out.println("Upload result: " + result);
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

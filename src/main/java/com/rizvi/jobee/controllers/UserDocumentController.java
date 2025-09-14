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

import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.mappers.UserDocumentMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.UserDocumentService;
import com.rizvi.jobee.dtos.user.UserDocumentDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/user-documents")
public class UserDocumentController {

        private final UserDocumentService userDocumentService;
        private final UserProfileRepository userProfileRepository;
        private final UserDocumentMapper userDocumentMapper;

        @GetMapping("/user/me")
        @Operation(summary = "Get documents for the authenticated user")
        public ResponseEntity<List<UserDocumentDto>> getUserDocuments(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                Long userId = principal.getId();
                var documents = userDocumentService.getUserDocuments(userId);
                var documentDtos = documents.stream().map(userDocumentMapper::toDto).toList();
                return ResponseEntity.ok(documentDtos);
        }

        @PostMapping()
        @Operation(summary = "Create a user document via file")
        public ResponseEntity<?> createUserDocumentViaFile(
                        @RequestParam("document") MultipartFile document,
                        @RequestParam("documentType") String documentType,
                        @RequestParam(name = "title", required = false) String title,
                        @AuthenticationPrincipal CustomPrincipal principal,
                        UriComponentsBuilder uriComponentsBuilder) throws RuntimeException {
                Long userId = principal.getId();
                var userProfile = userProfileRepository.findById(userId).orElseThrow(
                                () -> new AccountNotFoundException("User profile not found"));
                var userDocumentType = UserDocumentType.valueOf(documentType); // Validate document type
                var createdDocument = userDocumentService.createUserDocumentViaFile(document, userDocumentType,
                                userProfile, title, false);
                if (createdDocument == null) {
                        return ResponseEntity.badRequest().build();
                }

                var uri = uriComponentsBuilder.path("/user-documents/{id}")
                                .buildAndExpand(createdDocument.getId())
                                .toUri();
                return ResponseEntity.created(uri).body(userDocumentMapper.toDto(createdDocument));
        }
}

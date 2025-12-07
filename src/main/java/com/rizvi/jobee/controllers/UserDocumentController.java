package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.enums.DocumentUrlType;
import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.mappers.UserDocumentMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.repositories.UserProfileRepository;
import com.rizvi.jobee.services.MessageFileService;
import com.rizvi.jobee.services.UserDocumentService;
import com.rizvi.jobee.dtos.user.CreateDocViaLinkDto;
import com.rizvi.jobee.dtos.user.UpdateUserDocumentRequestDto;
import com.rizvi.jobee.dtos.user.UserDocumentDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user-documents")
public class UserDocumentController {

        private final UserDocumentService userDocumentService;
        private final UserProfileRepository userProfileRepository;
        private final UserDocumentMapper userDocumentMapper;
        private final MessageFileService messageFileService;

        @GetMapping("/user/me")
        @Operation(summary = "Get documents for the authenticated user")
        public ResponseEntity<List<UserDocumentDto>> getUserDocuments(
                        @AuthenticationPrincipal CustomPrincipal principal) {
                Long userId = principal.getId();
                var documents = userDocumentService.getUserDocuments(userId);
                var documentDtos = documents.stream().map(userDocumentMapper::toDto).toList();
                return ResponseEntity.ok(documentDtos);
        }

        @PostMapping("/image")
        @Operation(summary = "Create a document via an image")
        public ResponseEntity<?> createUserDocumentViaImage(
                        @RequestParam("documentImage") MultipartFile documentImage,
                        @RequestParam("documentType") String documentType,
                        @RequestParam(name = "title", required = false) String title,
                        @AuthenticationPrincipal CustomPrincipal principal,
                        UriComponentsBuilder uriComponentsBuilder) {
                // If the documentType is a Message Attachement, we use the MessageFileService
                var userDocumentType = UserDocumentType.valueOf(documentType);
                if (userDocumentType == UserDocumentType.MESSAGE_ATTACHMENT) {
                        var conversationId = Long.valueOf(title);
                        try {
                                var uploadedFile = messageFileService.uploadMessageFile(
                                                conversationId, documentImage, principal.getId(), principal.getRole());
                                return ResponseEntity.ok(uploadedFile);
                        } catch (Exception e) {
                                return ResponseEntity.badRequest().build();
                        }
                }
                Long userId = principal.getId();
                var userProfile = userProfileRepository.findById(userId).orElseThrow(
                                () -> new AccountNotFoundException("User profile not found"));
                var createdDocument = userDocumentService.createUserDocumentViaImage(documentImage, userDocumentType,
                                userProfile, title);
                if (createdDocument == null) {
                        return ResponseEntity.badRequest().build();
                }
                var uri = uriComponentsBuilder.path("/user-documents/{id}")
                                .buildAndExpand(createdDocument.getId())
                                .toUri();
                return ResponseEntity.created(uri).body(userDocumentMapper.toDto(createdDocument));
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

                // Only users will upload documents outside of message attachments
                var userDocumentType = UserDocumentType.valueOf(documentType);
                if (userDocumentType == UserDocumentType.MESSAGE_ATTACHMENT) {
                        var conversationId = Long.valueOf(title);
                        try {
                                var uploadedFile = messageFileService.uploadMessageFile(conversationId, document,
                                                principal.getId(), principal.getRole());
                                System.out.println("SYED-DEBUG: Uploaded message attachment via file");
                                return ResponseEntity.ok(uploadedFile);
                        } catch (Exception e) {
                                return ResponseEntity.badRequest().build();
                        }
                }
                var userProfile = userProfileRepository.findById(userId).orElseThrow(
                                () -> new AccountNotFoundException("User profile not found"));
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

        @PostMapping("/link")
        @Operation(summary = "Create a user document via link")
        public ResponseEntity<UserDocumentDto> createUserDocumentViaLink(
                        @RequestBody CreateDocViaLinkDto request,
                        @AuthenticationPrincipal CustomPrincipal principal,
                        UriComponentsBuilder uriComponentsBuilder) {
                Long userId = principal.getId();
                var userProfile = userProfileRepository.findById(userId).orElseThrow(
                                () -> new AccountNotFoundException("User profile not found"));
                var documentType = UserDocumentType.valueOf(request.getDocumentType());
                var documentUrlType = DocumentUrlType.valueOf(request.getDocumentUrlType());
                var createdDocument = userDocumentService.createDocumentViaLink(
                                userProfile,
                                request.getDocumentLink(),
                                documentType,
                                request.getDocumentTitle(),
                                documentUrlType);
                if (createdDocument == null) {
                        return ResponseEntity.badRequest().build();
                }
                var uri = uriComponentsBuilder.path("/user-documents/{id}")
                                .buildAndExpand(createdDocument.getId())
                                .toUri();
                return ResponseEntity.created(uri).body(userDocumentMapper.toDto(createdDocument));
        }

        @PatchMapping("/{id}")
        @Operation(summary = "Update a user document. The title or document type")
        public ResponseEntity<UserDocumentDto> updateUserDocument(
                        @PathVariable Long id,
                        @RequestBody UpdateUserDocumentRequestDto request,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                var userProfileId = principal.getProfileId();
                var title = request.getTitle();
                UserDocumentType documentType = UserDocumentType.valueOf(request.getDocumentType());
                var updateDocument = userDocumentService.updateUserDocument(id, userProfileId, title, documentType);
                return ResponseEntity.ok(userDocumentMapper.toDto(updateDocument));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete a user document")
        public ResponseEntity<Void> deleteUserDocument(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomPrincipal principal) {
                System.out.println(
                                "SYED-DEBUG: Deleting document with ID");
                Long userId = principal.getProfileId();
                userDocumentService.deleteUserDocument(id, userId);
                return ResponseEntity.noContent().build();
        }
}

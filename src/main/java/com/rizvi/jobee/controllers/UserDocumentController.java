package com.rizvi.jobee.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.services.UserDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/user-documents")
public class UserDocumentController {

    private final UserDocumentService userDocumentService;

    // TODO: Add CustomPrincipal to get the user profile
    // Currently hardcoded to user id 1
    @PostMapping()
    @Operation(summary = "Create a user document via file")
    public ResponseEntity<Void> createUserDocumentViaFile(
            @RequestParam("document") MultipartFile document,
            @RequestParam("documentType") String documentType) {
        System.out.println("Document Data: " + document.getOriginalFilename());
        userDocumentService.uploadDocument(1L, document, UserDocumentType.valueOf(documentType));
        // Example document type
        // Get the user profile from principal and validate it exists
        // Built the user document entity
        // Add the document to the S3 Bucket
        // Add the S3 url to the document
        // Save the document to the repository
        // Add the document to the user profile's if needed

        return ResponseEntity.ok().build();
    }
}

package com.rizvi.jobee.controllers;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AlreadyRegisteredException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.CompanyNotFoundException;
import com.rizvi.jobee.exceptions.IncompleteProfileException;
import com.rizvi.jobee.exceptions.IncorrectEmailOrPasswordException;
import com.rizvi.jobee.exceptions.InvalidDocumentURLLinkException;
import com.rizvi.jobee.exceptions.SkillNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Error creating company: " + ex.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotFoundException(AccountNotFoundException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Account not found: " + ex.getMessage()));
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCompanyNotFoundException(CompanyNotFoundException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Company not found: " + ex.getMessage()));
    }

    @ExceptionHandler(IncorrectEmailOrPasswordException.class)
    public ResponseEntity<Map<String, String>> handleIncorrectEmailOrPasswordException(
            IncorrectEmailOrPasswordException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Incorrect email or password: " + ex.getMessage()));
    }

    @ExceptionHandler(AmazonS3Exception.class)
    public ResponseEntity<Map<String, String>> handleAmazonS3Exception(AmazonS3Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of("Error", "Amazon S3 error: " + ex.getMessage()));
    }

    @ExceptionHandler(SkillNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSkillNotFoundException(SkillNotFoundException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Skill not found: " + ex.getMessage()));
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyRegisteredException(AlreadyRegisteredException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Already registered: " + ex.getMessage()));
    }

    @ExceptionHandler(InvalidDocumentURLLinkException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDocumentURLLinkException(
            InvalidDocumentURLLinkException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Invalid document URL link: " + ex.getMessage()));
    }

    @ExceptionHandler(IncompleteProfileException.class)
    public ResponseEntity<Map<String, String>> handleIncompleteProfileException(
            IncompleteProfileException ex) {
        return ResponseEntity.badRequest().body(Map.of("Error", "Incomplete profile: " + ex.getMessage()));
    }
}

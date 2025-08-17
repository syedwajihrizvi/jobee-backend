package com.rizvi.jobee.controllers;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.rizvi.jobee.exceptions.AccountNotFoundException;
import com.rizvi.jobee.exceptions.AmazonS3Exception;
import com.rizvi.jobee.exceptions.CompanyNotFoundException;
import com.rizvi.jobee.exceptions.IncorrectEmailOrPasswordException;

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
}
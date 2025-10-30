package com.rizvi.jobee.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2/linkedin")
public class LinkedinController {
    @GetMapping("/callback")
    public ResponseEntity<Void> linkedInCallback(@RequestParam String code) {
        System.out.println("Received authorization code: " + code);
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }
}

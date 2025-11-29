package com.rizvi.jobee.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuthController {

    @GetMapping("/linkedin-redirect")
    public ResponseEntity<Void> linkedInCallback(@RequestParam String code) {
        System.out.println("Received authorization code: " + code);
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

    @GetMapping("/zoom-redirect")
    public ResponseEntity<?> zoomRedirect(@RequestParam String code, @RequestParam String state) {
        String deepLink = "com.syedwajihrizvi.JobeeFrontEnd://redirect?code=" + code + "&state=" + state;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", deepLink);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/webex-redirect")
    public ResponseEntity<?> webexRedirect(@RequestParam String code, @RequestParam String state) {
        System.out.println("Received Webex authorization code: " + code + ", state: " + state);
        String deepLink = "com.syedwajihrizvi.JobeeFrontEnd://redirect?code=" + code + "&state=" + state;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", deepLink);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

package com.rizvi.jobee.controllers;

import java.util.List;

import com.rizvi.jobee.dtos.message.ConversationDto;
import com.rizvi.jobee.dtos.message.MessageDto;
import com.rizvi.jobee.services.MessageService;
import com.rizvi.jobee.principals.CustomPrincipal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversationsForAuthenticatedUser(
            @AuthenticationPrincipal CustomPrincipal userPrincipal) {
        var userId = userPrincipal.getId();
        var userRole = userPrincipal.getRole();
        var conversations = messageService.getConversationsForUser(userId, userRole);
        return ResponseEntity.ok(conversations);
    }

}

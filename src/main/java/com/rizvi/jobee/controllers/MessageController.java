package com.rizvi.jobee.controllers;

import java.util.List;

import com.rizvi.jobee.dtos.message.ConversationDto;
import com.rizvi.jobee.dtos.message.ConversationMessageRequestDto;
import com.rizvi.jobee.dtos.message.MessageDto;
import com.rizvi.jobee.services.MessageService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

import com.rizvi.jobee.principals.CustomPrincipal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping()
    public ResponseEntity<List<MessageDto>> getMessagesForAuthenticatedUserWithOtherParty(
            @AuthenticationPrincipal CustomPrincipal userPrincipal,
            @RequestParam Long conversationId,
            @RequestParam Long otherPartyId,
            @RequestParam String otherPartyRole) {
        var userId = userPrincipal.getId();
        var userRole = userPrincipal.getRole();
        var requestDto = new ConversationMessageRequestDto();
        requestDto.setConversationId(conversationId);
        requestDto.setOtherPartyId(otherPartyId);
        requestDto.setOtherPartyRole(otherPartyRole);
        // Possible the conversationId is null
        var messages = messageService.getMessagesBetweenUsers(userId, userRole, requestDto);
        List<MessageDto> messageDtos = messages.stream().map(message -> {
            MessageDto messageDto = new MessageDto();
            messageDto.setId(message.getId());
            messageDto.setText(message.getText());
            messageDto.setTimestamp(message.getTimestamp());
            messageDto.setSentByUser(message.messageSentByUser(userId, userRole));
            return messageDto;
        }).toList();
        return ResponseEntity.ok(messageDtos);
    }
}

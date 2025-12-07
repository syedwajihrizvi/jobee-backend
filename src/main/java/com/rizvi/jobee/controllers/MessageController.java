package com.rizvi.jobee.controllers;

import java.util.List;

import com.rizvi.jobee.dtos.message.ConversationDto;
import com.rizvi.jobee.dtos.message.ConversationMessageRequestDto;
import com.rizvi.jobee.dtos.message.CreateConversationDto;
import com.rizvi.jobee.dtos.message.MessageDto;
import com.rizvi.jobee.entities.Conversation;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.mappers.MessageMapper;
import com.rizvi.jobee.services.MessageService;

import io.swagger.v3.oas.annotations.Operation;

import com.rizvi.jobee.principals.CustomPrincipal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;
    private final MessageMapper messageMapper;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversationsForAuthenticatedUser(
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal CustomPrincipal userPrincipal) {
        var userId = userPrincipal.getId();
        var userRole = userPrincipal.getRole();
        var conversations = messageService.getConversationsForUser(userId, userRole, search);
        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversationForAuthenticatedUser(
            @AuthenticationPrincipal CustomPrincipal userPrincipal,
            @RequestBody CreateConversationDto createConversationDto) {
        var userId = userPrincipal.getId();
        var userRole = userPrincipal.getRole();
        var otherPartyId = Long.valueOf(createConversationDto.getOtherPartyId());
        var otherPartyRole = createConversationDto.getOtherPartyRole();
        var conversation = messageService.createConversationBetweenUsers(userId, userRole, otherPartyId,
                otherPartyRole);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversationBetweenUsers")
    public ResponseEntity<Long> getConversationBetweenUsers(
            @AuthenticationPrincipal CustomPrincipal userPrincipal,
            @RequestParam String otherPartyId,
            @RequestParam String otherPartyRole) {
        var userId = userPrincipal.getId();
        var userRole = userPrincipal.getRole();
        var conversation = messageService.getConversationBetweenUsers(userId, userRole, Long.valueOf(otherPartyId),
                otherPartyRole);
        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(conversation.getId());
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
        var userType = userRole.equals("BUSINESS") || userRole.equals("ADMIN")
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        // Possible the conversationId is null
        var messages = messageService.getMessagesBetweenUsers(userId, userRole, requestDto);
        List<MessageDto> messageDtos = messages.stream()
                .map(message -> messageMapper.toMessageDto(message, userId, userType))
                .toList();
        return ResponseEntity.ok(messageDtos);
    }

    @PatchMapping("/{conversationId}/read")
    @Operation(summary = "Mark the last message in a conversation as read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long conversationId) {
        messageService.markMessageAsRead(conversationId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

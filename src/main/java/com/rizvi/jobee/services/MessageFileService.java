package com.rizvi.jobee.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.dtos.message.MessageDto;
import com.rizvi.jobee.dtos.message.MessageFileUploadDto;
import com.rizvi.jobee.entities.Conversation;
import com.rizvi.jobee.entities.Message;
import com.rizvi.jobee.enums.MessageType;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.mappers.MessageMapper;
import com.rizvi.jobee.repositories.ConversationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MessageFileService {
    private final S3Service s3Service;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public MessageFileUploadDto uploadMessageFile(Long conversationId, MultipartFile file, Long userId, String role)
            throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (!isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        String fileType = getFileTypeCategory(contentType);
        String fileUrl = s3Service.uploadMessageFile(file, userId, fileType);

        // Add the message to the conversation and send Websocket Notification
        var userType = role.equals("BUSINESS") || role.equals("ADMIN")
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        var receiverType = userType == MessagerUserType.USER
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with id: " + conversationId));
        var message = Message.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(fileType)
                .fileSize(file.getSize())
                .conversation(conversation)
                .messageType(MessageType.FILE)
                .senderType(userType)
                .receiverType(receiverType)
                .senderId(userId)
                .receiverId(conversation.getOtherPartyId(userId, userType))
                .build();
        sendInAppMessage(message);
        return MessageFileUploadDto.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(fileType)
                .fileSize(file.getSize())
                .build();
    }

    private boolean isAllowedFileType(String contentType) {
        return ALLOWED_IMAGE_TYPES.contains(contentType) || ALLOWED_DOCUMENT_TYPES.contains(contentType);
    }

    private String getFileTypeCategory(String contentType) {
        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return "image";
        } else if (ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            return "document";
        }
        return "other";
    }

    private void sendInAppMessage(Message message) {
        String receiverDest = MessageService.createMessageDestination(message.getReceiverType(),
                message.getReceiverId());
        String senderDest = MessageService.createMessageDestination(message.getSenderType(), message.getSenderId());
        var savedMessage = messageService.saveMessage(message);

        MessageDto senderMessageDto = messageMapper.toMessageDto(savedMessage, message.getSenderId(),
                message.getSenderType());
        MessageDto receiverMessageDto = messageMapper.toMessageDto(savedMessage, message.getReceiverId(),
                message.getReceiverType());
        receiverMessageDto.setSentByUser(false);
        messagingTemplate.convertAndSend(receiverDest, receiverMessageDto);
        messagingTemplate.convertAndSend(senderDest, senderMessageDto);
    }
}

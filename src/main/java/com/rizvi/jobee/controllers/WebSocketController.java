package com.rizvi.jobee.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.rizvi.jobee.dtos.message.MessageDto;
import com.rizvi.jobee.entities.Message;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.mappers.MessageMapper;
import com.rizvi.jobee.services.MessageService;
import com.rizvi.jobee.services.UserNotificationService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class WebSocketController {
        private final SimpMessagingTemplate messagingTemplate;
        private final MessageService messageService;
        private final UserNotificationService userNotificationService;
        private final MessageMapper messageMapper;

        @MessageMapping("/sendMessage")
        @SendTo("/topic/messages")
        public MessageDto sendMessage(Message message) {
                String receiverDest = "/topic/messages/" + message.getReceiverType().toString().toLowerCase() + "/"
                                + message.getReceiverId();
                String senderDest = "/topic/messages/" + message.getSenderType().toString().toLowerCase() + "/"
                                + message.getSenderId();
                Message savedMessage = messageService.saveMessage(message);
                // Since we are sending to both, we need two DTOs
                MessageDto senderMessageDto = messageMapper.toMessageDto(savedMessage, message.getSenderId(),
                                message.getSenderType());
                MessageDto receiverMessageDto = messageMapper.toMessageDto(savedMessage, message.getReceiverId(),
                                message.getReceiverType());
                receiverMessageDto.setSentByUser(false);

                messagingTemplate.convertAndSend(receiverDest, receiverMessageDto);
                messagingTemplate.convertAndSend(senderDest, senderMessageDto);
                return senderMessageDto;
        }

        @MessageMapping("/sendNotification")
        @SendTo("/topic/notifications")
        public Notification sendNotification(Notification notification) {
                System.out.println("Received notification: " + notification);
                String recepientDest = "/topic/notifications/"
                                + notification.getRecipientType().toString().toLowerCase() + "/"
                                + notification.getRecipientId();
                System.out.println("Sending notification to: " + recepientDest);
                Notification savedNotification = userNotificationService.saveNotification(notification);
                messagingTemplate.convertAndSend(recepientDest, savedNotification);
                return savedNotification;
        }
}

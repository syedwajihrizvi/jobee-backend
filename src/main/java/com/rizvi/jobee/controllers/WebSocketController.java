package com.rizvi.jobee.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.rizvi.jobee.dtos.message.MessageDto;
import com.rizvi.jobee.entities.Message;
import com.rizvi.jobee.mappers.MessageMapper;
import com.rizvi.jobee.services.MessageService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class WebSocketController {
        private final SimpMessagingTemplate messagingTemplate;
        private final MessageService messageService;
        private final MessageMapper messageMapper;

        @MessageMapping("/sendMessage")
        @SendTo("/topic/messages")
        public MessageDto sendMessage(Message message) {
                // TODO: Save message to database for persistence
                // Logic to send message
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
                System.out.println(savedMessage.toString());
                return senderMessageDto;
        }
}

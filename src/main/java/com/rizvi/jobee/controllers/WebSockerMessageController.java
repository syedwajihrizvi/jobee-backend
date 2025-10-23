package com.rizvi.jobee.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.rizvi.jobee.entities.Message;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class WebSockerMessageController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        // TODO: Save message to database for persistence
        // Logic to send message
        String destination = "/topic/messages/" + message.getReceiverType().toString().toLowerCase() + "/"
                + message.getReceiverId();
        System.out.println("Sending message to destination: " + destination);
        messagingTemplate.convertAndSend(destination, message);
        return message;
    }
}

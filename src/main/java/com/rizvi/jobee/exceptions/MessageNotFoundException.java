package com.rizvi.jobee.exceptions;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(Long messageId) {
        super("Message not found with id: " + messageId);
    }

    public MessageNotFoundException(String message) {
        super(message);
    }

}

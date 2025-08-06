package com.rizvi.jobee.exceptions;

public class IncorrectEmailOrPasswordException extends RuntimeException {
    public IncorrectEmailOrPasswordException(String message) {
        super(message);
    }
}

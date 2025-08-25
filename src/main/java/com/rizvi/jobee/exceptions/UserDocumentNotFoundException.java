package com.rizvi.jobee.exceptions;

public class UserDocumentNotFoundException extends RuntimeException {

    public UserDocumentNotFoundException(Long documentId) {
        super("User document not found with id: " + documentId);
    }

    public UserDocumentNotFoundException(String message) {
        super(message);
    }

}

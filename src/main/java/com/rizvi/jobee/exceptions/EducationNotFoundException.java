package com.rizvi.jobee.exceptions;

public class EducationNotFoundException extends RuntimeException {

    public EducationNotFoundException(Long educationId) {
        super("Education not found with id: " + educationId);
    }

    public EducationNotFoundException(String string) {
        super(string);
    }

}

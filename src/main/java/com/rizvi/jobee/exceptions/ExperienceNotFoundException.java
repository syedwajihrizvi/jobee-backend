package com.rizvi.jobee.exceptions;

public class ExperienceNotFoundException extends RuntimeException {

    public ExperienceNotFoundException(Long experienceId) {
        super("Experience not found with id: " + experienceId);
    }

    public ExperienceNotFoundException(String string) {
        super(string);
    }

}

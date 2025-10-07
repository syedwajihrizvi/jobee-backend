package com.rizvi.jobee.exceptions;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(Long projectId) {
        super("Project with ID " + projectId + " not found.");
    }

}

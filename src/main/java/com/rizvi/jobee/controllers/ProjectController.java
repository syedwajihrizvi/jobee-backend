package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rizvi.jobee.dtos.project.ProjectDto;
import com.rizvi.jobee.mappers.ProjectMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @GetMapping("/my-projects")
    @Operation(summary = "Get all projects for authenticated user")
    public ResponseEntity<List<ProjectDto>> getAllProjectsForUser(
            @AuthenticationPrincipal CustomPrincipal principal) {
        var id = principal.getId();
        var projects = projectService.getProjectsByUserId(id).stream()
                .map(projectMapper::toProjectDto)
                .toList();
        return ResponseEntity.ok(projects);
    }
}

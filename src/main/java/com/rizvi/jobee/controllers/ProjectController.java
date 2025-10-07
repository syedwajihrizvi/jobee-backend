package com.rizvi.jobee.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rizvi.jobee.dtos.project.CreateProjectDto;
import com.rizvi.jobee.dtos.project.ProjectDto;
import com.rizvi.jobee.mappers.ProjectMapper;
import com.rizvi.jobee.principals.CustomPrincipal;
import com.rizvi.jobee.services.ProjectService;
import com.rizvi.jobee.services.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/profiles/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserProfileService userProfileService;
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

    @PostMapping
    @Operation(summary = "Add project to user profile")
    public ResponseEntity<ProjectDto> addProject(
            @RequestBody CreateProjectDto createProjectDto,
            @AuthenticationPrincipal CustomPrincipal principal,
            UriComponentsBuilder uriComponentsBuilder) {
        var userId = principal.getId();
        System.out.println(createProjectDto);
        var userProfile = userProfileService.getUserProfileById(userId);
        var savedProject = projectService.createProject(createProjectDto, userProfile);
        var uri = uriComponentsBuilder.path("/profiles/experience/{id}")
                .buildAndExpand(savedProject.getId())
                .toUri();
        return ResponseEntity.created(uri).body(projectMapper.toProjectDto(savedProject));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a project in user profile")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable Long id,
            @RequestBody CreateProjectDto updateProjectDto,
            @AuthenticationPrincipal CustomPrincipal principal) {
        var updatedProject = projectService.updateProject(id, updateProjectDto);
        return ResponseEntity.ok(projectMapper.toProjectDto(updatedProject));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project from user profile")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomPrincipal principal) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}

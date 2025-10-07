package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.project.CreateProjectDto;
import com.rizvi.jobee.entities.Project;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.ProjectNotFoundException;
import com.rizvi.jobee.repositories.ProjectRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findByUserId(userId);
    }

    public Project createProject(CreateProjectDto createProjectDto, UserProfile userProfile) {
        System.out.println(createProjectDto);
        var project = Project.builder()
                .name(createProjectDto.getName())
                .description(createProjectDto.getDescription())
                .link(createProjectDto.getLink())
                .yearCompleted(createProjectDto.getYearCompleted())
                .userProfile(userProfile)
                .build();
        return projectRepository.save(project);
    }

    public Project updateProject(Long projectId, CreateProjectDto updateProjectDto) {
        var project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        project.setName(updateProjectDto.getName());
        project.setDescription(updateProjectDto.getDescription());
        project.setLink(updateProjectDto.getLink());
        project.setYearCompleted(updateProjectDto.getYearCompleted());
        return projectRepository.save(project);
    }

    public void deleteProject(Long projectId) {
        var project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        projectRepository.delete(project);
    }
}

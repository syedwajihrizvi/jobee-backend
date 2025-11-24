package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.project.CreateProjectDto;
import com.rizvi.jobee.entities.Project;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.ProjectNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AIProject;
import com.rizvi.jobee.repositories.ProjectRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserProfileRepository userProfileRepository;

    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findByUserId(userId);
    }

    public Project createProject(CreateProjectDto createProjectDto, UserProfile userProfile) {
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

    @Transactional
    public boolean createProjectsForUserFromAISchemas(
            List<AIProject> projects, UserProfile userProfile) {
        for (AIProject project : projects) {
            System.out.println("Processing AIProject: " + project);
            var id = project.id;
            var title = project.title;
            var description = project.description;
            var yearCompleted = project.yearCompleted;
            var link = project.link;

            if (!id.isEmpty()) {
                CreateProjectDto request = new CreateProjectDto(
                        title,
                        description,
                        link,
                        yearCompleted);
                updateProject(Long.parseLong(id), request);
            } else {
                var newProject = Project.builder()
                        .name(title)
                        .description(description)
                        .yearCompleted(yearCompleted)
                        .link(link)
                        .userProfile(userProfile)
                        .build();
                userProfile.addProject(newProject);
                projectRepository.save(newProject);
            }

        }
        userProfileRepository.save(userProfile);
        return true;
    }

}

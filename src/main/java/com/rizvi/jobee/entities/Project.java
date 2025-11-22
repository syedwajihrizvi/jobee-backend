package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import com.rizvi.jobee.helpers.AISchemas.AIProject;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "user_projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "project_name", nullable = false)
    private String name;

    @Column(name = "project_description")
    private String description;

    @Column(name = "project_link")
    private String link;

    @Column(name = "year_completed")
    private String yearCompleted;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;
    // TODO: Add images or videos for project as well

    public boolean isNew(AIProject project) {
        return this.name.equals(project.getTitle()) &&
                this.description.equals(project.getDescription()) &&
                ((this.yearCompleted == null && project.getYearCompleted() == null) ||
                        (this.yearCompleted != null && this.yearCompleted.equals(project.getYearCompleted())))
                &&
                ((this.link == null && project.getLink() == null) ||
                        (this.link != null && this.link.equals(project.getLink())));
    }
}

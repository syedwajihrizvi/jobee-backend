package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select p from Project p where p.userProfile.id = :userId")
    List<Project> findByUserId(Long userId);

}

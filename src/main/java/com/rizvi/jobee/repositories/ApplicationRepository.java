package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rizvi.jobee.entities.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // Additional query methods can be defined here if needed

}

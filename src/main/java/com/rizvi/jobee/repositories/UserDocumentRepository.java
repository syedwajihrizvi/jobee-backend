package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.UserDocument;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    // Define methods for CRUD operations if needed

}

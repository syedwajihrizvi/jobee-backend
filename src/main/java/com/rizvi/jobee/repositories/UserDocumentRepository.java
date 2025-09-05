package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.UserDocument;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    // Define methods for CRUD operations if needed
    @Query("select d from UserDocument d where d.user.id = :userId")
    List<UserDocument> findByUserId(Long userId);
}

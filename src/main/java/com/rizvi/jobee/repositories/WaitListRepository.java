package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.WaitListEntry;
import java.util.List;

public interface WaitListRepository extends JpaRepository<WaitListEntry, Long> {
    List<WaitListEntry> findByEmailAndCompany(String email, String company);
}

package com.rizvi.Autohub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.Autohub.entities.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    // Additional query methods can be defined here if needed

}

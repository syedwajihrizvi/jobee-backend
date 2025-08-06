package com.rizvi.jobee.repositories;

import org.springframework.data.repository.CrudRepository;

import com.rizvi.jobee.entities.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {

}

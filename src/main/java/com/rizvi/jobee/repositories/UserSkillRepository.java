package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.UserSkill;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    @EntityGraph(attributePaths = { "skill", "userProfile" })
    @Query("select us from UserSkill us where us.userProfile.id = :userProfileId and us.skill.id = :skillId")
    UserSkill findByUserProfileIdAndSkillId(Long userProfileId, Long skillId);
}

package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rizvi.jobee.entities.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    @Query(value = """
            SELECT * FROM skills WHERE LOWER(REGEXP_REPLACE(name, '[^a-z0-9]', '', 'g')) = LOWER(REGEXP_REPLACE(:name, '[^a-z0-9]', '', 'g'))""", nativeQuery = true)
    Skill findByNameLike(@Param("name") String name);
}

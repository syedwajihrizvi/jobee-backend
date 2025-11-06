package com.rizvi.jobee.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query(value = """
                SELECT * FROM tags WHERE regexp_replace(name, '[^a-zA-Z0-9 ]', '', 'g') =
                regexp_replace(?1, '[^a-zA-Z0-9 ]', '', 'g')
            """, nativeQuery = true)
    Tag findByName(String name);

    Tag findBySlug(String slug);
}

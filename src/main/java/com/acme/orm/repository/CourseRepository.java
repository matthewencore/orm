package com.acme.orm.repository;

import com.acme.orm.domain.Course;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
        select distinct c from Course c
        left join fetch c.modules m
        where c.id = :id
        """)
    Optional<Course> findDetailedById(@Param("id") Long id);
}


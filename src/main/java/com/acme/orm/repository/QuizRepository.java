package com.acme.orm.repository;

import com.acme.orm.domain.Quiz;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByModuleId(Long moduleId);

    @Query("""
        select distinct q from Quiz q
        left join fetch q.questions qu
        left join fetch qu.options
        where q.id = :id
        """)
    Optional<Quiz> findDetailedById(@Param("id") Long id);
}


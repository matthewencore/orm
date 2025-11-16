package com.acme.orm.repository;

import com.acme.orm.domain.Assignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByLessonModuleCourseId(Long courseId);
}


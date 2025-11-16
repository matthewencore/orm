package com.acme.orm.repository;

import com.acme.orm.domain.CourseReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    List<CourseReview> findByCourseId(Long courseId);

    Optional<CourseReview> findByCourseIdAndStudentId(Long courseId, Long studentId);
}


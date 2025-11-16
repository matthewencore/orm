package com.acme.orm.repository;

import com.acme.orm.domain.QuizSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    List<QuizSubmission> findByStudentId(Long studentId);

    List<QuizSubmission> findByQuizModuleCourseId(Long courseId);
}


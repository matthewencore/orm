package com.acme.orm.repository;

import com.acme.orm.domain.Submission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<Submission> findByStudentId(Long studentId);

    List<Submission> findByAssignmentLessonId(Long lessonId);
}


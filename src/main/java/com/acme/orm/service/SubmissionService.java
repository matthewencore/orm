package com.acme.orm.service;

import com.acme.orm.domain.Assignment;
import com.acme.orm.domain.Submission;
import com.acme.orm.domain.User;
import com.acme.orm.repository.SubmissionRepository;
import com.acme.orm.service.exception.BusinessException;
import com.acme.orm.service.exception.NotFoundException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentService assignmentService;
    private final UserService userService;

    @Transactional
    public Submission submitAssignment(Long assignmentId, Long studentId, String content) {
        if (submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).isPresent()) {
            throw new BusinessException("Student already submitted this assignment");
        }

        Assignment assignment = assignmentService.getAssignment(assignmentId);
        User student = userService.getUserOrThrow(studentId);

        Submission submission = Submission.builder()
            .assignment(assignment)
            .student(student)
            .submittedAt(Instant.now())
            .content(content)
            .build();

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission gradeSubmission(Long submissionId, Integer score, String feedback) {
        Submission submission = submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Submission %d not found".formatted(submissionId)));

        submission.setScore(score);
        submission.setFeedback(feedback);
        return submission;
    }

    @Transactional(readOnly = true)
    public List<Submission> findByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }
}


package com.acme.orm.service;

import com.acme.orm.domain.Assignment;
import com.acme.orm.domain.Lesson;
import com.acme.orm.repository.AssignmentRepository;
import com.acme.orm.repository.LessonRepository;
import com.acme.orm.service.exception.NotFoundException;
import com.acme.orm.web.dto.CreateAssignmentRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public Assignment createAssignment(Long lessonId, CreateAssignmentRequest request) {
        Lesson lesson = lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Lesson %d not found".formatted(lessonId)));

        Assignment assignment = Assignment.builder()
            .lesson(lesson)
            .title(request.title())
            .description(request.description())
            .dueDate(request.dueDate())
            .maxScore(request.maxScore())
            .build();

        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public Assignment getAssignment(Long id) {
        return assignmentRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsForCourse(Long courseId) {
        return assignmentRepository.findByLessonModuleCourseId(courseId);
    }
}


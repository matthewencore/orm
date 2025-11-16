package com.acme.orm.service;

import com.acme.orm.domain.Course;
import com.acme.orm.domain.Enrollment;
import com.acme.orm.domain.User;
import com.acme.orm.domain.enums.EnrollmentStatus;
import com.acme.orm.repository.EnrollmentRepository;
import com.acme.orm.service.exception.BusinessException;
import com.acme.orm.service.exception.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public Enrollment enroll(Long courseId, Long studentId) {
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("Student already enrolled in course");
        }
        Course course = courseService.getCourse(courseId);
        User student = userService.getUserOrThrow(studentId);

        Enrollment enrollment = Enrollment.builder()
            .course(course)
            .student(student)
            .enrolledAt(Instant.now())
            .status(EnrollmentStatus.ACTIVE)
            .build();

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenroll(Long enrollmentId) {
        enrollmentRepository.deleteById(enrollmentId);
    }

    @Transactional(readOnly = true)
    public List<User> getStudentsForCourse(Long courseId) {
        List<User> students = enrollmentRepository
            .findByCourseId(courseId)
            .stream()
            .map(Enrollment::getStudent)
            .collect(Collectors.toList());

        // Инициализируем необходимые поля внутри транзакции, чтобы избежать LazyInitializationException
        students.forEach(student -> {
            if (student != null) {
                student.getName();
                student.getEmail();
                student.getRole();
            }
        });

        return students;
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesForStudent(Long studentId) {
        return enrollmentRepository
            .findByStudentId(studentId)
            .stream()
            .map(Enrollment::getCourse)
            .collect(Collectors.toList());
    }

    @Transactional
    public Enrollment updateStatus(Long courseId, Long studentId, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository
            .findByCourseIdAndStudentId(courseId, studentId)
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        enrollment.setStatus(status);
        return enrollment;
    }
}


package com.acme.orm.web;

import com.acme.orm.domain.Course;
import com.acme.orm.domain.Enrollment;
import com.acme.orm.domain.User;
import com.acme.orm.service.CourseService;
import com.acme.orm.service.EnrollmentService;
import com.acme.orm.web.dto.CourseDetailsResponse;
import com.acme.orm.web.dto.CourseSummary;
import com.acme.orm.web.dto.CreateCourseRequest;
import com.acme.orm.web.dto.EnrollRequest;
import com.acme.orm.web.dto.EnrollmentResponse;
import com.acme.orm.web.dto.UserSummary;
import com.acme.orm.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @PostMapping("/courses")
    public ResponseEntity<CourseDetailsResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        Course course = courseService.createCourse(request);
        CourseDetailsResponse response = DtoMapper.toCourseDetails(courseService.getCourseDetails(course.getId()));
        return ResponseEntity
            .created(URI.create("/api/courses/" + course.getId()))
            .body(response);
    }

    @GetMapping("/courses/{courseId}")
    public CourseDetailsResponse getCourse(@PathVariable Long courseId) {
        return DtoMapper.toCourseDetails(courseService.getCourseDetails(courseId));
    }

    @GetMapping("/courses")
    public List<CourseSummary> listCourses() {
        return courseService.getAllCourses()
            .stream()
            .map(DtoMapper::toCourseSummary)
            .toList();
    }

    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<EnrollmentResponse> enroll(
        @PathVariable Long courseId,
        @Valid @RequestBody EnrollRequest request
    ) {
        Enrollment enrollment = enrollmentService.enroll(courseId, request.studentId());
        EnrollmentResponse response = new EnrollmentResponse(
            enrollment.getId(),
            enrollment.getCourse().getId(),
            enrollment.getStudent().getId(),
            enrollment.getEnrolledAt(),
            enrollment.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<Void> unenroll(@PathVariable Long enrollmentId) {
        enrollmentService.unenroll(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/students")
    public List<UserSummary> listStudents(@PathVariable Long courseId) {
        List<User> students = enrollmentService.getStudentsForCourse(courseId);
        return students.stream().map(DtoMapper::toUserSummary).collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/courses")
    public List<CourseSummary> listCoursesForStudent(@PathVariable Long userId) {
        return enrollmentService.getCoursesForStudent(userId)
            .stream()
            .map(DtoMapper::toCourseSummary)
            .collect(Collectors.toList());
    }
}


package com.acme.orm.web.mapper;

import com.acme.orm.domain.Assignment;
import com.acme.orm.domain.Course;
import com.acme.orm.domain.Lesson;
import com.acme.orm.domain.Module;
import com.acme.orm.domain.QuizSubmission;
import com.acme.orm.domain.Submission;
import com.acme.orm.domain.User;
import com.acme.orm.web.dto.CourseDetailsResponse;
import com.acme.orm.web.dto.CourseSummary;
import com.acme.orm.web.dto.QuizResultResponse;
import com.acme.orm.web.dto.SubmissionResponse;
import com.acme.orm.web.dto.UserSummary;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static CourseDetailsResponse toCourseDetails(Course course) {
        List<CourseDetailsResponse.ModuleDto> modules = course.getModules()
            .stream()
            .sorted(Comparator.comparing(module -> module.getOrderIndex() == null ? 0 : module.getOrderIndex()))
            .map(DtoMapper::toModuleDto)
            .toList();

        List<String> tags = course.getTags()
            .stream()
            .map(tag -> tag.getName())
            .sorted()
            .toList();

        return new CourseDetailsResponse(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getCategory() != null ? course.getCategory().getName() : null,
            course.getTeacher() != null ? course.getTeacher().getName() : null,
            course.getStartDate(),
            course.getDurationDays(),
            tags,
            modules
        );
    }

    public static CourseSummary toCourseSummary(Course course) {
        return new CourseSummary(
            course.getId(),
            course.getTitle(),
            course.getCategory() != null ? course.getCategory().getName() : null,
            course.getStartDate(),
            course.getDurationDays()
        );
    }

    public static UserSummary toUserSummary(User user) {
        return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public static SubmissionResponse toSubmissionResponse(Submission submission) {
        return new SubmissionResponse(
            submission.getId(),
            submission.getAssignment().getId(),
            submission.getStudent().getId(),
            submission.getSubmittedAt(),
            submission.getContent(),
            submission.getScore(),
            submission.getFeedback()
        );
    }

    public static QuizResultResponse toQuizResultResponse(QuizSubmission submission) {
        return new QuizResultResponse(
            submission.getId(),
            submission.getQuiz().getId(),
            submission.getStudent().getId(),
            submission.getScore(),
            submission.getPassed(),
            submission.getTakenAt()
        );
    }

    private static CourseDetailsResponse.ModuleDto toModuleDto(Module module) {
        List<CourseDetailsResponse.LessonDto> lessons = module.getLessons()
            .stream()
            .sorted(Comparator.comparing(Lesson::getId))
            .map(DtoMapper::toLessonDto)
            .toList();
        return new CourseDetailsResponse.ModuleDto(module.getId(), module.getTitle(), module.getOrderIndex(), lessons);
    }

    private static CourseDetailsResponse.LessonDto toLessonDto(Lesson lesson) {
        List<CourseDetailsResponse.AssignmentDto> assignments = lesson.getAssignments()
            .stream()
            .map(DtoMapper::toAssignmentDto)
            .collect(Collectors.toList());
        return new CourseDetailsResponse.LessonDto(
            lesson.getId(),
            lesson.getTitle(),
            lesson.getContent(),
            lesson.getVideoUrl(),
            assignments
        );
    }

    private static CourseDetailsResponse.AssignmentDto toAssignmentDto(Assignment assignment) {
        return new CourseDetailsResponse.AssignmentDto(
            assignment.getId(),
            assignment.getTitle(),
            assignment.getDescription(),
            assignment.getDueDate(),
            assignment.getMaxScore()
        );
    }
}


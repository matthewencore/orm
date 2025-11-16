package com.acme.orm.web.dto;

import java.time.LocalDate;
import java.util.List;

public record CourseDetailsResponse(
    Long id,
    String title,
    String description,
    String category,
    String teacher,
    LocalDate startDate,
    Integer durationDays,
    List<String> tags,
    List<ModuleDto> modules
) {

    public record ModuleDto(
        Long id,
        String title,
        Integer orderIndex,
        List<LessonDto> lessons
    ) {
    }

    public record LessonDto(
        Long id,
        String title,
        String content,
        String videoUrl,
        List<AssignmentDto> assignments
    ) {
    }

    public record AssignmentDto(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        Integer maxScore
    ) {
    }
}


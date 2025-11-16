package com.acme.orm.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreateCourseRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull Long categoryId,
    @NotNull Long teacherId,
    LocalDate startDate,
    Integer durationDays,
    List<Long> tagIds,
    @Valid List<ModuleRequest> modules
) {

    public record ModuleRequest(
        @NotBlank String title,
        @NotNull Integer orderIndex,
        @Valid List<LessonRequest> lessons
    ) {
    }

    public record LessonRequest(
        @NotBlank String title,
        String content,
        String videoUrl
    ) {
    }
}


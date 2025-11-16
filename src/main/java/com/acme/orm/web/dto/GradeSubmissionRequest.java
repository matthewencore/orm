package com.acme.orm.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradeSubmissionRequest(
    @NotNull @Min(0) @Max(100) Integer score,
    String feedback
) {
}


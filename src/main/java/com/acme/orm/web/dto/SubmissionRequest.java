package com.acme.orm.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmissionRequest(
    @NotNull Long studentId,
    @NotBlank String content
) {
}


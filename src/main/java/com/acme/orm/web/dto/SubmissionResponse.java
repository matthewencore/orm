package com.acme.orm.web.dto;

import java.time.Instant;

public record SubmissionResponse(
    Long id,
    Long assignmentId,
    Long studentId,
    Instant submittedAt,
    String content,
    Integer score,
    String feedback
) {
}


package com.acme.orm.web.dto;

import java.time.Instant;

public record QuizResultResponse(
    Long id,
    Long quizId,
    Long studentId,
    Integer score,
    Boolean passed,
    Instant takenAt
) {
}


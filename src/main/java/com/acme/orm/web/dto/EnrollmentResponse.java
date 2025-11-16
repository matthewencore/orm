package com.acme.orm.web.dto;

import com.acme.orm.domain.enums.EnrollmentStatus;
import java.time.Instant;

public record EnrollmentResponse(
    Long id,
    Long courseId,
    Long studentId,
    Instant enrolledAt,
    EnrollmentStatus status
) {
}


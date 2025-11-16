package com.acme.orm.web.dto;

import java.time.LocalDate;

public record CourseSummary(
    Long id,
    String title,
    String category,
    LocalDate startDate,
    Integer durationDays
) {
}


package com.acme.orm.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record QuizAttemptRequest(
    @NotNull Long studentId,
    @Valid @NotEmpty List<AnswerSelection> answers
) {

    public record AnswerSelection(
        @NotNull Long questionId,
        @NotEmpty List<Long> optionIds
    ) {
    }
}


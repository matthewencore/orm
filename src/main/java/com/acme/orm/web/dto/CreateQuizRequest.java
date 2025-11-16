package com.acme.orm.web.dto;

import com.acme.orm.domain.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateQuizRequest(
    @NotBlank String title,
    Integer timeLimitMinutes,
    @Valid @NotEmpty List<QuestionRequest> questions
) {

    public record QuestionRequest(
        @NotBlank String text,
        @NotNull QuestionType type,
        @Valid @NotEmpty List<AnswerRequest> options
    ) {
    }

    public record AnswerRequest(
        @NotBlank String text,
        boolean correct
    ) {
    }
}


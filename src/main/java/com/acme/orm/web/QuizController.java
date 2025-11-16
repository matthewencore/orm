package com.acme.orm.web;

import com.acme.orm.domain.Quiz;
import com.acme.orm.domain.QuizSubmission;
import com.acme.orm.service.QuizService;
import com.acme.orm.web.dto.CreateQuizRequest;
import com.acme.orm.web.dto.QuizAttemptRequest;
import com.acme.orm.web.dto.QuizResultResponse;
import com.acme.orm.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/modules/{moduleId}/quiz")
    public ResponseEntity<Long> createQuiz(
        @PathVariable Long moduleId,
        @Valid @RequestBody CreateQuizRequest request
    ) {
        Quiz quiz = quizService.createQuiz(moduleId, request);
        return ResponseEntity.created(URI.create("/api/quizzes/" + quiz.getId())).body(quiz.getId());
    }

    @PostMapping("/quizzes/{quizId}/attempts")
    public QuizResultResponse takeQuiz(
        @PathVariable Long quizId,
        @Valid @RequestBody QuizAttemptRequest request
    ) {
        QuizSubmission submission = quizService.takeQuiz(quizId, request);
        return DtoMapper.toQuizResultResponse(submission);
    }

    @GetMapping("/users/{userId}/quiz-results")
    public List<QuizResultResponse> listQuizResults(@PathVariable Long userId) {
        return quizService.getResultsForStudent(userId)
            .stream()
            .map(DtoMapper::toQuizResultResponse)
            .collect(Collectors.toList());
    }
}


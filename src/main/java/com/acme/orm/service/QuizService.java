package com.acme.orm.service;

import com.acme.orm.domain.AnswerOption;
import com.acme.orm.domain.Module;
import com.acme.orm.domain.Question;
import com.acme.orm.domain.Quiz;
import com.acme.orm.domain.QuizSubmission;
import com.acme.orm.domain.User;
import com.acme.orm.repository.ModuleRepository;
import com.acme.orm.repository.QuizRepository;
import com.acme.orm.repository.QuizSubmissionRepository;
import com.acme.orm.service.exception.BusinessException;
import com.acme.orm.service.exception.NotFoundException;
import com.acme.orm.web.dto.CreateQuizRequest;
import com.acme.orm.web.dto.QuizAttemptRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final ModuleRepository moduleRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserService userService;

    @Transactional
    public Quiz createQuiz(Long moduleId, CreateQuizRequest request) {
        Module module = moduleRepository
            .findById(moduleId)
            .orElseThrow(() -> new NotFoundException("Module %d not found".formatted(moduleId)));

        if (module.getQuiz() != null) {
            throw new BusinessException("Module already has a quiz");
        }

        Quiz quiz = Quiz.builder()
            .module(module)
            .title(request.title())
            .timeLimitMinutes(request.timeLimitMinutes())
            .build();

        List<Question> questions = request.questions()
            .stream()
            .map(questionRequest -> {
                Question question = Question.builder()
                    .quiz(quiz)
                    .text(questionRequest.text())
                    .type(questionRequest.type())
                    .build();

                List<AnswerOption> options = questionRequest.options()
                    .stream()
                    .map(answerRequest -> AnswerOption.builder()
                        .question(question)
                        .text(answerRequest.text())
                        .correct(answerRequest.correct())
                        .build())
                    .toList();
                question.setOptions(options);
                return question;
            })
            .toList();

        quiz.setQuestions(questions);
        module.setQuiz(quiz);
        return quizRepository.save(quiz);
    }

    @Transactional
    public QuizSubmission takeQuiz(Long quizId, QuizAttemptRequest request) {
        Quiz quiz = quizRepository
            .findDetailedById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz %d not found".formatted(quizId)));

        User student = userService.getUserOrThrow(request.studentId());

        Map<Long, Set<Long>> providedAnswers = request.answers()
            .stream()
            .collect(Collectors.toMap(
                QuizAttemptRequest.AnswerSelection::questionId,
                selection -> Set.copyOf(selection.optionIds())));

        long correctAnswers = quiz.getQuestions()
            .stream()
            .filter(question -> {
                Set<Long> correctOptionIds = question.getOptions()
                    .stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());

                Set<Long> selected = providedAnswers.getOrDefault(question.getId(), Set.of());

                return !correctOptionIds.isEmpty() && selected.equals(correctOptionIds);
            })
            .count();

        int score = quiz.getQuestions().isEmpty()
            ? 0
            : (int) Math.round((correctAnswers * 100.0) / quiz.getQuestions().size());
        boolean passed = score >= 70;

        QuizSubmission submission = QuizSubmission.builder()
            .quiz(quiz)
            .student(student)
            .score(score)
            .takenAt(Instant.now())
            .passed(passed)
            .build();

        return quizSubmissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> getResultsForStudent(Long studentId) {
        return quizSubmissionRepository.findByStudentId(studentId);
    }
}


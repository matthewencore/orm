package com.acme.orm.repository;

import com.acme.orm.domain.AnswerOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    List<AnswerOption> findByQuestionQuizId(Long quizId);
}



package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.AiQuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiQuestionAnswerRepository extends JpaRepository<AiQuestionAnswer, Long> {
    AiQuestionAnswer findAiQuestionAnswerById(Long questionId);
}

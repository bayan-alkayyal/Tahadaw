package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.AiGeneratedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGeneratedQuestionRepository extends JpaRepository<AiGeneratedQuestion, Long> {
    AiGeneratedQuestion findAiGeneratedQuestionById(Long questionId);
}

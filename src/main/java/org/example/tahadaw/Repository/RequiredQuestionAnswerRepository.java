package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.RequiredQuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequiredQuestionAnswerRepository extends JpaRepository<RequiredQuestionAnswer, Long> {
    RequiredQuestionAnswer findRequiredQuestionAnswerById(Long answerId);
}

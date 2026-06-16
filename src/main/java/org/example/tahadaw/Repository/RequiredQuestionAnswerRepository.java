package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.RequiredQuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequiredQuestionAnswerRepository extends JpaRepository<RequiredQuestionAnswer, Long> {

    Optional<RequiredQuestionAnswer> findRequiredQuestionAnswerById(Long id);
}

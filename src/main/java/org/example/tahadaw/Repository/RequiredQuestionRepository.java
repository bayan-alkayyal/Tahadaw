package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.RequiredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequiredQuestionRepository extends JpaRepository<RequiredQuestion, Long> {

    Optional<RequiredQuestion> findRequiredQuestionById(Long id);
}

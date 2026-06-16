package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.RequiredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequiredQuestionRepository extends JpaRepository<RequiredQuestion, Long> {
    RequiredQuestion findRequiredQuestionById(Long questionId);
}

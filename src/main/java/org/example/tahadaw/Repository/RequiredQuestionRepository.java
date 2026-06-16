package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.RequiredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequiredQuestionRepository extends JpaRepository<RequiredQuestion, Long> {

    Optional<RequiredQuestion> findRequiredQuestionById(Long id);
}

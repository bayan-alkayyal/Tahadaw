package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.RequiredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequiredQuestionRepository extends JpaRepository<RequiredQuestion, Long> {

    RequiredQuestion findRequiredQuestionById(Long id);
}

package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.AiGeneratedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiGeneratedQuestionRepository extends JpaRepository<AiGeneratedQuestion, Long> {

    Optional<AiGeneratedQuestion> findAiGeneratedQuestionById(Long id);

    List<AiGeneratedQuestion> findByGiftPlan_IdOrderByDisplayOrderAsc(Long giftPlanId);

    boolean existsByGiftPlan_Id(Long giftPlanId);
}

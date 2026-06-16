package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.AiQuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiQuestionAnswerRepository extends JpaRepository<AiQuestionAnswer, Long> {

    Optional<AiQuestionAnswer> findAiQuestionAnswerById(Long id);

    Optional<AiQuestionAnswer> findByAiGeneratedQuestion_Id(Long aiGeneratedQuestionId);

    List<AiQuestionAnswer> findByAiGeneratedQuestion_GiftPlan_IdOrderByCreatedAtAsc(Long giftPlanId);
}
